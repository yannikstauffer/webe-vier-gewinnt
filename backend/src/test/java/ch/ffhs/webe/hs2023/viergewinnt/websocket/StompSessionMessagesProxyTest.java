package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.chat.ChatService;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.ChatsDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.OutboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserUpdateDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.UserUpdateType;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.game;
import static ch.ffhs.webe.hs2023.viergewinnt.user.model.UserTest.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StompSessionMessagesProxyTest {

    @Mock
    UserService userService;

    @Mock
    ChatService chatService;

    @Mock
    GameService gameService;

    @Mock
    StompMessageService stompMessageService;

    @InjectMocks
    StompSessionMessagesProxy stompSessionMessagesProxy;

    @Test
    void publishAllChatsTo() {
        // arrange
        final var recipient = user(1);
        final var privateMessage = this.message(100, MessageType.PRIVATE);
        final var publicMessage = this.message(200, MessageType.PUBLIC);
        when(this.chatService.getPublicMessages(any())).thenReturn(List.of(publicMessage));
        when(this.chatService.getPrivateMessages(recipient)).thenReturn(List.of(privateMessage));
        final ArgumentCaptor<ChatsDto> chatsDtoArgumentCaptor = ArgumentCaptor.forClass(ChatsDto.class);

        // act
        this.stompSessionMessagesProxy.publishAllChatsTo(recipient);

        // assert
        verify(this.chatService).getPublicMessages(any());
        verify(this.chatService).getPrivateMessages(recipient);
        verify(this.stompMessageService).sendToUser(eq(Queues.CHATS), eq(recipient), chatsDtoArgumentCaptor.capture());
        assertThat(chatsDtoArgumentCaptor.getValue().getLobbyMessages()).contains(OutboundMessageDto.of(publicMessage));
        assertThat(chatsDtoArgumentCaptor.getValue().getPrivateMessages()).contains(OutboundMessageDto.of(privateMessage));
    }

    @Test
    void publishAllUsersTo() {
        // arrange
        final var recipient = user(1);
        final var users = List.of(user(100));
        when(this.userService.getAllWithSession()).thenReturn(users);

        // act
        this.stompSessionMessagesProxy.publishAllUsersTo(recipient);

        // assert
        verify(this.userService).getAllWithSession();
        verify(this.stompMessageService).sendToUser(Queues.USERS, recipient, UserDto.of(users));
    }

    @Test
    void publishAllGamesTo() {
        // arrange
        final var recipient = user(1);
        final var games = List.of(game(100), game(101));
        when(this.gameService.getAllGames()).thenReturn(games);

        // act
        this.stompSessionMessagesProxy.publishAllGamesTo(recipient);

        // assert
        verify(this.stompMessageService).sendToUser(Queues.GAMES, recipient, GameDto.of(games));
    }

    @Test
    void publishUserUpdate() {
        // arrange
        final var user = user(1);
        final var userUpdateType = UserUpdateType.ONLINE;

        // act
        this.stompSessionMessagesProxy.publishUserUpdate(user, userUpdateType);

        // assert
        verify(this.stompMessageService).send(Topics.USERS, UserUpdateDto.of(user, userUpdateType));
    }

    @Test
    void publishGameUpdates() {
        // arrange
        final var game1 = game(1);
        final var game2 = game(2);
        final var games = List.of(game1, game2);
        final var spyStompSessionMessagesProxy = spy(this.stompSessionMessagesProxy);

        // act
        spyStompSessionMessagesProxy.publishGameUpdates(games);

        // assert
        verify(spyStompSessionMessagesProxy).publishGameUpdate(game1.getUserOne(), game1);
        verify(spyStompSessionMessagesProxy).publishGameUpdate(game1.getUserTwo(), game1);
        verify(spyStompSessionMessagesProxy).publishGameUpdate(game2.getUserOne(), game2);
        verify(spyStompSessionMessagesProxy).publishGameUpdate(game2.getUserTwo(), game2);
        verify(this.stompMessageService).send(Topics.LOBBY_GAMES, GameDto.of(game1));
        verify(this.stompMessageService).send(Topics.LOBBY_GAMES, GameDto.of(game2));
    }

    @Test
    void publishGameUpdate() {
        // arrange
        final var game1 = game(1);
        final var user1 = game1.getUserOne();

        // act
        this.stompSessionMessagesProxy.publishGameUpdate(user1, game1);

        // assert
        verify(this.stompMessageService).sendToUser(Queues.GAME, user1, GameStateDto.of(game1));
    }

    Message message(final int id, final MessageType messageType) {
        return Message.builder()
                .id(id)
                .messageType(messageType)
                .sender(user(1))
                .receiver(user(100))
                .text("foo")
                .build();
    }

}