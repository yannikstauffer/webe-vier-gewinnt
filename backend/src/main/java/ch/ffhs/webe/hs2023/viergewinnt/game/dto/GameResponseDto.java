package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResponseDto {
    private int gameId;
    private GameState status;
    private String creatorName;

    public static GameResponseDto of(final Game game) {
        return GameResponseDto.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .creatorName(game.getUserOne().getFirstName())
                .build();
    }
}


