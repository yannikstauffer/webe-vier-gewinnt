package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameBoard;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import org.junit.jupiter.api.Test;

import static ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState.CONNECTED;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState.DISCONNECTED;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState.QUIT;
import static ch.ffhs.webe.hs2023.viergewinnt.user.model.UserTest.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GameTest {

    @Test
    void copyAsNew() {
        // arrange
        final var original = game(1);
        original.getBoard().addDisc(1, original.getNextMove());
        original.setGameLevel(GameLevel.LEVEL2);

        // act
        final var copy = original.copyAsNew();

        // assert
        assertThat(copy.getUserOne()).isEqualTo(original.getUserOne());
        assertThat(copy.getUserTwo()).isEqualTo(original.getUserTwo());
        assertThat(copy.getUserOneState()).isEqualTo(original.getUserOneState());
        assertThat(copy.getUserTwoState()).isEqualTo(original.getUserTwoState());
        assertThat(copy.getGameLevel()).isEqualTo(original.getGameLevel());
        assertThat(copy.getBoard().asListObject()).containsAll((new GameBoard()).asListObject());
        assertThat(copy.getGameState()).isEqualTo(GameState.WAITING_FOR_PLAYERS);

    }

    @Test
    void addUser() {
        // arrange
        final var game = gameWithoutUsers(2);
        final var user1 = user(101);
        final var user2 = user(102);

        // act
        game.addUser(user1);
        game.addUser(user2);

        // assert
        assertThat(game.getUserOne()).isEqualTo(user1);
        assertThat(game.getUserOneState()).isEqualTo(CONNECTED);
        assertThat(game.getUserTwo()).isEqualTo(user2);
        assertThat(game.getUserTwoState()).isEqualTo(CONNECTED);
    }

    @Test
    void addUser_ifNotConnected_setUserConnected() {
        // arrange
        final var game = game(3);
        final var user1 = game.getUserOne();
        final var user2 = game.getUserTwo();
        game.setUserState(user1.getId(), DISCONNECTED);
        game.setUserState(user2.getId(), QUIT);

        // act
        game.addUser(user1);
        game.addUser(user2);

        // assert
        assertThat(game.getUserOne()).isEqualTo(user1);
        assertThat(game.getUserOneState()).isEqualTo(CONNECTED);
        assertThat(game.getUserTwo()).isEqualTo(user2);
        assertThat(game.getUserTwoState()).isEqualTo(CONNECTED);
    }

    @Test
    void addUser_ifTwoUsersAreSet_throw() {
        // arrange
        final var game = game(4);
        final var user3 = user(200);

        // act
        assertThatThrownBy(() -> game.addUser(user3))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Game is full");
    }

    static Game gameWithoutUsers(final int id) {
        final var game = new Game();
        game.setId(id);
        game.setGameState(GameState.WAITING_FOR_PLAYERS);
        return game;

    }

    public static Game game(final int id) {
        final var game = new Game();
        game.setId(id);
        game.setUserOne(user(101));
        game.setUserTwo(user(102));
        game.setUserState(101, CONNECTED);
        game.setUserState(102, CONNECTED);
        game.setGameState(GameState.IN_PROGRESS);
        game.setNextMove(101);
        game.setGameLevel(GameLevel.LEVEL1);
        return game;
    }
}