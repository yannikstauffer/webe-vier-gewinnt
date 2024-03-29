package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.OutboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static ch.ffhs.webe.hs2023.viergewinnt.user.model.UserTest.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @Mock
    ChatService chatService;

    @Mock
    UserService userService;

    @Mock
    StompMessageService messageService;

    @InjectMocks
    ChatController chatController;

    @Test
    void receivePublicMessage() {
        // arrange
        final var principal = mock(Principal.class);
        final var sender = mock(User.class);
        final var inboundMessageDto = mock(InboundMessageDto.class);
        final var expectedMessage = this.publicMessage(sender);

        when(principal.getName()).thenReturn("foo@bar.com");
        when(this.userService.getUserByEmail("foo@bar.com")).thenReturn(sender);
        when(this.chatService.storePublicMessage(inboundMessageDto, sender)).thenReturn(expectedMessage);

        // act
        final var actualMessage = this.chatController.receivePublicMessage(inboundMessageDto, principal);

        // assert
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.getText()).isEqualTo(expectedMessage.getText());
    }

    @Test
    void receivePrivateMessage() {
        // arrange
        final var sender = this.sender(1);
        final var principal = this.principal(sender);
        final var receiver = user(100);
        final var inboundMessageDto = InboundMessageDto.builder().receiverId(100).build();
        final var storedMessage = this.privateMessage(sender, receiver);
        final var expectedOutboundMessage = this.outboundMessageDto(storedMessage);

        when(this.chatService.storePrivateMessage(inboundMessageDto, sender)).thenReturn(storedMessage);

        // act
        this.chatController.receivePrivateMessage(inboundMessageDto, principal);

        // assert
        verify(this.messageService).sendToUser(Queues.CHAT, receiver, expectedOutboundMessage);
        verify(this.messageService).sendToUser(Queues.CHAT, sender, expectedOutboundMessage);
    }

    @Test
    void receivePrivateMessage_throwsVierGewinntException_whenReceiverIsMissingOnMessage() {
        // arrange
        final var sender = this.sender(2);
        final var principal = this.principal(sender);

        final var inboundMessageDto = mock(InboundMessageDto.class);
        final var storedMessage = this.privateMessage(sender, null);

        when(this.chatService.storePrivateMessage(inboundMessageDto, sender)).thenReturn(storedMessage);

        // act + act
        assertThatThrownBy(() -> this.chatController.receivePrivateMessage(inboundMessageDto, principal))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Message receiver for message with id " + storedMessage.getId() + " not found");
    }

    User sender(final int userId) {
        final var sender = user(userId);
        when(this.userService.getUserByEmail(sender.getEmail())).thenReturn(sender);

        return sender;
    }

    Principal principal(final User sender) {
        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(sender.getEmail());
        return principal;
    }

    OutboundMessageDto outboundMessageDto(final Message message) {
        final var builder = OutboundMessageDto.builder()
                .id(message.getId())
                .text(message.getText())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt())
                .sender(UserDto.of(message.getSender()));
        message.getReceiver().ifPresent(user -> builder.receiver(UserDto.of(user)));
        return builder.build();
    }

    Message publicMessage(final User sender) {
        return Message.builder()
                .text("this is a public message")
                .messageType(MessageType.PUBLIC)
                .sender(sender)
                .build();
    }

    Message privateMessage(final User sender, final User receiver) {
        return Message.builder()
                .text("this is a public message")
                .messageType(MessageType.PUBLIC)
                .sender(sender)
                .receiver(receiver)
                .build();
    }
}