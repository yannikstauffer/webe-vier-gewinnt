package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import ch.ffhs.webe.hs2023.viergewinnt.game.GameMessagesProxy;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL2;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL3;

@Slf4j
@Component
public class LevelService {
    static final long LEVEL_TWO_DURATION_IN_SEC = 5;
    static final int LEVEL_THREE_ROUND_COUNT = 5;

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

    public Optional<Game> applyLevelModifications(final Game game) {
        if (game.isMoveExpected() && game.getGameLevel() == LEVEL2) {
            this.cancelScheduledLevel2Action(game);
            this.addLevel2TimedDiscDrop(game);
        } else if (game.isMoveExpected() && game.getGameLevel() == LEVEL3) {
            return this.addLevel3AnyonymousDiscDrop(game);
        }
        return Optional.empty();

    }

    private void cancelScheduledLevel2Action(final Game game) {
        this.timedActionScheduler.cancel(game);
    }

    private Optional<Game> addLevel3AnyonymousDiscDrop(final Game game) {
        final var discCount = game.getBoard().getPlayerDiscCount();

        if (discCount > 0 && discCount % LEVEL_THREE_ROUND_COUNT == 0) {
            log.debug("LEVEL3: Adding random disc drop");
            return Optional.of(this.gameService.dropRandomAnonymousDisc(game.getId()));
        } else {
            log.trace("LEVEL3: No action for this round");
        }
        return Optional.empty();
    }

    private void addLevel2TimedDiscDrop(final Game game) {

        final var user = game.getNextMove() == game.getUserOne().getId() ? game.getUserOne() : game.getUserTwo();

        final var timedAction = new TimedAction(game, LevelService.LEVEL_TWO_DURATION_IN_SEC, TimeUnit.SECONDS) {
            @Override
            public void action() {
                final var game = LevelService.this.gameService.dropRandomDisc(this.game.getId(), user);
                LevelService.log.debug("LEVEL2: Dropped random disc for user {} in game {}", user.getId(), game.getId());
                LevelService.this.gameMessagesProxy.notifyPlayers(game);
                LevelService.this.applyLevelModifications(game);
            }
        };

        this.timedActionScheduler.schedule(timedAction);
    }
}
