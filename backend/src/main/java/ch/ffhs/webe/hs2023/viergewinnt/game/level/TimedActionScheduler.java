package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
public class TimedActionScheduler {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private static final ConcurrentHashMap<Integer, TimedAction> gameTimedAction = new ConcurrentHashMap<>();

    public void schedule(final TimedAction timedAction) {
        gameTimedAction.put(timedAction.getGame().getId(), timedAction);

        log.debug("Scheduling timed action for game {}", timedAction.getGame().getId());
        scheduledExecutorService.schedule(
                () -> {
                    log.debug("Running timed action for game {}", timedAction.getGame().getId());
                    gameTimedAction.remove(timedAction.getGame().getId());
                    timedAction.run();
                }
                ,
                timedAction.getDuration(),
                timedAction.getTimeUnit()
        );
    }

    public void cancel(final Game game) {
        final var action = gameTimedAction.get(game.getId());
        if (action != null) {
            log.debug("Cancelling timed action for game {}", game.getId());
            action.cancel();
        }
    }
}
