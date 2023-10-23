package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRequestDto {
    private GameDto game;
    private UserDto userOne;
    private UserDto userTwo;
}

