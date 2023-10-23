package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.MessageUserDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResponseDto {
    private GameDto game;
    private MessageUserDto userOne;
    private MessageUserDto userTwo;

    public static GameResponseDto of(final Game game) {
        final GameDto gameDto = GameDto.builder()
                .id(game.getId())
                .playerOne(MessageUserDto.of(game.getUserOne()))
                .playerTwo(game.getUserTwo() != null ? MessageUserDto.of(game.getUserTwo()) : null)
                .build();

        final MessageUserDto userOne = MessageUserDto.of(game.getUserOne());
        final MessageUserDto userTwo = game.getUserTwo() != null ? MessageUserDto.of(game.getUserTwo()) : null;

        return GameResponseDto.builder()
                .game(gameDto)
                .userOne(userOne)
                .userTwo(userTwo)
                .build();
    }
}




