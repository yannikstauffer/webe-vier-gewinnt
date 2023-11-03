package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.chat.ChatService;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.ChatsDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.OutboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserUpdateDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.UserUpdateType;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class StompSessionMessagesProxyTest {

    @MockBean
    UserService userService;

    @MockBean
    ChatService chatService;

    @MockBean
    StompMessageService stompMessageService;

    @Autowired
    StompSessionMessagesProxy stompSessionMessagesProxy;

    @Test
    void publishAllChatsTo() {
        // arrange
        final var recipient = this.user(1);
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
        final var recipient = this.user(1);
        final var users = List.of(this.user(100));
        when(this.userService.getAllWithSession()).thenReturn(users);

        // act
        this.stompSessionMessagesProxy.publishAllUsersTo(recipient);

        // assert
        verify(this.userService).getAllWithSession();
        verify(this.stompMessageService).sendToUser(Queues.USERS, recipient, UserDto.of(users));
    }

    @Test
    void publishUserUpdate() {
        // arrange
        final var user = this.user(1);
        final var userUpdateType = UserUpdateType.ONLINE;

        // act
        this.stompSessionMessagesProxy.publishUserUpdate(user, userUpdateType);

        // assert
        verify(this.stompMessageService).send(Topics.USERS, UserUpdateDto.of(user, userUpdateType));
    }

    Message message(final int id, final MessageType messageType) {
        return Message.builder()
                .id(id)
                .messageType(messageType)
                .sender(this.user(1))
                .receiver(this.user(100))
                .text("foo")
                .build();
    }

    User user(final int id) {
        return User.builder()
                .id(id)
                .firstName("foo")
                .lastName("bar")
                .build();
    }
}