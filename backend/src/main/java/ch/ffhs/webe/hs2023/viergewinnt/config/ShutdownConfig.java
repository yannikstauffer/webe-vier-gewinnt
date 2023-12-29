package ch.ffhs.webe.hs2023.viergewinnt.config;

import ch.ffhs.webe.hs2023.viergewinnt.base.SystemMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static ch.ffhs.webe.hs2023.viergewinnt.base.SystemMessageCode.SERVER_IS_SHUTTING_DOWN;

@Slf4j
@Configuration
public class ShutdownConfig {
    private final StompMessageService stompMessageService;

    @Autowired
    public ShutdownConfig(final StompMessageService stompMessageService) {
        this.stompMessageService = stompMessageService;
    }

    public Runnable onShutdown() {
        final var shutdownMessage = SystemMessageDto.of(SERVER_IS_SHUTTING_DOWN);
        return () -> {
            this.stompMessageService.send(Topics.SYSTEM, shutdownMessage);
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                log.error("Interrupted while waiting for shutdown", e);
                Thread.currentThread().interrupt();
            }
        };
    }
}
