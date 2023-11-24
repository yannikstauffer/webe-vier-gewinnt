package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class GameServiceImpl implements GameService {

    private static final Random RANDOM = new Random();
    private final GameRepository gameRepository;
    private final UserService userService;

    @Autowired
    public GameServiceImpl(final GameRepository gameRepository, final UserService userService) {
        this.gameRepository = gameRepository;
        this.userService = userService;
    }

    @Override
    public Game createGame(final User currentUser) {
        final Game newGame = new Game();
        newGame.setGameState(GameState.WAITING_FOR_PLAYERS);
        newGame.setGameBoardState(GameBoardState.WAITING_FOR_PLAYERS);
        newGame.setGameLevel(GameLevel.LEVEL1);
        newGame.setUserOne(currentUser);
        newGame.setBoard(new GameBoard());

        final Game savedGame = this.gameRepository.save(newGame);
        log.debug("Saved new game with ID: " + savedGame.getId());

        return savedGame;
    }

    @Override
    public List<Game> getAllGames() {
        final Iterable<Game> gamesIterable = this.gameRepository.findAll();

        final List<Game> games = new ArrayList<>();
        gamesIterable.forEach(games::add);

        return games;
    }

    @Override
    public Game joinGame(final int gameId, final User currentUser) {
        final Game game = this.findGameOrThrow(gameId);

        if (game.hasTwoUsers() && !isUserOne(game, currentUser) && !isUserTwo(game, currentUser)) {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Das Spiel ist bereits voll!");
        }

        if (game.getUserOne() == null && !isUserTwo(game, currentUser)) {
            game.setUserOne(currentUser);
        } else if (game.getUserTwo() == null && !isUserOne(game, currentUser)) {
            game.setUserTwo(currentUser);
        }


        if (game.hasTwoUsers() && game.getBoard().contains(currentUser.getId())) {
            game.setGameBoardState(GameBoardState.READY_TO_CONTINUE);
        } else if (game.hasTwoUsers()) {
            game.setGameBoardState(GameBoardState.READY_TO_START);
        }

        this.userService.setCurrentGameId(currentUser.getId(), game.getId());

        return this.gameRepository.save(game);
    }

    @Override
    public Game controlGame(final GameRequestDto request, final User currentUser) {
        final Game game = this.findGameOrThrow(request.getGameId());

        this.validatePlayer(game, currentUser);
        Game updatedGame = game;

        switch (request.getMessage()) {
            case "start" -> updatedGame = this.startGame(game);
            case "restart" -> updatedGame = this.restartGame(game);
            case "continue" -> updatedGame = this.continueGame(game);
            case "leave" -> {
                updatedGame = this.removePlayerFromGame(game, currentUser);

                if (this.bothUsersLeft(updatedGame)) {
                    if (updatedGame.getGameState() == GameState.IN_PROGRESS) {
                        updatedGame.setGameState(GameState.NOT_FINISHED);
                    } else if (updatedGame.getGameState() == GameState.WAITING_FOR_PLAYERS) {
                        updatedGame.setGameState(GameState.NEVER_STARTED);
                    }
                } else {
                    updatedGame.setGameState(GameState.WAITING_FOR_PLAYERS);
                    updatedGame.setGameBoardState(GameBoardState.PLAYER_QUIT);
                }
            }
            case "LEVEL1" -> updatedGame.setGameLevel(GameLevel.LEVEL1);
            case "LEVEL2" -> updatedGame.setGameLevel(GameLevel.LEVEL2);
            case "LEVEL3" -> updatedGame.setGameLevel(GameLevel.LEVEL3);
            default -> {
                return updatedGame;
            }
        }

        return this.gameRepository.save(updatedGame);
    }

    @Override
    public Game getGameById(final int gameId) {
        final Game game = this.findGameOrThrow(gameId);

        return this.gameRepository.save(game);
    }

    @Override
    public void setGameBoardStatesForUser(final User user, final GameBoardState gameBoardState) {
        final List<Game> gamesForUser = this.gameRepository.findGamesByUserId(user.getId());

        for (final Game game : gamesForUser) {
            if (game.getGameState() == GameState.IN_PROGRESS) {
                game.setGameState(GameState.PAUSED);
            }
            game.setGameBoardState(gameBoardState);
        }

        this.gameRepository.saveAll(gamesForUser);
    }

    @Override
    public List<Game> getGamesForUser(final int userId) {
        return this.gameRepository.findGamesByUserId(userId);
    }

    @Override
    public Game dropRandomDisc(final int gameId, final User currentUser) {
        final Game game = this.findGameOrThrow(gameId);

        if (game.isMoveExpected()) {
            final var availableRandomColumn = this.randomAvailableColumn(game);
            if (availableRandomColumn.isPresent()) {
                log.trace("Drop random disc {} for user {} in game {}", availableRandomColumn.get(), currentUser.getId(), game.getId());
                return this.dropPlayerDisc(game, availableRandomColumn.get(), currentUser);
            } else {
                log.warn("GameState indicates that game {} is still active but no free columns are available.", game.getId());
            }
        }

        return game;
    }

    @Override
    public Game dropRandomAnonymousDisc(final int gameId) {
        final Game game = this.findGameOrThrow(gameId);

        if (game.isMoveExpected()) {
            final var availableRandomColumn = this.randomAvailableColumn(game);
            if (availableRandomColumn.isPresent()) {
                log.trace("Drop anonymous random disc {} in game {}", availableRandomColumn.get(), game.getId());
                return this.dropAnonymousDisc(game, availableRandomColumn.get());
            } else {
                log.warn("GameState indicates that game {} is still active but no free columns are available.", game.getId());
            }
        }

        return game;
    }

    @Override
    public Game dropDisc(final int gameId, final int column, final User currentUser) {
        final Game game = this.findGameOrThrow(gameId);
        return this.dropPlayerDisc(game, column, currentUser);
    }

    private Game initializeNewGame(final Game game) {
        game.setBoard(new GameBoard());
        game.setGameState(GameState.IN_PROGRESS);
        game.setGameBoardState(GameBoardState.MOVE_EXPECTED);
        game.setNextMove(RANDOM.nextBoolean() ? game.getUserOne().getId() : game.getUserTwo().getId());

        return this.gameRepository.save(game);
    }

    private Game startGame(final Game game) {
        this.validateTwoPlayers(game);
        return this.initializeNewGame(game);
    }

    private Game restartGame(final Game game) {
        this.validateTwoPlayers(game);

        final Game newGame = new Game();
        newGame.setGameLevel(game.getGameLevel());
        newGame.setUserOne(game.getUserOne());
        newGame.setUserTwo(game.getUserTwo());

        return this.initializeNewGame(newGame);
    }

    private Game continueGame(final Game game) {
        this.validateTwoPlayers(game);

        game.setGameState(GameState.IN_PROGRESS);
        game.setGameBoardState(GameBoardState.MOVE_EXPECTED);

        return game;
    }

    private Game removePlayerFromGame(final Game game, final User currentUser) {
        this.validatePlayer(game, currentUser);

        if (isUserOne(game, currentUser)) {
            game.setUserOne(null);
        }
        if (isUserTwo(game, currentUser)) {
            game.setUserTwo(null);

        }

        return this.gameRepository.save(game);
    }

    private Optional<Integer> randomAvailableColumn(final Game game) {
        final var gameBoard = game.getBoard();

        if (gameBoard.isFull()) {
            return Optional.empty();
        }

        int randomColumn = RANDOM.nextInt(7);

        int i = 0;
        while (gameBoard.isColumnFull(randomColumn)) {
            if (i++ > 6) {
                log.warn("All columns in game {} are full but expected not to be.", game.getId());
                return Optional.empty();
            }
            randomColumn = randomColumn + 1 % 7;
        }

        return Optional.of(randomColumn);
    }


    private Game dropPlayerDisc(final Game game, final int columnId, final User currentUser) {
        this.validateGameInProgress(game);
        this.validatePlayer(game, currentUser);
        this.validateNextMove(game, currentUser);

        final var gameBoard = this.gameBoardWithDisc(game, columnId, currentUser.getId());

        this.updateGame(game, gameBoard, currentUser);
        return this.gameRepository.save(game);
    }


    private Game dropAnonymousDisc(final Game game, final int columnId) {
        this.validateGameInProgress(game);

        final var gameBoard = this.gameBoardWithDisc(game, columnId, GameBoard.ANONYMOUS_DISC_NUMBER);

        this.updateGame(game, gameBoard, null);
        return this.gameRepository.save(game);
    }

    private void updateGame(final Game game, final GameBoard gameBoard, @Nullable final User optionalUser) {
        final boolean hasWon = Optional.ofNullable(optionalUser)
                .map(User::getId)
                .map(gameBoard::checkWinner)
                .orElse(false);

        if (hasWon) {
            game.setGameBoardState(GameBoardState.PLAYER_HAS_WON);
            game.setGameState(GameState.FINISHED);
        } else if (gameBoard.isFull()) {
            game.setGameBoardState(GameBoardState.DRAW);
            game.setGameState(GameState.FINISHED);
        } else if (optionalUser != null) {
            game.setNextMove(game.getNextMove().equals(game.getUserOne().getId()) ?
                    game.getUserTwo().getId() : game.getUserOne().getId());
        }

        game.setBoard(gameBoard);
    }

    private void validateNextMove(final Game game, final User currentUser) {
        if (currentUser.getId() != game.getNextMove()) {
            throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "The current user is not allowed to drop a disc.");
        }
    }

    private GameBoard gameBoardWithDisc(final Game game, final int columnId, final int discNumber) {
        final GameBoard gameBoard = game.getBoard();

        if (gameBoard.isColumnFull(columnId)) {
            throw VierGewinntException.of(ErrorCode.INVALID_MOVE, "Ungültiger Zug, Spalte ist voll!");
        }

        gameBoard.addDisc(columnId, discNumber);
        return gameBoard;
    }


    private void validatePlayer(final Game game, final User currentUser) {
        if (currentUser == null) {
            throw VierGewinntException.of(ErrorCode.NULL_PLAYER, "Player was not set.");
        }

        if (!isUserOne(game, currentUser) && !isUserTwo(game, currentUser)) {
            throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "The current user is not part of this game.");

        }
    }

    private void validateGameInProgress(final Game game) {
        if (game.getGameState() != GameState.IN_PROGRESS) {
            throw VierGewinntException.of(ErrorCode.INVALID_GAME_STATE, "The game state should be IN_PROGRESS");
        }
    }

    private void validateTwoPlayers(final Game game) {
        if (game.getUserOne() == null || game.getUserTwo() == null) {
            throw VierGewinntException.of(ErrorCode.GAME_NOT_READY, "Warten auf Spieler!");
        }
    }

    private Game findGameOrThrow(final int gameId) {
        return this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));
    }

    private boolean bothUsersLeft(final Game game) {
        return game.getUserOne() == null && game.getUserTwo() == null;
    }

    private static boolean isUserOne(final Game game, final User currentUser) {
        return game.getUserOne() != null && game.getUserOne().getId() == currentUser.getId();
    }

    private static boolean isUserTwo(final Game game, final User currentUser) {
        return game.getUserTwo() != null && game.getUserTwo().getId() == currentUser.getId();
    }


}
