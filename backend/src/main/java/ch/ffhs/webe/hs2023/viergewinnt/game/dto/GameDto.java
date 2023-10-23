package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
@Builder
public class GameDto {
    private int id;
    private UserDto userOne;
    private UserDto userTwo;

    public static GameDto of(final Game game) {
        final var builder =
                GameDto.builder()
                        .id(game.getId())
                        .userOne(UserDto.of(game.getUserOne()));
        if (game.getUserTwo() != null) {
            builder.userTwo(UserDto.of(game.getUserTwo()));
        }
        return builder.build();
    }

    public static List<GameDto> of(final Collection<Game> games) {
        return games.stream().map(GameDto::of).toList();
    }
}
