package ch.ffhs.webe.hs2023.viergewinnt.game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameActionDto {
    private Integer gameId;
    private Integer column;
    private String message;
}