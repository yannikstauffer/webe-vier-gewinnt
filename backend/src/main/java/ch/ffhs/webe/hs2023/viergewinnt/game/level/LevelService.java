package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import ch.ffhs.webe.hs2023.viergewinnt.game.GameMessagesProxy;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL2;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL3;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState.IN_PROGRESS;

@Slf4j
@Component
public class LevelService {
    private static final long LEVEL_TWO_DURATION_IN_SEC = 5;

    private final TimedActionScheduler timedActionScheduler;
    private final GameMessagesProxy gameMessagesProxy;
    private final GameService gameService;

    @Autowired
    public LevelService(final TimedActionScheduler timedActionScheduler, final GameMessagesProxy gameMessagesProxy,
                        final GameService gameService) {
        this.timedActionScheduler = timedActionScheduler;
        this.gameMessagesProxy = gameMessagesProxy;
        this.gameService = gameService;
    }

    public void clearLevelActions(final Game game) {
        this.timedActionScheduler.cancel(game);
    }

    public void applyLevelActions(final Game game) {

        if (game.getGameLevel() == LEVEL2 && game.getGameState() == IN_PROGRESS) {
            this.addLevel2TimedDiscDrop(game);
        } else if (game.getGameLevel() == LEVEL3 || game.getGameState() == IN_PROGRESS) {
            this.level3Implementation(game);
        }
    }

    private void level3Implementation(final Game game) {

        //todo: implement level 3
        log.info("LEVEL3: Not yet implemented");

    }

    private void addLevel2TimedDiscDrop(final Game game) {

        final var user = game.getNextMove() == game.getUserOne().getId() ? game.getUserOne() : game.getUserTwo();

        final var timedAction = new TimedAction(game, LevelService.LEVEL_TWO_DURATION_IN_SEC, TimeUnit.SECONDS) {
            @Override
            public void action() {
                final var game = LevelService.this.gameService.dropRandomDisc(this.game.getId(), user);
                LevelService.log.debug("LEVEL2: Dropped random disc for user {} in game {}", user.getId(), game.getId());
                LevelService.this.gameMessagesProxy.notifyPlayers(game);
                LevelService.this.applyLevelActions(game);
            }
        };

        this.timedActionScheduler.schedule(timedAction);
    }
}
