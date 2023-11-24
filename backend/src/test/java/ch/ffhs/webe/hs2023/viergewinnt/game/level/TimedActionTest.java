package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.game;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TimedActionTest {
    TimedAction timedActionSpy;

    @BeforeEach
    void setUp() {
        this.timedActionSpy = timedActionSpy(0L);
    }

    @Test
    void run() {
        // act
        this.timedActionSpy.run();

        // assert
        assertThat(this.timedActionSpy.isCancelled()).isFalse();
        verify(this.timedActionSpy, times(1)).action();
    }

    @Test
    void run_wontTriggerAction_ifCancelled() {
        // arrange
        this.timedActionSpy.cancel();

        // act
        this.timedActionSpy.run();

        // assert
        assertThat(this.timedActionSpy.isCancelled()).isTrue();
        verify(this.timedActionSpy, never()).action();
    }

    static TimedAction timedActionSpy(final long milliDuration) {
        return spy(new TimedAction(game(1), milliDuration, TimeUnit.MILLISECONDS) {
            @Override
            public void action() {
                // foobar
            }
        });
    }
}