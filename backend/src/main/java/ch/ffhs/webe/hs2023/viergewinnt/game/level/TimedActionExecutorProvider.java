package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ScheduledExecutorService;


@Getter
@AllArgsConstructor
public class TimedActionExecutorProvider {
    private final ScheduledExecutorService scheduledExecutorService;

}
