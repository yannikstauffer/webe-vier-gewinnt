package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.chat.repository.MessageRepository;
import ch.ffhs.webe.hs2023.viergewinnt.chat.values.MessageType;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {
    @Mock
    MessageRepository messageRepository;

    @Mock
    UserService userService;

    @InjectMocks
    ChatServiceImpl chatServiceImpl;

    @Test
    void storePublicMessage() {
        // arrange
        final var sender = User.builder().id(1).build();
        final var inboundMessageDto = InboundMessageDto.builder()
                .text("foo")
                .build();

        when(this.messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // act
        final var actualMessage = this.chatServiceImpl.storePublicMessage(inboundMessageDto, sender);

        // assert
        verify(this.messageRepository, times(1)).save(any());
        assertThat(actualMessage.getMessageType()).isEqualTo(MessageType.PUBLIC);
        assertThat(actualMessage.getSender()).isEqualTo(sender);
        assertThat(actualMessage.getReceiver()).isEmpty();
        assertThat(actualMessage.getText()).isEqualTo(inboundMessageDto.getText());
    }

    @Test
    void storePrivateMessage() {
        // arrange
        final var sender = User.builder().id(1).build();
        final var receiver = User.builder().id(100).build();
        final var inboundMessageDto = InboundMessageDto.builder()
                .text("foo")
                .receiverId(receiver.getId())
                .build();

        when(this.userService.getUserById(inboundMessageDto.getReceiverId())).thenReturn(receiver);
        when(this.messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // act
        final var actualMessage = this.chatServiceImpl.storePrivateMessage(inboundMessageDto, sender);

        // assert
        verify(this.messageRepository, times(1)).save(any());
        assertThat(actualMessage.getMessageType()).isEqualTo(MessageType.PRIVATE);
        assertThat(actualMessage.getSender()).isEqualTo(sender);
        assertThat(actualMessage.getReceiver()).contains(receiver);
        assertThat(actualMessage.getText()).isEqualTo(inboundMessageDto.getText());
    }

    @Test
    void getPrivateMessages() {
        // arrange
        final var user = User.builder().id(1).build();
        final var expectedMessage = Message.builder().build();

        when(this.messageRepository.findPrivateBy(user)).thenReturn(Collections.singletonList(expectedMessage));

        // act
        final var actualMessages = this.chatServiceImpl.getPrivateMessages(user);

        // assert
        verify(this.messageRepository, times(1)).findPrivateBy(user);
        assertThat(actualMessages).containsExactly(expectedMessage);
    }

    @Test
    void getPublicMessages() {
        // arrange
        final var time = java.time.LocalDateTime.now();
        final var expectedMessage = Message.builder().build();

        when(this.messageRepository.findPublicBy(time)).thenReturn(Collections.singletonList(expectedMessage));

        // act
        final var actualMessages = this.chatServiceImpl.getPublicMessages(time);

        // assert
        verify(this.messageRepository, times(1)).findPublicBy(time);
        assertThat(actualMessages).containsExactly(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("validate_faultyMessagesArgs")
    void validate_faultyMessages(final InboundMessageDto inboundMessageDto, final MessageType messageType, final Class<? extends Exception> exceptionClass, final String errorMessage) {
        // arrange
        // act + assert
        assertThatThrownBy(() -> this.chatServiceImpl.validate(inboundMessageDto, messageType))
                .isInstanceOf(exceptionClass)
                .hasMessageContaining(errorMessage);

    }

    static Stream<Arguments> validate_faultyMessagesArgs() {
        return Stream.of(
                Arguments.of(null, MessageType.PUBLIC, IllegalArgumentException.class, "InboundMessageDto is null"),
                Arguments.of(InboundMessageDto.builder().build(), MessageType.PUBLIC,
                        VierGewinntException.class, "Message text is empty"),
                Arguments.of(InboundMessageDto.builder().text("").build(), MessageType.PUBLIC,
                        VierGewinntException.class, "Message text is empty"),
                Arguments.of(InboundMessageDto.builder().text("foo").receiverId(1).build(), MessageType.PUBLIC,
                        VierGewinntException.class, "Receiver should not be set on public message"),
                Arguments.of(InboundMessageDto.builder().text("foo").build(), MessageType.PRIVATE,
                        VierGewinntException.class, "Receiver not set"),
                Arguments.of(InboundMessageDto.builder().build(), MessageType.PRIVATE,
                        VierGewinntException.class, "Message text is empty"),
                Arguments.of(InboundMessageDto.builder().text("").build(), MessageType.PRIVATE,
                        VierGewinntException.class, "Message text is empty")
        );
    }

    @ParameterizedTest
    @MethodSource("validateArgs")
    void validate(final InboundMessageDto inboundMessageDto, final MessageType messageType) {
        // arrange
        // act + assert
        assertDoesNotThrow(() -> this.chatServiceImpl.validate(inboundMessageDto, messageType));

    }

    static Stream<Arguments> validateArgs() {
        return Stream.of(
                Arguments.of(InboundMessageDto.builder().text("foo").build(), MessageType.PUBLIC),
                Arguments.of(InboundMessageDto.builder().text("foo").receiverId(1).build(), MessageType.PRIVATE)
        );
    }

}