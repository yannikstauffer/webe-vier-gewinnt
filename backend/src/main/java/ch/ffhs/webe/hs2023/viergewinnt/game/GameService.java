package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;

public interface GameService {
    Game createGame(User currentUser);

    List<Game> getAllGames();

    Game getGameById(final int gameId);

    Game joinGame(final int gameId, final User currentUser);

    Game dropRandomDisc(int gameId, User currentUser);

    Game updateGameBoard(final int gameId, final int column, final User currentUser, final String message);

    void deleteAllGames();

    Game controlGame(GameRequestDto request, final User currentUser);

    void setGameBoardStatesForUser(final User user, final GameBoardState gameBoardState);

    List<Game> getGamesForUser(final int userId);

}