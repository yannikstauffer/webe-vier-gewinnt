package ch.ffhs.webe.hs2023.viergewinnt.chat.dto;

import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
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

    private MessageUserDto sender;
    private MessageUserDto receiver;

    public static OutboundMessageDto of(final Message message) {
        final var builder = OutboundMessageDto.builder()
                .id(message.getId())
                .text(message.getText())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt())
                .sender(MessageUserDto.of(message.getSender()));

        message.getReceiver().ifPresent(user -> builder.receiver(MessageUserDto.of(user)));

        return builder.build();
    }
}
