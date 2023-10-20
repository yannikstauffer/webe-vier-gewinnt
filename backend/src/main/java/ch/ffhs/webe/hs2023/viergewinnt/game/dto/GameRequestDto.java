package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.MessageUserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRequestDto {
    private GameDto game;
    private MessageUserDto userOne;
    private MessageUserDto userTwo;
}

