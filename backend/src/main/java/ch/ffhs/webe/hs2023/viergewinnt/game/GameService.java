package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;

public interface GameService {
    Game createGame(User currentUser);

    List<Game> getAllGames();

    Game getGameById(final int gameId);

    Game joinGame(final int gameId, final User currentUser);

    Game updateGameBoard(final int gameId, final int column, final User currentUser);

    void deleteAllGames();

    Game controlGame(GameRequestDto request, final User currentUser);

    List<Game> getGamesForUser(final User user);

}