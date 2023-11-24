package ch.ffhs.webe.hs2023.viergewinnt.game;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class GameBoard {
    static final int CONNECT = 4;
    static final int EMPTY_CELL = 0;
    static final int ROW_COUNT = 6;
    static final int COLUMN_COUNT = 7;
    public static final int ANONYMOUS_DISC_NUMBER = -5;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.NONE)
    private final List<List<Integer>> board = new ArrayList<>();

    public GameBoard() {
        this.initializeBoard();
    }

    public GameBoard(final List<List<Integer>> boardData) {
        validateBoardData(boardData);
        for (final List<Integer> boardDataRow : boardData) {
            this.board.add(new ArrayList<>(boardDataRow));
        }
    }

    public List<List<Integer>> asListObject() {
        final List<List<Integer>> list = new ArrayList<>();
        for (final List<Integer> boardDataRow : this.board) {
            list.add(new ArrayList<>(boardDataRow));
        }
        return list;
    }

    public boolean isColumnFull(final int column) {
        this.validateColumnId(column);
        for (int row = this.board.size() - 1; row >= 0; row--) {
            if (this.board.get(row).get(column) == 0) {
                return false;
            }
        }
        return true;
    }

    public void addDisc(final int column, final int playerId) {
        this.validateColumnId(column);
        for (int row = this.board.size() - 1; row >= 0; row--) {
            if (this.board.get(row).get(column) == 0) {
                this.board.get(row).set(column, playerId);
                return;
            }
        }
    }

    public int getPlayerDiscCount() {
        int count = 0;
        for (final List<Integer> column : this.board) {
            for (final Integer cell : column) {
                if (cell != EMPTY_CELL && cell != ANONYMOUS_DISC_NUMBER) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean contains(final int id) {
        for (final List<Integer> column : this.board) {
            if (column.contains(id)) return true;
        }
        return false;
    }

    public boolean isFull() {
        return !this.contains(EMPTY_CELL);
    }

    public boolean checkWinner(final int playerId) {
        for (int row = 0; row < this.board.size(); row++) {
            for (int col = 0; col < this.board.get(row).size(); col++) {
                if (this.checkLine(playerId, row, col, 1, 0) ||          // Horizontal
                        this.checkLine(playerId, row, col, 0, 1) ||      // Vertikal
                        this.checkLine(playerId, row, col, 1, 1) ||      // Diagonal unten
                        this.checkLine(playerId, row, col, 1, -1)) {     // Diagonal oben
                    return true;
                }
            }
        }
        return false;
    }

    private void initializeBoard() {
        this.board.clear();

        for (int row = 0; row < ROW_COUNT; row++) {
            final Integer[] column = new Integer[COLUMN_COUNT];
            Arrays.fill(column, EMPTY_CELL);
            this.board.add(new ArrayList<>(Arrays.asList(column)));
        }
    }

    private boolean checkLine(final int playerId, final int startRow, final int startCol, final int deltaRow, final int deltaCol) {
        final int endRow = startRow + (CONNECT - 1) * deltaRow;
        final int endCol = startCol + (CONNECT - 1) * deltaCol;

        // Ausserhalb Spielbrett?
        if (endRow < 0 || endRow >= this.board.size() || endCol < 0 || endCol >= this.board.get(0).size()) {
            return false;
        }

        for (int i = 0; i < CONNECT; i++) {
            if (this.board.get(startRow + deltaRow * i).get(startCol + deltaCol * i) != playerId) {
                return false;
            }
        }
        return true;
    }

    private void validateColumnId(final int columnId) {
        if (columnId < 0 || columnId >= COLUMN_COUNT) {
            throw new IllegalArgumentException("ColumnId must be between 0 and " + COLUMN_COUNT);
        }
    }

    private static void validateBoardData(final List<List<Integer>> boardData) {
        if (boardData.size() != ROW_COUNT) {
            throw new IllegalArgumentException("boardData must have " + ROW_COUNT + " rows");
        }
        for (final List<Integer> boardDataRow : boardData) {
            if (boardDataRow.size() != COLUMN_COUNT) {
                throw new IllegalArgumentException("boardData must have " + COLUMN_COUNT + " columns");
            }
        }
    }
}
