package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResponseDto {
    private GameDto game;
    private UserDto userOne;
    private UserDto userTwo;

    public static GameResponseDto of(final Game game) {
        final GameDto gameDto = GameDto.builder()
                .id(game.getId())
                .playerOne(UserDto.of(game.getUserOne()))
                .playerTwo(game.getUserTwo() != null ? UserDto.of(game.getUserTwo()) : null)
                .build();

        final UserDto userOne = UserDto.of(game.getUserOne());
        final UserDto userTwo = game.getUserTwo() != null ? UserDto.of(game.getUserTwo()) : null;

        return GameResponseDto.builder()
                .game(gameDto)
                .userOne(userOne)
                .userTwo(userTwo)
                .build();
    }
}




