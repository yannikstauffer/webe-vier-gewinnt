package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;

public interface GameService {
    Game createGame(User creator);

    List<Game> getAllGames();

    Game joinGame(GameRequestDto request);

    void leftGame(GameRequestDto request);

    void deleteAllGames();
}