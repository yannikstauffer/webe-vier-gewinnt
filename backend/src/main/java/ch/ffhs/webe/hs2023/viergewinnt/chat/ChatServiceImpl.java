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
import java.util.List;

@Component
class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Autowired
    public ChatServiceImpl(final MessageRepository messageRepository, final UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Override
    public Message storePublicMessage(final InboundMessageDto inboundMessageDto, final User sender) {
        final var message = Message.builder()
                .text(inboundMessageDto.getText())
                .messageType(MessageType.PUBLIC)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();
        return this.messageRepository.save(message);
    }

    @Override
    public Message storePrivateMessage(final InboundMessageDto inboundMessageDto, final User sender) {
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

    @Override
    public List<Message> getPrivateMessages(final User user) {
        return this.messageRepository.findPrivateBy(user);
    }

    @Override
    public List<Message> getPublicMessages() {
        return this.messageRepository.findPublicBy(LocalDateTime.now().minusMinutes(10));
    }
}
