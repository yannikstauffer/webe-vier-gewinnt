package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompMessageService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public StompMessageService(final SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void send(final String destination, final Object payload) {
        log.debug("Sending message to destination {}: {}", destination, payload);
        this.simpMessagingTemplate.convertAndSend(destination, payload);
    }

    public void sendToUser(final String destination, final User receiver, final Object payload) {
        final var username = receiver.getEmail();
        log.debug("Sending message to user {} on destination {}: {}", username, destination, payload);

        receiver.getSessions().forEach(session ->
                this.simpMessagingTemplate.convertAndSendToUser(
                        session.getSessionId(),
                        destination,
                        payload
                ));

        this.simpMessagingTemplate.convertAndSendToUser(username, destination, payload);
    }

}
