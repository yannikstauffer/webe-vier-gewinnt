package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameStateDto {
    private Integer gameId;
    private List<List<Integer>> board;
    private Integer nextMove;
    private GameLevel gameLevel;
    private GameState gameState;
    private UserDto userOne;
    private UserState userOneState;
    private UserDto userTwo;
    private UserState userTwoState;

    public static GameStateDto of(final Game game) {
        final var builder = GameStateDto.builder()
                .gameId(game.getId())
                .gameState(game.getGameState())
                .board(game.getBoard().asListObject())
                .nextMove(game.getNextMove())
                .gameLevel(game.getGameLevel());

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
}
