package ch.ffhs.webe.hs2023.viergewinnt;

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
}
