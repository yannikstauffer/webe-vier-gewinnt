package ch.ffhs.webe.hs2023.viergewinnt.config;

import ch.ffhs.webe.hs2023.viergewinnt.game.level.TimedActionExecutorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class GameConfig {
    static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Bean
    public TimedActionExecutorProvider timedActionExecutorProvider() {
        return new TimedActionExecutorProvider(scheduledExecutorService);
    }
}
