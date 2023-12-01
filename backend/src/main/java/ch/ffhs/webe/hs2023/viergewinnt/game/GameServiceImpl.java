package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState;
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

import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.CONTINUE;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEAVE;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEVEL1;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEVEL2;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEVEL3;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.RESTART;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.START;

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
        newGame.addUser(currentUser);

        final Game savedGame = this.gameRepository.save(newGame);
        log.debug("Saved new game with ID: " + savedGame.getId());

        return savedGame;
    }

    @Override
    public List<Game> getAllGames() {
        return this.gameRepository.findCurrentlyActive();
    }

    @Override
    public Game joinGame(final int gameId, final User currentUser) {
        final Game game = this.findGameOrThrow(gameId);

        if (game.hasTwoUsers() && !game.containsUser(currentUser.getId())) {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Game is full");
        }

        game.addUser(currentUser);

        final var savedGame = this.gameRepository.save(game);
        this.userService.setCurrentGameId(currentUser.getId(), savedGame.getId());

        return savedGame;
    }

    @Override
    public Game controlGame(final GameRequestDto request, final User currentUser) {
        final Game game = this.findGameOrThrow(request.getGameId());

        this.validatePlayer(game, currentUser);

        Game updatedGame = game;
        switch (request.getMessage()) {
            case START -> updatedGame.start();
            case RESTART -> updatedGame = this.restartGame(updatedGame);
            case CONTINUE -> updatedGame.resume();
            case LEAVE -> this.leaveGame(updatedGame, currentUser);
            case LEVEL1 -> updatedGame.setGameLevel(GameLevel.LEVEL1);
            case LEVEL2 -> updatedGame.setGameLevel(GameLevel.LEVEL2);
            case LEVEL3 -> updatedGame.setGameLevel(GameLevel.LEVEL3);
            default -> throw VierGewinntException.of(ErrorCode.UNKNOWN, "Unknown game message " + request.getMessage());
        }

        if (game.getUsers().isEmpty() || game.bothUsersLeftAfterAbort()) {
            this.gameRepository.delete(game);
            game.setGameState(GameState.DELETED);
            return game;
        }

        return this.gameRepository.save(updatedGame);
    }

    @Override
    public Game getGameById(final int gameId) {
        return this.findGameOrThrow(gameId);
    }

    @Override
    public void setUserAsDisconnected(final User user, final List<Game> games) {
        final List<Game> updated = new ArrayList<>();
        for (final Game game : games) {
            if (game.isReadyToStart()) {
                game.clearUser(user.getId());
                updated.add(game);
            } else if (game.isNotDone()) {
                game.setUserState(user.getId(), UserState.DISCONNECTED);
                if (game.getGameState() == GameState.IN_PROGRESS) {
                    game.setGameState(GameState.PAUSED);
                }
                updated.add(game);
            }
        }

        this.gameRepository.saveAll(updated);
    }

    @Override
    public List<Game> getGamesForUser(final int userId) {
        return this.gameRepository.findCurrentlyActiveForUserId(userId);
    }

    @Override
    public Game dropRandomDisc(final int gameId, final User currentUser) {
        final Game game = this.findGameOrThrow(gameId);

        if (game.isMoveExpected()) {
            final var availableRandomColumn = this.randomAvailableColumn(game);
            if (availableRandomColumn.isPresent()) {
                log.trace("Drop random disc {} for user {} in game {}", availableRandomColumn.get(), currentUser.getId(), game.getId());
                this.dropPlayerDisc(game, availableRandomColumn.get(), currentUser);
                return this.gameRepository.save(game);
            } else {
                log.error("GameState indicates that game {} is still active but no free columns are available.", game.getId());
                throw new IllegalStateException("GameState does not match GameBoard." +
                        " GameBoard is full and GameState should be a final state.");
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
                this.dropAnonymousDisc(game, availableRandomColumn.get());
                return this.gameRepository.save(game);
            } else {
                log.error("GameState indicates that game {} is still active but no free columns are available.", game.getId());
                throw new IllegalStateException("GameState does not match GameBoard." +
                        " GameBoard is full and GameState should be a final state.");
            }
        }

        return game;
    }

    @Override
    public Game dropDisc(final int gameId, final int column, final User currentUser) {
        final Game game = this.findGameOrThrow(gameId);
        this.dropPlayerDisc(game, column, currentUser);
        return this.gameRepository.save(game);
    }

    @Override
    public void setAllConnectedUsersAsDisconnected() {
        final List<Game> games = this.gameRepository.findByUserState(UserState.CONNECTED);

        for (final Game game : games) {
            if (game.getGameState() == GameState.IN_PROGRESS) {
                game.setGameState(GameState.PAUSED);
            }
            if (game.getUserOneState() == UserState.CONNECTED) {
                game.setUserState(game.getUserOne().getId(), UserState.DISCONNECTED);
            }
            if (game.getUserTwoState() == UserState.CONNECTED) {
                game.setUserState(game.getUserTwo().getId(), UserState.DISCONNECTED);
            }
        }

        this.gameRepository.saveAll(games);
    }

    private void leaveGame(final Game game, final User currentUser) {
        if (game.isFinished()) {
            game.setUserState(currentUser.getId(), UserState.QUIT);
        } else if (game.isWaitingForPlayers()) {
            game.clearUser(currentUser.getId());
        } else {
            game.setUserState(currentUser.getId(), UserState.QUIT);
            game.setGameState(GameState.PLAYER_LEFT);
        }
    }

    private Game restartGame(final Game game) {
        final Game newGame = game.copyAsNew();
        newGame.start();
        return newGame;
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


    void dropPlayerDisc(final Game game, final int columnId, final User currentUser) {
        this.validateIsMoveExpected(game);
        this.validatePlayer(game, currentUser);
        this.validateNextMove(game, currentUser);

        final var gameBoard = this.gameBoardWithDisc(game, columnId, currentUser.getId());

        this.setNewStatesAfterDiscDrop(game, gameBoard, currentUser);
    }


    private void dropAnonymousDisc(final Game game, final int columnId) {
        this.validateIsMoveExpected(game);

        final var gameBoard = this.gameBoardWithDisc(game, columnId, GameBoard.ANONYMOUS_DISC_NUMBER);

        this.setNewStatesAfterDiscDrop(game, gameBoard, null);
    }

    private void setNewStatesAfterDiscDrop(final Game game, final GameBoard gameBoard, @Nullable final User optionalUser) {
        final boolean hasWon = Optional.ofNullable(optionalUser)
                .map(User::getId)
                .map(gameBoard::checkWinner)
                .orElse(false);

        if (hasWon) {
            game.setGameState(GameState.PLAYER_HAS_WON);
        } else if (gameBoard.isFull()) {
            game.setGameState(GameState.DRAW);
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

    GameBoard gameBoardWithDisc(final Game game, final int columnId, final int discNumber) {
        final GameBoard gameBoard = game.getBoard();

        if (gameBoard.isColumnFull(columnId)) {
            throw VierGewinntException.of(ErrorCode.INVALID_MOVE, "Invalid move. Column is already full.");
        }

        gameBoard.addDisc(columnId, discNumber);
        return gameBoard;
    }


    private void validatePlayer(final Game game, final User currentUser) {
        if (currentUser == null) {
            throw VierGewinntException.of(ErrorCode.NULL_PLAYER, "Player was not set.");
        }

        if (!game.containsUser(currentUser.getId())) {
            throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "The current user is not part of this game.");

        }
    }

    private void validateIsMoveExpected(final Game game) {
        if (!game.isMoveExpected()) {
            throw VierGewinntException.of(ErrorCode.INVALID_GAME_STATE, "The game state should be IN_PROGRESS");
        }
    }


    private Game findGameOrThrow(final int gameId) {
        return this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));
    }

}
