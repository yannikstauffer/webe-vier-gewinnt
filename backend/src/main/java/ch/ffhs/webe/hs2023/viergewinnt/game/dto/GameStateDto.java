package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class GameStateDto {
    private Integer gameId;
    private ArrayList<ArrayList<Integer>> board;
    private Integer nextMove; //todo: implementieren
    private GameState gameState;
    private String message;

    public static GameStateDto of(final Game game, final String message) {
        final var builder =
                GameStateDto.builder()
                        .gameId(game.getId())
                        .board(game.getBoard())
                        .gameState(game.getStatus())
                        .message(message);


        return builder.build();
    }
}
