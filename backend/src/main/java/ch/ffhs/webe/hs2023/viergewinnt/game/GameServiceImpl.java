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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class GameServiceImpl implements GameService {

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


        GameBoard gameBoard = new GameBoard();
        newGame.setBoard(gameBoard.getBoard());

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
        final Game game = findGameOrThrow(gameId);

        if (game.isFull() && !isUserOneCurrentUser(game, currentUser) && !isUserTwoCurrentUser(game, currentUser)) {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Das Spiel ist bereits voll!");
        }

        if (game.getUserOne() == null && !isUserTwoCurrentUser(game, currentUser)) {
            game.setUserOne(currentUser);
        } else if (game.getUserTwo() == null && !isUserOneCurrentUser(game, currentUser)) {
            game.setUserTwo(currentUser);
        }

        if (game.isFull() && game.isPlayerInBoard(currentUser.getId())) {
            game.setGameBoardState(GameBoardState.READY_TO_CONTINUE);
        } else if (game.isFull()) {
            game.setGameBoardState(GameBoardState.READY_TO_START);
        }

        this.userService.setCurrentGameId(currentUser.getId(), game.getId());

        return this.gameRepository.save(game);
    }

    @Override
    public Game controlGame(GameRequestDto request, final User currentUser) {
        final Game game = findGameOrThrow(request.getGameId());

        validatePlayer(game, currentUser);
        Game updatedGame = game;

        switch (request.getMessage()) {
            case "start" -> updatedGame = startGame(game);
            case "restart" -> updatedGame = restartGame(game);
            case "continue" -> updatedGame = continueGame(game);
            case "leave" -> {
                updatedGame = removePlayerFromGame(game, currentUser);

                if (bothUsersLeft(updatedGame)) {
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

    private Game initializeGame(Game game, boolean isNewGame) {
        if (isNewGame) {
            GameBoard gameBoard = new GameBoard();
            gameBoard.resetBoard();
            game.setBoard(gameBoard.getBoard());
        }

        game.setGameState(GameState.IN_PROGRESS);
        game.setGameBoardState(GameBoardState.MOVE_EXPECTED);
        game.setNextMove(new Random().nextBoolean() ? game.getUserOne().getId() : game.getUserTwo().getId());

        return this.gameRepository.save(game);
    }

    private Game startGame(Game game) {
        validateTwoPlayers(game);
        return initializeGame(game, true);
    }

    private Game restartGame(Game game) {
        validateTwoPlayers(game);

        Game newGame = new Game();
        newGame.setGameLevel(game.getGameLevel());
        newGame.setUserOne(game.getUserOne());
        newGame.setUserTwo(game.getUserTwo());

        return initializeGame(newGame, true);
    }

    private Game continueGame(Game game) {
        validateTwoPlayers(game);

        game.setGameState(GameState.IN_PROGRESS);
        game.setGameBoardState(GameBoardState.MOVE_EXPECTED);

        return game;
    }

    private Game removePlayerFromGame(Game game, User currentUser) {
        validatePlayer(game, currentUser);

        if (game.getUserOne() != null) {
            if (game.getUserOne().getId() == currentUser.getId()) {
                game.setUserOne(null);
            }
        }

        if (game.getUserTwo() != null) {
            if (game.getUserTwo().getId() == currentUser.getId()) {
                game.setUserTwo(null);
            }
        }

        return this.gameRepository.save(game);
    }

    @Override
    public void deleteAllGames() {
        this.gameRepository.deleteAll();
    }

    @Override
    public Game updateGameBoard(int gameId, int column, final User currentUser, final String message) {
        final Game game = findGameOrThrow(gameId);

        validateGameInProgress(game, currentUser);

        GameBoard gameBoard = new GameBoard();
        gameBoard.setBoard(game.getBoard());
        boolean isUpdated = true;

        if(game.getGameLevel() == GameLevel.LEVEL3 && message.equals("specialDisc")){
            gameBoard.updateBoardColumn(column, -5);
        } else {
            gameBoard.updateBoardColumn(column, currentUser.getId());
        }

        if (!isUpdated) {
            throw VierGewinntException.of(ErrorCode.INVALID_MOVE, "Ungültiger Zug, Spalte ist voll!");
        }

        boolean hasWon = gameBoard.checkWinner(currentUser.getId());

        if (hasWon) {
            game.setGameBoardState(GameBoardState.PLAYER_HAS_WON);
            game.setGameState(GameState.FINISHED);
        } else if (gameBoard.isFull()) {
            game.setGameBoardState(GameBoardState.DRAW);
            game.setGameState(GameState.FINISHED);
        } else {
            game.setNextMove(game.getNextMove().equals(game.getUserOne().getId()) ?
                    game.getUserTwo().getId() : game.getUserOne().getId());
        }

        game.setBoard(gameBoard.getBoard());
        return this.gameRepository.save(game);
    }

    @Override
    public Game getGameById(final int gameId) {
        final Game game = findGameOrThrow(gameId);

        return this.gameRepository.save(game);
    }

    @Override
    public void setGameBoardStatesForUser(final User user, final GameBoardState gameBoardState) {
        List<Game> gamesForUser = this.gameRepository.findGamesByUserId(user.getId());

        for (Game game : gamesForUser) {
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

    private void validatePlayer(Game game, User currentUser) {
        if (currentUser == null) {
            throw VierGewinntException.of(ErrorCode.NULL_PLAYER, "Player was not set.");
        }

        if (game.getUserOne() != null && game.getUserTwo() != null) {
            if (game.getUserOne().getId() != currentUser.getId() && game.getUserTwo().getId() != currentUser.getId()) {
                throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "The current user is not part of this game.");
            }
        }
    }

    private void validateGameInProgress(Game game, User currentUser) {
        validatePlayer(game, currentUser);
        if (game.getGameState() != GameState.IN_PROGRESS) {
            throw VierGewinntException.of(ErrorCode.INVALID_GAME_STATE, "The game state should be IN_PROGRESS");
        }
    }

    private void validateTwoPlayers(Game game) {
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

    private boolean isUserOneCurrentUser(final Game game, User currentUser) {
        if (game.getUserOne() != null) {
            return game.getUserOne().getId() == currentUser.getId();
        } else {
            return false;
        }
    }

    private boolean isUserTwoCurrentUser(final Game game, User currentUser) {
        if (game.getUserTwo() != null) {
            return game.getUserTwo().getId() == currentUser.getId();
        } else {
            return false;
        }
    }

}
