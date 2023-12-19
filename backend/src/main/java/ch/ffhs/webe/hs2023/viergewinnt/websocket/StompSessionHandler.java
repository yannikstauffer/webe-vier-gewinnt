package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.SessionService;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class StompSessionHandler implements ApplicationListener<SessionConnectEvent> {

    private final UserService userService;
    private final SessionService sessionService;
    private final GameService gameService;
    private final StompSessionMessagesProxy stompSessionMessagesProxy;

    @Autowired
    public StompSessionHandler(final UserService userService, final SessionService sessionService, final GameService gameService, final StompSessionMessagesProxy stompSessionMessagesProxy) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.gameService = gameService;
        this.stompSessionMessagesProxy = stompSessionMessagesProxy;
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

        this.stompSessionMessagesProxy.publishUserUpdate(currentUser, UserUpdateType.ONLINE);
    }

    @EventListener
    public CompletableFuture<Void> onSocketDisconnected(final SessionDisconnectEvent event) {
        final var currentUser = this.getUser(event);
        final var sessionId = this.getSessionId(event);

        log.info("[Disconnected] {}", currentUser.getEmail());
        this.sessionService.removeSession(currentUser, sessionId);
        log.debug("Session {} removed for user {}", sessionId, currentUser.getEmail());

        if (currentUser.getSessions().isEmpty()) {
            return this.publishUserOfflineEvents(currentUser.getEmail());
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> publishUserOfflineEvents(final String userEmail) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                log.error("Error while waiting for user to reconnect", e);
                Thread.currentThread().interrupt();
            }
            final var user = this.userService.getUserByEmail(userEmail);
            if (!user.getSessions().isEmpty()) {
                return;
            }
            log.debug("User {} no longer online. Sending update to all subscribers.", user.getEmail());
            this.stompSessionMessagesProxy.publishUserUpdate(user, UserUpdateType.OFFLINE);

            final List<Game> gamesUserWasIn = this.gameService.getGamesForUser(user.getId());
            this.gameService.setUserAsDisconnected(user, gamesUserWasIn);
            this.stompSessionMessagesProxy.publishGameUpdates(gamesUserWasIn);
        });
    }

    @EventListener
    public void onTopicSubscribe(final SessionSubscribeEvent event) {
        final var subscription = event.getMessage().getHeaders().get("simpDestination");
        final var currentUser = this.getUser(event);

        if (Topics.LOBBY_CHAT.equals(subscription)) {
            this.stompSessionMessagesProxy.publishAllChatsTo(currentUser);
        } else if (Topics.USERS.equals(subscription)) {
            this.stompSessionMessagesProxy.publishAllUsersTo(currentUser);
        } else if (Topics.LOBBY_GAMES.equals(subscription)) {
            this.stompSessionMessagesProxy.publishAllGamesTo(currentUser);
        }
    }

    @EventListener
    public void onQueuesSubscribe(final SessionSubscribeEvent event) {
        final var subscription = event.getMessage().getHeaders().get("simpDestination");
        final var currentUser = this.getUser(event);

        if (("/user" + Queues.GAME).equals(subscription)) { // Sicherheit, dass das update auch kommt nach dem join
            final var gameId = currentUser.popCurrentGameId();
            if (gameId != null) {
                final var currentGame = this.gameService.getGameById(gameId);
                this.stompSessionMessagesProxy.publishGameUpdate(currentUser, currentGame);
            }
        }
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