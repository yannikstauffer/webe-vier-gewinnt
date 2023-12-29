package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class GameDto {
    private int id;
    private UserDto userOne;
    private UserDto userTwo;
    private UserState userOneState;
    private UserState userTwoState;
    private GameState gameState;

    public static GameDto of(final Game game) {
        final var builder = GameDto.builder()
                .id(game.getId())
                .gameState(game.getGameState());

        if (game.getUserOne() != null) {
            builder.userOne(UserDto.of(game.getUserOne()))
                    .userOneState(game.getUserOneState());
        }

        if (game.getUserTwo() != null) {
            builder.userTwo(UserDto.of(game.getUserTwo()))
                    .userTwoState(game.getUserTwoState());
        }

        return builder.build();
    }

    public static List<GameDto> of(final Collection<Game> games) {
        if (games == null || games.isEmpty()) {
            return new ArrayList<>();
        }

        return games.stream()
                .map(GameDto::of)
                .toList();
    }
}
