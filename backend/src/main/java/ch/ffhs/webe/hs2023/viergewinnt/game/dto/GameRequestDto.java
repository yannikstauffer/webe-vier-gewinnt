package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRequestDto {
    private Integer gameId;
    private String message;
}

