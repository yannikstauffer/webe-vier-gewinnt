package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
    @JoinColumn(name = COL_USER_ONE_ID, nullable = false, updatable = false)
    private User userOne;

    @ManyToOne
    @JoinColumn(name = COL_USER_TWO_ID)
    private User userTwo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameState gameState;

    @Enumerated(EnumType.STRING)
    private GameBoardState gameBoardState;

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

    public ArrayList<ArrayList<Integer>> getBoard() {
        if (boardJson == null || boardJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(boardJson, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw VierGewinntException.of(ErrorCode.GAMEBOARD_READ_ERROR, e);
        }
    }

    public void setBoard(ArrayList<ArrayList<Integer>> board) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.boardJson = mapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw VierGewinntException.of(ErrorCode.GAMEBOARD_WRITE_ERROR, e);
        }
    }


    public boolean isFull() {
        return userOne != null && userTwo != null;
    }
}