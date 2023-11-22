package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class TimedAction implements Runnable {
    protected final LocalDateTime startTime;
    protected final long duration;
    protected final TimeUnit timeUnit;
    @Getter
    public final Game game;
    @Getter
    private boolean cancelled;

    protected TimedAction(final Game game, final long duration, final TimeUnit timeUnit) {
        this.startTime = LocalDateTime.now();
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.game = game;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public final void run() {
        if (this.cancelled) {
            return;
        }
        this.action();
    }

    public abstract void action();


}
