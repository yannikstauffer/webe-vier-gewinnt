package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
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

    @Autowired
    public GameServiceImpl(final GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(final User currentUser) {
        final Game newGame = new Game();
        newGame.setGameState(GameState.WAITING_FOR_PLAYERS);
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
        final Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.isFull()) {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Das Spiel ist bereits voll!");
        }

        if (game.getUserOne() != null && game.getUserTwo() == null) {
            game.setUserTwo(currentUser);
        }

        return this.gameRepository.save(game);
    }

    @Override
    public Game startGame(Game game) {
        game.setGameState(GameState.IN_PROGRESS);
        game.setGameBoardState(GameBoardState.MOVE_EXPECTED);
        game.setNextMove(new Random().nextBoolean() ? game.getUserOne().getId() : game.getUserTwo().getId());

        return gameRepository.save(game);
    }

    @Override
    public Game restartGame(Game game) {
        Game newGame = new Game();
        newGame.setUserOne(game.getUserOne());
        newGame.setUserTwo(game.getUserTwo());
        GameBoard gameBoard = new GameBoard();
        gameBoard.resetBoard();
        newGame.setBoard(gameBoard.getBoard());

        newGame.setGameState(GameState.IN_PROGRESS);
        newGame.setGameBoardState(GameBoardState.MOVE_EXPECTED);
        newGame.setNextMove(new Random().nextBoolean() ? game.getUserOne().getId() : game.getUserTwo().getId());

        return gameRepository.save(newGame);
    }

    @Override
    public void leftGame(final int gameId, final User currentUser) {
        final Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.getUserOne().getId() == currentUser.getId()) {
            this.gameRepository.delete(game);
        } else if (game.getUserTwo().getId() == currentUser.getId()) {
            game.setUserTwo(null);
            game.setGameState(GameState.WAITING_FOR_PLAYERS);
            this.gameRepository.save(game);
        }
    }

    @Override
    public void deleteAllGames() {
        this.gameRepository.deleteAll();
    }

    @Override
    public Game updateGameBoard(int gameId, int column, final User currentUser) {
        final Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        validateGameInProgress(game, currentUser);

        GameBoard gameBoard = new GameBoard();
        gameBoard.setBoard(game.getBoard());
        boolean isUpdated = gameBoard.updateBoardColumn(column, currentUser.getId());

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
        return gameRepository.save(game);
    }

    @Override
    public Game getGameById(final int gameId) {
        final Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        return this.gameRepository.save(game);
    }

    @Override
    public void validatePlayer(Game game, User currentUser) {
        if (currentUser == null) {
            throw VierGewinntException.of(ErrorCode.NULL_PLAYER, "Player was not set.");
        }

        if (game.getUserOne().getId() != currentUser.getId() && game.getUserTwo().getId() != currentUser.getId()) {
            throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "The current user is not part of this game.");
        }
    }

    @Override
    public void validateGameInProgress(Game game, User currentUser) {
        validatePlayer(game, currentUser);
        if (game.getGameState() != GameState.IN_PROGRESS) {
            throw VierGewinntException.of(ErrorCode.INVALID_GAME_STATE, "The game state should be IN_PROGRESS");
        }

        if (game.getUserOne() == null || game.getUserTwo() == null) {
            throw VierGewinntException.of(ErrorCode.GAME_NOT_READY, "Warten auf Spieler!");
        }
    }

}
