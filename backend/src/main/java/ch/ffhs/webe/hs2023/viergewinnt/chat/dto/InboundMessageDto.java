package ch.ffhs.webe.hs2023.viergewinnt.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InboundMessageDto {
    private String text;
    private Integer receiverId;
    private int senderId; // todo: remove when auth is implemented
}
