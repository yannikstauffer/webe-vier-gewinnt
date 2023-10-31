package ch.ffhs.webe.hs2023.viergewinnt;

import ch.ffhs.webe.hs2023.viergewinnt.base.SystemMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.SessionRepository;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static ch.ffhs.webe.hs2023.viergewinnt.base.SystemMessageCode.SERVER_IS_SHUTTING_DOWN;

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
    public CommandLineRunner addSystemMessageToShutdown(final StompMessageService stompMessageService) {
        return args -> {
            final var shutdownMessage = SystemMessageDto.of(SERVER_IS_SHUTTING_DOWN);
            final Thread shutdownHook = new Thread(() -> stompMessageService.send(Topics.SYSTEM, shutdownMessage));
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        };
    }
}
