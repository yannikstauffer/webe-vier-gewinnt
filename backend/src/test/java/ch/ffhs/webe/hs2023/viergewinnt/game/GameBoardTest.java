package ch.ffhs.webe.hs2023.viergewinnt.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static ch.ffhs.webe.hs2023.viergewinnt.game.GameBoard.COLUMN_COUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GameBoardTest {

    @Test
    void constructor() {
        final var gameBoard = new GameBoard();

        assertThat(gameBoard.asListObject()).hasSize(GameBoard.ROW_COUNT);
        for (final var row : gameBoard.asListObject()) {
            assertThat(row).hasSize(GameBoard.COLUMN_COUNT);
            assertThat(row).containsOnly(GameBoard.EMPTY_CELL);
        }
    }

    @Test
    void constructor_withData() {
        // arrange
        final List<List<Integer>> boardData = new ArrayList<>();
        IntStream.range(0, GameBoard.ROW_COUNT)
                .forEach(row -> {
                    final List<Integer> boardDataRow = new ArrayList<>();
                    IntStream.range(0, GameBoard.COLUMN_COUNT)
                            .forEach(boardDataRow::add);
                    boardData.add(boardDataRow);
                });

        // act
        final var gameBoard = new GameBoard(boardData);
        final var result = gameBoard.asListObject();

        // assert
        assertThat(result).isEqualTo(boardData);
    }

    @Test
    void constructor_withData_failsWithWrongRowsSize() {
        final List<List<Integer>> boardData = new ArrayList<>();

        assertThatThrownBy(() -> new GameBoard(boardData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("boardData must have 6 rows");
    }

    @Test
    void constructor_withData_failsWithWrongColumnsSize() {
        final List<List<Integer>> boardData = new ArrayList<>();
        boardData.add(new ArrayList<>());
        boardData.add(new ArrayList<>());
        boardData.add(new ArrayList<>());
        boardData.add(new ArrayList<>());
        boardData.add(new ArrayList<>());
        boardData.add(new ArrayList<>());

        assertThatThrownBy(() -> new GameBoard(boardData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("boardData must have 7 columns");
    }


    public static GameBoard gameBoard(final int playerDiscCount) {
        return gameBoard(playerDiscCount, -1);
    }

    public static GameBoard gameBoard(final int playerDiscCount, final int discNumber) {
        final var gameBoard = new GameBoard();
        IntStream.range(0, playerDiscCount)
                .forEach(count -> gameBoard.addDisc(count % 7, discNumber));
        return gameBoard;
    }


    public static GameBoard fullGameBoard() {
        return gameBoard(GameBoard.ROW_COUNT * COLUMN_COUNT);
    }

}