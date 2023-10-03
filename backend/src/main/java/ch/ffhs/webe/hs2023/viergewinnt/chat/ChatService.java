package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.chat.repository.MessageRepository;
import ch.ffhs.webe.hs2023.viergewinnt.player.PlayerService;
import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatService {
    private final MessageRepository messageRepository;
    private final PlayerService playerService;

    @Autowired
    public ChatService(final MessageRepository messageRepository, final PlayerService playerService) {
        this.messageRepository = messageRepository;
        this.playerService = playerService;
    }

    Message storePublicMessage(final InboundMessageDto inboundMessageDto, final Player sender) {
        final var message = Message.builder()
                .text(inboundMessageDto.getText())
                .messageType(MessageType.PUBLIC)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();
        return this.messageRepository.save(message);
    }

    Message storePrivateMessage(final InboundMessageDto inboundMessageDto, final Player sender) {
        final var receiver = this.playerService.getPlayerById(inboundMessageDto.getReceiverId());

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
