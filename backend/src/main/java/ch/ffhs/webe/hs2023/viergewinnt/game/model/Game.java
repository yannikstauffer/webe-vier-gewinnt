package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameBoard;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "games")
public class Game {

    private static final String COL_USER_ONE_ID = "user_one_id";
    private static final String COL_USER_TWO_ID = "user_two_id";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = COL_USER_ONE_ID)
    private User userOne;

    @Setter(AccessLevel.PRIVATE)
    @Enumerated(EnumType.STRING)
    private UserState userOneState;

    @ManyToOne
    @JoinColumn(name = COL_USER_TWO_ID)
    private User userTwo;

    @Setter(AccessLevel.PRIVATE)
    @Enumerated(EnumType.STRING)
    private UserState userTwoState;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState gameState = GameState.WAITING_FOR_PLAYERS;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameLevel gameLevel = GameLevel.LEVEL1;

    @Column(name = "next_move")
    private Integer nextMove;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Column(columnDefinition = "TEXT")
    private String boardJson;

    public Game copyAsNew() {
        final var game = new Game();
        game.setUserOne(this.userOne);
        game.setUserOneState(this.userOneState);
        game.setUserTwo(this.userTwo);
        game.setUserTwoState(this.userTwoState);
        game.setGameLevel(this.gameLevel);
        return game;
    }

    public GameBoard getBoard() {
        if (this.boardJson == null || this.boardJson.isEmpty()) {
            return new GameBoard();
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final List<List<Integer>> arrayLists = mapper.readValue(this.boardJson, new TypeReference<>() {
            });
            return new GameBoard(arrayLists);
        } catch (final IOException e) {
            throw VierGewinntException.of(ErrorCode.GAMEBOARD_READ_ERROR, e);
        }
    }

    public void setBoard(final GameBoard board) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            this.boardJson = mapper.writeValueAsString(board.asListObject());
        } catch (final JsonProcessingException e) {
            throw VierGewinntException.of(ErrorCode.GAMEBOARD_WRITE_ERROR, e);
        }
    }

    public List<User> getUsers() {
        final var users = new ArrayList<User>(2);
        if (this.userOne != null) {
            users.add(this.userOne);
        }
        if (this.userTwo != null) {
            users.add(this.userTwo);
        }
        return users;
    }

    public void setGameState(final GameState gameState) {
        this.validateNextState(gameState);
        this.gameState = gameState;
    }

    public boolean isUserOneConnected() {
        return this.userOne != null && this.userOneState == UserState.CONNECTED;
    }

    public boolean isUserTwoConnected() {
        return this.userTwo != null && this.userTwoState == UserState.CONNECTED;
    }

    public boolean hasTwoUsers() {
        return this.userOne != null && this.userTwo != null;
    }

    public boolean isWaitingForPlayers() {
        return this.getGameState() == GameState.WAITING_FOR_PLAYERS;
    }

    public boolean isReadyToStart() {
        return this.getGameState() == GameState.WAITING_FOR_PLAYERS
                && this.bothUsersAreConnected();
    }

    public boolean isMoveExpected() {
        return this.getGameState() == GameState.IN_PROGRESS
                && this.bothUsersAreConnected();
    }

    public boolean isMoveExpectedBy(final User user) {
        return this.getGameState() == GameState.IN_PROGRESS
                && this.bothUsersAreConnected()
                && this.getNextMove() == user.getId();
    }

    public boolean isPaused() {
        return this.getGameState() == GameState.PAUSED
                && !this.bothUsersAreConnected();
    }

    public boolean isReadyToContinue() {
        return (this.getGameState() == GameState.PAUSED || this.getGameState() == GameState.PLAYER_LEFT)
                && this.bothUsersAreConnected();
    }

    public boolean isFinished() {
        return this.getGameState() == GameState.PLAYER_HAS_WON
                || this.getGameState() == GameState.DRAW;
    }

    public boolean bothUsersAreConnected() {
        return this.userOne != null
                && this.userTwo != null
                && this.userOneState == UserState.CONNECTED
                && this.userTwoState == UserState.CONNECTED;
    }

    public boolean bothUsersLeftAfterAbort() {
        return !this.isFinished()
                && this.userOneState == UserState.QUIT
                && this.userTwoState == UserState.QUIT;
    }

    public boolean isNotDone() {
        return this.getGameState() == GameState.IN_PROGRESS
                || this.getGameState() == GameState.PAUSED;
    }

    public void setUserState(final int userId, final UserState userState) {
        if (this.userOne != null && this.userOne.getId() == userId) {
            this.userOneState = userState;
        } else if (this.userTwo != null && this.userTwo.getId() == userId) {
            this.userTwoState = userState;
        } else {
            throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "User " + userId + " not found in game");
        }
    }

    public void clearUser(final int userId) {
        if (!this.isWaitingForPlayers()) {
            throw new IllegalStateException("Cannot remove user " + userId
                    + " from game " + this.id
                    + " because it is no longer in the state WAITING_FOR_PLAYERS but "
                    + this.gameState);
        }

        if (this.userOne != null && this.userOne.getId() == userId) {
            this.userOne = null;
            this.userOneState = null;
        } else if (this.userTwo != null && this.userTwo.getId() == userId) {
            this.userTwo = null;
            this.userTwoState = null;
        } else {
            throw VierGewinntException.of(ErrorCode.INVALID_PLAYER, "User " + userId + " not found in game");
        }
    }

    public boolean containsUser(final int userId) {
        return this.userOne != null && this.userOne.getId() == userId
                || this.userTwo != null && this.userTwo.getId() == userId;
    }

    public void addUser(final User user) {
        if (this.containsUser(user.getId())) {
            this.setUserState(user.getId(), UserState.CONNECTED);
            return;
        }

        if (this.userOne == null) {
            this.userOne = user;
            this.userOneState = UserState.CONNECTED;
        } else if (this.userTwo == null) {
            this.userTwo = user;
            this.userTwoState = UserState.CONNECTED;
        } else {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Game is full");
        }
    }

    public void start() {
        if (!this.isReadyToStart()) {
            throw VierGewinntException.of(ErrorCode.GAME_NOT_READY, "Game is not ready to start");
        }
        this.gameState = GameState.IN_PROGRESS;
        this.nextMove = System.currentTimeMillis() % 2 == 0 ? this.userOne.getId() : this.userTwo.getId();
    }

    public void resume() {
        if (!this.isReadyToContinue()) {
            throw VierGewinntException.of(ErrorCode.GAME_NOT_READY, "Game is not ready to continue");
        }
        this.gameState = GameState.IN_PROGRESS;
    }

    boolean isValidNextState(final GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("State must not be null");
        }

        return !this.isFinished()
                || (state == GameState.DELETED || state == this.gameState)
                || (this.isReadyToStart() && state == GameState.IN_PROGRESS)
                || (this.gameState == GameState.IN_PROGRESS && state == GameState.PAUSED)
                || (this.isReadyToContinue() && state == GameState.IN_PROGRESS)
                || (this.gameState == GameState.IN_PROGRESS && state == GameState.PLAYER_HAS_WON)
                || (this.gameState == GameState.IN_PROGRESS && state == GameState.DRAW)
                || (this.gameState == GameState.IN_PROGRESS && state == GameState.PLAYER_LEFT)
                || (this.gameState == GameState.PAUSED && state == GameState.PLAYER_LEFT);

    }

    private void validateNextState(final GameState state) {
        if (this.isValidNextState(state)) {
            return;
        }

        throw new IllegalArgumentException("Invalid state transition from " + this.gameState + " to " + state);
    }

}