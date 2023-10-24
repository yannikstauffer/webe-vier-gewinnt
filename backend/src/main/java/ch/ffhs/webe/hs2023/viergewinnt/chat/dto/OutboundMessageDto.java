package ch.ffhs.webe.hs2023.viergewinnt.chat.dto;

import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OutboundMessageDto {
    private int id;
    private String text;
    private MessageType messageType;
    private LocalDateTime sentAt;

    private UserDto sender;
    private UserDto receiver;

    public static OutboundMessageDto of(final Message message) {
        final var builder = OutboundMessageDto.builder()
                .id(message.getId())
                .text(message.getText())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt())
                .sender(UserDto.of(message.getSender()));

        message.getReceiver().ifPresent(user -> builder.receiver(UserDto.of(user)));

        return builder.build();
    }
}
