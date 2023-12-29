package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimedActionSchedulerTest {
    @Mock
    ScheduledExecutorService scheduledExecutorService;

    TimedActionScheduler timedActionScheduler;

    @BeforeEach
    void setUp() {
        this.timedActionScheduler = new TimedActionScheduler(new TimedActionExecutorProvider(this.scheduledExecutorService));
    }

    @AfterEach
    void tearDown() {
        TimedActionScheduler.gameTimedAction.clear();
    }

    @Test
    void schedule() {
        // arrange
        final var delay = 1000L;
        final var timedAction = TimedActionTest.timedActionSpy(delay);
        final var expectedTimeUnit = timedAction.getTimeUnit();
        // act
        this.timedActionScheduler.schedule(timedAction);

        // assert
        assertThat(TimedActionScheduler.gameTimedAction).containsValue(timedAction);
        verify(this.scheduledExecutorService)
                .schedule(any(Runnable.class), eq(delay), eq(expectedTimeUnit));
    }

    @Test
    void cancel() {
        // arrange
        final var timedAction = TimedActionTest.timedActionSpy(0L);

        // act
        this.timedActionScheduler.schedule(timedAction);
        this.timedActionScheduler.cancel(timedAction.getGame());

        // assert
        assertThat(timedAction.isCancelled()).isTrue();
    }
}