package ch.ffhs.webe.hs2023.viergewinnt.game;

import java.util.stream.IntStream;

public class GameBoardTest {
    public static GameBoard gameBoard(final int playerDiscCount) {
        final var gameBoard = new GameBoard();
        IntStream.range(0, playerDiscCount)
                .forEach(count -> gameBoard.addDisc(count % 7, count + 1));
        return gameBoard;
    }

}