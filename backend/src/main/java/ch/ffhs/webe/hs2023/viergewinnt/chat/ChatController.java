package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.OutboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.player.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class ChatController {
    private final ChatService chatService;
    private final PlayerService playerService; //will be used when auth is implemented

    @Autowired
    public ChatController(final ChatService chatService,
                          final PlayerService playerService) {
        this.chatService = chatService;
        this.playerService = playerService;
    }

    @MessageMapping("/message")
    @SendTo("/lobby/chat")
    public OutboundMessageDto receivePublicMessage(@Payload final InboundMessageDto message, final Principal user) {
        // todo get user from principal instead of temporary senderid on message
        final var sender = this.playerService.getPlayerById(message.getSenderId());

        final var stored = this.chatService.storePublicMessage(message, sender);
        return OutboundMessageDto.of(stored);
    }


    @MessageMapping("/private-message")
    @SendToUser("/queue/chat")
    public OutboundMessageDto receivePrivateMessage(@Payload final InboundMessageDto message) {
        // todo get user from principal instead of temporary senderid on message
        final var sender = this.playerService.getPlayerById(message.getSenderId());

        final var stored = this.chatService.storePrivateMessage(message, sender);
        return OutboundMessageDto.of(stored);
    }
}
