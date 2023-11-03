package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
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
        this.validate(inboundMessageDto, MessageType.PUBLIC);

        final var message = Message.builder()
                .text(inboundMessageDto.getText())
                .messageType(MessageType.PUBLIC)
                .sender(sender)
                .build();
        return this.messageRepository.save(message);
    }

    @Override
    public Message storePrivateMessage(final InboundMessageDto inboundMessageDto, final User sender) {
        this.validate(inboundMessageDto, MessageType.PRIVATE);

        final var receiver = this.userService.getUserById(inboundMessageDto.getReceiverId());

        final var message = Message.builder()
                .text(inboundMessageDto.getText())
                .messageType(MessageType.PRIVATE)
                .sender(sender)
                .receiver(receiver)
                .build();
        return this.messageRepository.save(message);
    }

    @Override
    public List<Message> getPrivateMessages(final User user) {
        return this.messageRepository.findPrivateBy(user);
    }

    @Override
    public List<Message> getPublicMessages(final LocalDateTime since) {
        return this.messageRepository.findPublicBy(since);
    }

    void validate(final InboundMessageDto inboundMessageDto, final MessageType messageType) {
        if (inboundMessageDto == null) {
            throw new IllegalArgumentException("InboundMessageDto is null");
        }
        if (inboundMessageDto.getText() == null || inboundMessageDto.getText().isEmpty()) {
            throw VierGewinntException.of(ErrorCode.MESSAGE_TEXT_EMPTY, "Message text is empty");
        }

        if (messageType == MessageType.PUBLIC && inboundMessageDto.getReceiverId() != null) {
            throw VierGewinntException.of(ErrorCode.RECEIVER_ON_PUBLIC_MESSAGE, "Receiver should not be set on public message");
        }

        if (messageType == MessageType.PRIVATE && inboundMessageDto.getReceiverId() == null) {
            throw VierGewinntException.of(ErrorCode.RECEIVER_NOT_SET, "Receiver not set");
        }
    }
}
