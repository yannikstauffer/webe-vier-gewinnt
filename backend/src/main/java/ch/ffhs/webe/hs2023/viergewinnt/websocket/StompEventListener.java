package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.chat.ChatService;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.ChatsDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.SessionService;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserUpdateDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.UserUpdateType;
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

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
public class StompEventListener implements ApplicationListener<SessionConnectEvent> {
    private final UserService userService;
    private final ChatService chatService;
    private final SessionService sessionService;
    private final StompMessageService stompMessageService;


    @Autowired
    public StompEventListener(final StompMessageService messageService, final UserService userService, final SessionService sessionService, final ChatService chatService) {
        this.stompMessageService = messageService;
        this.userService = userService;
        this.sessionService = sessionService;
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
        final var sessionId = this.getSessionId(event);
        final var currentUser = this.getUser(event);

        log.info("[Connected] {}", currentUser.getEmail());
        this.sessionService.addSession(currentUser, sessionId);
        log.debug("SessionId {} added for user {}", sessionId, currentUser.getEmail());

        this.publishUserUpdate(currentUser, UserUpdateType.ONLINE);
    }

    @EventListener
    public void onSocketDisconnected(final SessionDisconnectEvent event) {
        final var currentUser = this.getUser(event);
        final var sessionId = this.getSessionId(event);

        log.info("[Disconnected] {}", currentUser.getEmail());
        this.sessionService.removeSession(currentUser, sessionId);
        log.debug("Session {} removed for user {}", sessionId, currentUser.getEmail());

        if (currentUser.getSessions().isEmpty()) {
            log.debug("User {} no longer online. Sending update to all subscribers.", currentUser.getEmail());
            this.publishUserUpdate(currentUser, UserUpdateType.OFFLINE);
        }
    }

    @EventListener
    public void onTopicSubscribe(final SessionSubscribeEvent event) {
        final var subscription = event.getMessage().getHeaders().get("simpDestination");
        final var currentUser = this.getUser(event);

        if (Topics.LOBBY_CHAT.equals(subscription)) {
            this.publishAllUsersTo(currentUser);
        } else if (Topics.USERS.equals(subscription)) {
            this.publishAllChatsTo(currentUser);
        }
    }

    private void publishAllChatsTo(final User recipient) {
        final var publicMessages = this.chatService.getPublicMessages(LocalDateTime.now().minusMinutes(10));
        final var privateMessages = this.chatService.getPrivateMessages(recipient);
        final var chats = ChatsDto.of(privateMessages, publicMessages);
        this.stompMessageService.sendToUser(Queues.CHATS, recipient, chats);
    }

    private void publishAllUsersTo(final User recipient) {
        final var users = this.userService.getAllWithSession();
        this.stompMessageService.sendToUser(Queues.USERS, recipient, UserDto.of(users));
    }

    private void publishUserUpdate(final User user, final UserUpdateType userUpdateType) {
        this.stompMessageService.send(Topics.USERS, UserUpdateDto.of(user, userUpdateType));
    }

    private User getUser(final AbstractSubProtocolEvent event) {
        final var userEmail = this.getUserName(event);
        return this.userService.getUserByEmail(userEmail);
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