package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.SessionService;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.Session;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.UserUpdateType;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;

import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.game;
import static ch.ffhs.webe.hs2023.viergewinnt.user.model.UserTest.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.DESTINATION_HEADER;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.SESSION_ID_HEADER;
import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.USER_HEADER;

@ExtendWith(MockitoExtension.class)
class StompSessionHandlerTest {
    @Mock
    UserService userService;

    @Mock
    StompSessionMessagesProxy stompSessionMessagesProxy;

    @Mock
    SessionService sessionService;

    @Mock
    GameService gameService;

    @InjectMocks
    StompSessionHandler stompSessionHandler;

    @Test
    void onSocketConnected() {
        // arrange
        final var sender = user(1);
        final var sessionId = "1";
        final var message = this.message(sender, sessionId);
        final var event = new SessionConnectedEvent("test-connected", message);

        // act
        this.stompSessionHandler.onSocketConnected(event);

        // assert
        verify(this.sessionService).addSession(sender, sessionId);
        verify(this.stompSessionMessagesProxy).publishUserUpdate(sender, UserUpdateType.ONLINE);
    }

    @Test
    void onSocketDisconnected_doesNotSendMessages_ifAdditionalSessionsExist() {
        // arrange
        final var sender = user(2, Collections.singletonList(mock(Session.class)));
        final var sessionId = "200";
        final var message = this.message(sender, sessionId);
        final var event = new SessionDisconnectEvent("test-disconnected", message, sessionId, CloseStatus.NORMAL);

        // act
        final var future = this.stompSessionHandler.onSocketDisconnected(event);

        // assert
        future.join();
        verify(this.sessionService).removeSession(sender, sessionId);
        verifyNoInteractions(this.stompSessionMessagesProxy, this.gameService);
    }

    @Test
    void onSocketDisconnected_doesSendUpdateMessages_ifNoMoreSessionsExist() {
        // arrange
        final var sender = user(3, Collections.emptyList());
        final var sessionId = "3";
        final var message = this.message(sender, sessionId);
        final var event = new SessionDisconnectEvent("test-disconnected", message, sessionId, CloseStatus.NORMAL);
        final var gamesUserWasIn = new ArrayList<Game>();
        when(this.gameService.getGamesForUser(sender.getId())).thenReturn(gamesUserWasIn);

        // act
        final var future = this.stompSessionHandler.onSocketDisconnected(event);

        // assert
        future.join();
        verify(this.sessionService).removeSession(sender, sessionId);
        verify(this.stompSessionMessagesProxy).publishUserUpdate(sender, UserUpdateType.OFFLINE);
        verify(this.gameService).setUserAsDisconnected(eq(sender), any());
        verify(this.stompSessionMessagesProxy).publishGameUpdates(gamesUserWasIn);
    }

    @Test
    void onTopicSubscribe_publishesAllChats_whenSubscribeToLobbyChatTopic() {
        // arrange
        final var sender = user(4);
        final var sessionId = "4";
        final var message = this.message(sender, sessionId, Topics.LOBBY_CHAT);
        final var event = new SessionSubscribeEvent("test-topic-subscribe-chat", message);

        // act
        this.stompSessionHandler.onTopicSubscribe(event);

        // assert
        verify(this.stompSessionMessagesProxy).publishAllChatsTo(sender);
    }

    @Test
    void onTopicSubscribe_publishesAllUsers_whenSubscribeToLobbyChatTopic() {
        // arrange
        final var sender = user(5);
        final var sessionId = "5";
        final var message = this.message(sender, sessionId, Topics.USERS);
        final var event = new SessionSubscribeEvent("test-topic-subscribe-user", message);

        // act
        this.stompSessionHandler.onTopicSubscribe(event);

        // assert
        verify(this.stompSessionMessagesProxy).publishAllUsersTo(sender);
    }

    @Test
    void onTopicSubscribe_publishesAllGames_whenSubscribeToLobbyGamesTopic() {
        // arrange
        final var sender = user(6);
        final var sessionId = "6";
        final var message = this.message(sender, sessionId, Topics.LOBBY_GAMES);
        final var event = new SessionSubscribeEvent("test-topic-subscribe-games", message);

        // act
        this.stompSessionHandler.onTopicSubscribe(event);

        // assert
        verify(this.stompSessionMessagesProxy).publishAllGamesTo(sender);
    }

    @Test
    void onQueuesSubscribe_publishesCurrentGame_whenSubscribeToGameQueue() {
        // arrange
        final var sender = user(6);
        final var sessionId = "6";
        final var message = this.message(sender, sessionId, "/user" + Queues.GAME);
        final var event = new SessionSubscribeEvent("test-queue-subscribe-game", message);
        final var game = game(2);
        sender.pushCurrentGameId(game.getId());
        when(this.gameService.getGameById(game.getId())).thenReturn(game);

        // act
        this.stompSessionHandler.onQueuesSubscribe(event);

        // assert
        verify(this.stompSessionMessagesProxy).publishGameUpdate(sender, game);
    }

    Message<byte[]> message(final User sender, final String sessionId) {
        return this.message(sender, sessionId, "/topic/test");
    }

    Message<byte[]> message(final User sender, final String sessionId, final String destination) {
        final var payload = "test-payload";
        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(sender.getEmail());
        when(this.userService.getUserByEmail(sender.getEmail())).thenReturn(sender);

        return MessageBuilder.withPayload(payload.getBytes())
                .setHeader(DESTINATION_HEADER, destination)
                .setHeader(SESSION_ID_HEADER, sessionId)
                .setHeader(USER_HEADER, principal)
                .build();
    }
}