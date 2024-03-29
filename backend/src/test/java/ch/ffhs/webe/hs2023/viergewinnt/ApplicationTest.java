package ch.ffhs.webe.hs2023.viergewinnt;

import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ApplicationTest {
    @MockBean
    SessionRepository sessionRepository;

    @MockBean
    GameService gameService;

    @Autowired
    Application application;

    @Test
    void commandLineRunners_onStart() {
        verify(this.sessionRepository, times(1)).deleteAll();
        verify(this.gameService, times(1)).setAllConnectedUsersAsDisconnected();
    }

}