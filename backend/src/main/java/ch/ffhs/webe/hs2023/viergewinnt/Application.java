package ch.ffhs.webe.hs2023.viergewinnt;

import ch.ffhs.webe.hs2023.viergewinnt.config.ShutdownConfig;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.SessionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner clearLeftOverStompSessions(final SessionRepository repository) {
        return args -> repository.deleteAll();
    }

    @Bean
    public CommandLineRunner setPlayerStateAsDisconnected(final GameService gameService) {
        return args -> gameService.setAllConnectedUsersAsDisconnected();
    }

    @Bean
    public CommandLineRunner addSystemMessageToShutdown(final ShutdownConfig shutdownConfig) {
        return args -> {
            final Thread shutdownHook = new Thread(shutdownConfig.onShutdown());
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        };
    }
}
