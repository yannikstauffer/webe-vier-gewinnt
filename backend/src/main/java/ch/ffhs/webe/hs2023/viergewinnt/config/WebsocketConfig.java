package ch.ffhs.webe.hs2023.viergewinnt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/4gewinnt/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/4gewinnt");
        // Für ../games/all etc. muss zustätzlich /topic/lobby/games eingefügt werden sonst geht es nicht -> allenfalls Bug bei dieser Version von Spring?
        registry.enableSimpleBroker("/topic/lobby/chat", "/topic/lobby/games","/topic/lobby/games/*", "/queue/chat", "/queue/error");
        registry.setUserDestinationPrefix("/user");
    }
}
