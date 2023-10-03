package ch.ffhs.webe.hs2023.viergewinnt.player.dto;

import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerDto {
    private int playerId;

    public static PlayerDto of(final Player player) {
        return PlayerDto.builder()
                .playerId(player.getId())
                .build();
    }

}
