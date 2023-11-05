package ch.ffhs.webe.hs2023.viergewinnt.game;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GameBoard {
    private static final int CONNECT = 4;
    private ArrayList<ArrayList<Integer>> board = initializeBoard();

    private ArrayList<ArrayList<Integer>> initializeBoard() {
        ArrayList<ArrayList<Integer>> board = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            ArrayList<Integer> newRow = new ArrayList<>();
            for (int col = 0; col < 7; col++) {
                newRow.add(0);
            }
            board.add(newRow);
        }
        return board;
    }

    public boolean updateBoardColumn(int column, int playerId) {
        for (int row = this.board.size() - 1; row >= 0; row--) {
            if (this.board.get(row).get(column) == 0) {
                this.board.get(row).set(column, playerId);
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        for (ArrayList<Integer> column : this.board) {
            for (int cell : column) {
                if (cell == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkWinner(int playerId) {
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.get(row).size(); col++) {
                if (checkLine(playerId, row, col, 1, 0) ||          // Horizontal
                        checkLine(playerId, row, col, 0, 1) ||      // Vertikal
                        checkLine(playerId, row, col, 1, 1) ||      // Diagonal unten
                        checkLine(playerId, row, col, 1, -1)) {     // Diagonal oben
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkLine(int playerId, int startRow, int startCol, int deltaRow, int deltaCol) {
        int endRow = startRow + (CONNECT - 1) * deltaRow;
        int endCol = startCol + (CONNECT - 1) * deltaCol;

        // Ausserhalb Spielbrett?
        if (endRow < 0 || endRow >= board.size() || endCol < 0 || endCol >= board.get(0).size()) {
            return false;
        }

        for (int i = 0; i < CONNECT; i++) {
            if (board.get(startRow + deltaRow * i).get(startCol + deltaCol * i) != playerId) {
                return false;
            }
        }
        return true;
    }
}
