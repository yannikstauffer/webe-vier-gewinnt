package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.player.dto.PlayerDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameDto {
    private int id;
    private PlayerDto playerOne;
    private PlayerDto playerTwo;

    public static GameDto of(final Game game) {
        return GameDto.builder()
                .id(game.getId())
                .playerOne(PlayerDto.of(game.getPlayerOne()))
                .playerTwo(PlayerDto.of(game.getPlayerTwo()))
                .build();
    }
}
