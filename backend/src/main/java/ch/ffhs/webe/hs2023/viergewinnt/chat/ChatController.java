package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.OutboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
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
    private final UserService userService;

    @Autowired
    public ChatController(final ChatService chatService,
                          final UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @MessageMapping(MessageSources.MESSAGE)
    @SendTo(Topics.LOBBY_CHAT)
    public OutboundMessageDto receivePublicMessage(@Payload final InboundMessageDto message, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());

        final var stored = this.chatService.storePublicMessage(message, sender);
        return OutboundMessageDto.of(stored);
    }


    @MessageMapping("/private-message")
    @SendToUser("/queue/chat")
    public OutboundMessageDto receivePrivateMessage(@Payload final InboundMessageDto message, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());

        final var stored = this.chatService.storePrivateMessage(message, sender);
        return OutboundMessageDto.of(stored);
    }
}
