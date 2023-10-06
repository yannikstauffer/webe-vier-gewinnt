package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.repository.MessageRepository;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatService {
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Autowired
    public ChatService(final MessageRepository messageRepository, final UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    Message storePublicMessage(final InboundMessageDto inboundMessageDto, final User sender) {
        final var message = Message.builder()
                .text(inboundMessageDto.getText())
                .messageType(MessageType.PUBLIC)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();
        return this.messageRepository.save(message);
    }

    Message storePrivateMessage(final InboundMessageDto inboundMessageDto, final User sender) {
        final var receiver = this.userService.getUserById(inboundMessageDto.getReceiverId());

        final var message = Message.builder()
                .text(inboundMessageDto.getText())
                .messageType(MessageType.PRIVATE)
                .sender(sender)
                .receiver(receiver)
                .sentAt(LocalDateTime.now())
                .build();
        return this.messageRepository.save(message);
    }

}
