package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameBoardState;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
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
    private GameBoardState gameBoardState;
    private GameLevel gameLevel;
    private UserDto userOne;
    private UserDto userTwo;
    private String message;

    public static GameStateDto of(final Game game) {
        final var builder =
                GameStateDto.builder()
                        .gameId(game.getId())
                        .board(game.getBoard().asListObject())
                        .nextMove(game.getNextMove())
                        .gameBoardState(game.getGameBoardState())
                        .gameLevel(game.getGameLevel())
                        .message(game.getStatusMessage());

        if (game.getUserOne() != null) {
            builder.userOne(UserDto.of(game.getUserOne()));
        }

        if (game.getUserTwo() != null) {
            builder.userTwo(UserDto.of(game.getUserTwo()));
        }

        return builder.build();
    }
}
