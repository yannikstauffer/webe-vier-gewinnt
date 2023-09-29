package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

/**
 * Class only for logging purposes
 * based on https://medium.com/@yairharel/websockets-spring-boot-application-cd33c8e90c0a
 **/
@Slf4j
@Component
public class StompEventListener implements ApplicationListener<SessionConnectEvent> {
    @Override
    public void onApplicationEvent(final SessionConnectEvent event) {
        final String userId = this.getUserName(event);
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        log.debug("STOMP-Connect Details [sessionId: {}, userId: {}, command: {}]",
                sha.getSessionId(), userId, sha.getCommand());

    }

    @EventListener
    public void onSocketConnected(final SessionConnectedEvent event) {
        log.info("[Connected] " + this.getUserName(event));
    }

    @EventListener
    public void onSocketDisconnected(final SessionDisconnectEvent event) {
        log.info("[Disconnected] " + this.getUserName(event));
    }

    private Optional<Principal> getOptionalUser(final AbstractSubProtocolEvent event) {
        final StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(event.getMessage());
        return Optional.ofNullable(stompHeaderAccessor.getUser());
    }

    private String getUserName(final AbstractSubProtocolEvent event) {
        return this.getOptionalUser(event)
                .map(Principal::getName)
                .orElse("{unknown}");
    }
}