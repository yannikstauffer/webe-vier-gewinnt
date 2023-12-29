package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.OutboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.MessageSources;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;
    private final StompMessageService messageService;

    @Autowired
    public ChatController(final ChatService chatService,
                          final UserService userService,
                          final StompMessageService messageService) {
        this.chatService = chatService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @MessageMapping(MessageSources.MESSAGE)
    @SendTo(Topics.LOBBY_CHAT)
    public OutboundMessageDto receivePublicMessage(@Payload final InboundMessageDto message, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());

        final var stored = this.chatService.storePublicMessage(message, sender);
        return OutboundMessageDto.of(stored);
    }

    @MessageMapping(MessageSources.PRIVATE_MESSAGE)
    public void receivePrivateMessage(@Payload final InboundMessageDto message, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());

        final var stored = this.chatService.storePrivateMessage(message, sender);
        final var receiver = stored.getReceiver()
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.USER_NOT_FOUND,
                        "Message receiver for message with id " + stored.getId() + " not found"));

        this.messageService.sendToUser(Queues.CHAT, receiver, OutboundMessageDto.of(stored));
        this.messageService.sendToUser(Queues.CHAT, sender, OutboundMessageDto.of(stored));
    }

}
