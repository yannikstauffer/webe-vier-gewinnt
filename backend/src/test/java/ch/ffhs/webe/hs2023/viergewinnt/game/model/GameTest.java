package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;

import static ch.ffhs.webe.hs2023.viergewinnt.user.model.UserTest.user;

public class GameTest {

    public static Game game(final int id) {
        final var game = new Game();
        game.setId(id);
        game.setUserOne(user(101));
        game.setUserTwo(user(102));
        game.setGameState(GameState.IN_PROGRESS);
        game.setGameBoardState(GameBoardState.MOVE_EXPECTED);
        game.setNextMove(101);
        game.setGameLevel(GameLevel.LEVEL1);
        return game;
    }
}