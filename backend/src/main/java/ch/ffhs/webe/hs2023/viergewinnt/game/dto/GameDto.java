package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameDto {
    private int id;
    private UserDto playerOne;
    private UserDto playerTwo;

    public static GameDto of(final Game game) {
        return GameDto.builder()
                .id(game.getId())
                .playerOne(UserDto.of(game.getUserOne()))
                .playerTwo(UserDto.of(game.getUserTwo()))
                .build();
    }
}
