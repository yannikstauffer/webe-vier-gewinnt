package ch.ffhs.webe.hs2023.viergewinnt.chat.dto;

import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ChatsDto {
    private final List<OutboundMessageDto> privateMessages;
    private final List<OutboundMessageDto> lobbyMessages;

    public static ChatsDto of(final List<Message> privateMessages, final List<Message> publicMessages) {
        return ChatsDto.builder()
                .privateMessages(OutboundMessageDto.of(privateMessages))
                .lobbyMessages(OutboundMessageDto.of(publicMessages))
                .build();
    }
}
