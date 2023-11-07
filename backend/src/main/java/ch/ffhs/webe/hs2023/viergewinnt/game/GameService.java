package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;

public interface GameService {
    Game createGame(User currentUser);

    List<Game> getAllGames();

    Game getGameById(final int gameId);

    Game joinGame(final int gameId, final User currentUser);

    void leftGame(final int gameId, final User currentUser);

    Game updateGameBoard(final int gameId, final int column, final User currentUser);

    void deleteAllGames();

    void startGame(Game game);

    void validatePlayer(Game game, User currentUser);

    void validateGameInProgress(Game game, User currentUser);

}