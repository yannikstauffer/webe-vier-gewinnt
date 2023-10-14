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
    private int creatorId;
    private String creatorName;
    private int userTwoId;
    private String userTwoName;

    public static GameResponseDto of(final Game game) {
        if (game.getUserTwo() == null) {
            return GameResponseDto.builder()
                    .gameId(game.getId())
                    .status(game.getStatus())
                    .creatorId(game.getUserOne().getId())
                    .creatorName(game.getUserOne().getFirstName())
                    .build();
        }
        return GameResponseDto.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .creatorId(game.getUserOne().getId())
                .creatorName(game.getUserOne().getFirstName())
                .userTwoId(game.getUserTwo().getId())
                .userTwoName(game.getUserTwo().getFirstName())
                .build();

    }
}


