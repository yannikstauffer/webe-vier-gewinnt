package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRequestDto {
    private int gameId;
    private int userId;
    private GameState gameState;


}
