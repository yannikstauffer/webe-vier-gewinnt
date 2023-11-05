package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;

import static ch.ffhs.webe.hs2023.viergewinnt.user.UserTestUtils.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StompMessageServiceTest {

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    StompMessageService stompMessageService;

    @Test
    void send() {
        // arrange
        final var destination = "/queue/test";
        final var payload = destination + " payload";

        // act
        this.stompMessageService.send(destination, payload);

        // assert
        verify(this.simpMessagingTemplate).convertAndSend(destination, payload);
    }

    @Test
    void sendToUser() {
        // arrange
        final var destination = "/topic/test";
        final var payload = destination + " payload";
        final var sessions = List.of(this.session("100"), this.session("200"));
        final var receiver = user(1, sessions);
        final var sessionIdCaptor = ArgumentCaptor.forClass(String.class);

        // act
        this.stompMessageService.sendToUser(destination, receiver, payload);

        // assert
        verify(this.simpMessagingTemplate, times(sessions.size() + 1)).convertAndSendToUser(sessionIdCaptor.capture(), eq(destination), eq(payload));
        assertThat(sessionIdCaptor.getAllValues()).containsAll(sessions.stream().map(Session::getSessionId).toList());
        verify(this.simpMessagingTemplate, times(1)).convertAndSendToUser(receiver.getEmail(), destination, payload);
    }

    @Test
    void sendToUser_doesNotSend_whenReceiverHasNoSession() {
        // arrange
        final var destination = "/topic/test";
        final var payload = destination + " payload";
        final var receiver = user(1, Collections.emptyList());

        // act
        this.stompMessageService.sendToUser(destination, receiver, payload);

        // assert
        verifyNoInteractions(this.simpMessagingTemplate);
    }

    Session session(final String sessionId) {
        return Session.builder()
                .sessionId(sessionId)
                .build();
    }
}