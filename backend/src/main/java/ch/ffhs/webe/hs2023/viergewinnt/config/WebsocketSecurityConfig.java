package ch.ffhs.webe.hs2023.viergewinnt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebsocketSecurityConfig {
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(final MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages.nullDestMatcher().authenticated()
                .simpDestMatchers("/4gewinnt/**").authenticated()
                .simpSubscribeDestMatchers("/user/queue/error").permitAll()
                .simpSubscribeDestMatchers("/user/queue/**", "/topic/lobby/**").authenticated()
                .anyMessage().denyAll();

        return messages.build();
    }
}