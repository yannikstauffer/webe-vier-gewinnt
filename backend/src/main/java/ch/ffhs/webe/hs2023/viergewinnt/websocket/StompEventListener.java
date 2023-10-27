package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.chat.ChatService;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.ChatsDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UsersDto;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Objects;

@Slf4j
@Component
public class StompEventListener implements ApplicationListener<SessionConnectEvent> {
    private final UserService userService;
    private final ChatService chatService;
    private final StompMessageService stompMessageService;


    @Autowired
    public StompEventListener(final StompMessageService messageService, final UserService userService, final ChatService chatService) {
        this.userService = userService;
        this.stompMessageService = messageService;
        this.chatService = chatService;
    }

    @Override
    public void onApplicationEvent(final SessionConnectEvent event) {
        final String userId = this.getUserName(event);
        final var details = this.nullableDetails(event);
        log.debug("STOMP-Connect Details [sessionId: {}, userId: {}, command: {}]",
                details.getSessionId(), userId, details.getCommand());
    }

    @EventListener
    public void onSocketConnected(final SessionConnectedEvent event) {
        final var userEmail = this.getUserName(event);
        final var sessionId = this.getSessionId(event);

        log.info("[Connected] {}", userEmail);
        this.userService.setSessionId(userEmail, sessionId);
        log.debug("SessionId {} added for user {}", sessionId, userEmail);
    }

    @EventListener
    public void onSocketDisconnected(final SessionDisconnectEvent event) {
        final var userEmail = this.getUserName(event);
        final var sessionId = this.getSessionId(event);

        log.info("[Disconnected] {}", userEmail);
        this.userService.removeSessionId(userEmail, sessionId);
        log.debug("SessionId {} removed for user {}", sessionId, userEmail);
    }

    @EventListener
    public void onTopicSubscribe(final SessionSubscribeEvent event) {
        final var subscription = event.getMessage().getHeaders().get("simpDestination");
        if (Topics.LOBBY_CHAT.equals(subscription)) {
            this.onLobbyChatSubscription(event);
        }

    }

    private void onLobbyChatSubscription(final SessionSubscribeEvent event) {
        final var userEmail = this.getUserName(event);
        final var currentUser = this.userService.getUserByEmail(userEmail);
        final var users = this.userService.getAllWithSession();
        this.stompMessageService.send(Topics.USERS, UsersDto.of(currentUser, users));

        final var publicMessages = this.chatService.getPublicMessages();
        final var privateMessages = this.chatService.getPrivateMessages(currentUser);
        final var chats = ChatsDto.of(privateMessages, publicMessages);
        this.stompMessageService.sendToUser(Queues.CHATS, currentUser, chats);
    }


    private String getUserName(final AbstractSubProtocolEvent event) {
        return Objects.requireNonNull(this.nullableDetails(event).getUser(), "User missing on STOMP event")
                .getName();
    }

    private String getSessionId(final AbstractSubProtocolEvent event) {
        return Objects.requireNonNull(this.nullableDetails(event).getSessionId(), "SessionId missing on STOMP event");
    }

    private StompHeaderAccessor nullableDetails(final AbstractSubProtocolEvent event) {
        return StompHeaderAccessor.wrap(event.getMessage());
    }

}