package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        newGame.setStatus(GameState.WAITING_FOR_PLAYERS);
        newGame.setUserOne(currentUser);

        GameBoard gameBoard = new GameBoard(); // Verwende GameBoard Klasse
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

        game.setUserTwo(currentUser);
        game.setStatus(GameState.IN_PROGRESS);
        this.gameRepository.save(game);

        return game;
    }

    @Override
    public void leftGame(final int gameId, final User currentUser) {
        final Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.getUserOne().getId() == currentUser.getId()) {
            this.gameRepository.delete(game);
        } else if (game.getUserTwo().getId() == currentUser.getId()) {
            game.setUserTwo(null);
            game.setStatus(GameState.WAITING_FOR_PLAYERS);
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

        // get Board
        GameBoard gameBoard = new GameBoard();
        gameBoard.setBoard(game.getBoard());

        // Gamelogic
        gameBoard.updateBoardColumn(column, currentUser.getId());

        if (gameBoard.isFull()) {
            game.setStatus(GameState.FINISHED);
        }

        // save Board
        game.setBoard(gameBoard.getBoard());
        return gameRepository.save(game);
    }
}
