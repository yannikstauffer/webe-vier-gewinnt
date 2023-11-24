package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameBoard;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
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

    @ManyToOne
    @JoinColumn(name = COL_USER_TWO_ID)
    private User userTwo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState gameState;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameBoardState gameBoardState;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameLevel gameLevel;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "next_move")
    private Integer nextMove;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Column(columnDefinition = "TEXT")
    private String boardJson;

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

    public boolean hasTwoUsers() {
        return this.userOne != null && this.userTwo != null;
    }

    public boolean isMoveExpected() {
        return this.getGameState() == GameState.IN_PROGRESS && this.getGameBoardState() == GameBoardState.MOVE_EXPECTED;
    }
}