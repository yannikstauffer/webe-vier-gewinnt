package ch.ffhs.webe.hs2023.viergewinnt.game;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GameBoard {
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
}
