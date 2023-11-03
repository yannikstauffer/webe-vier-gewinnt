package ch.ffhs.webe.hs2023.viergewinnt.game;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GameBoard {
    private ArrayList<ArrayList<Integer>> board = initializeBoard();

    private ArrayList<ArrayList<Integer>> initializeBoard() {
        ArrayList<ArrayList<Integer>> board = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ArrayList<Integer> column = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                column.add(0);
            }
            board.add(column);
        }
        return board;
    }

    public boolean updateBoardColumn(int column, int playerId) {
        for (int row = this.board.get(column).size() - 1; row >= 0; row--) {
            if (this.board.get(column).get(row) == 0) {
                this.board.get(column).set(row, playerId);
                break;
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
