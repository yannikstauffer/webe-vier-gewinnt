package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.MessageUserDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameDto {
    private int id;
    private MessageUserDto playerOne;
    private MessageUserDto playerTwo;

    public static GameDto of(final Game game) {
        return GameDto.builder()
                .id(game.getId())
                .playerOne(MessageUserDto.of(game.getUserOne()))
                .playerTwo(MessageUserDto.of(game.getUserTwo()))
                .build();
    }
}
