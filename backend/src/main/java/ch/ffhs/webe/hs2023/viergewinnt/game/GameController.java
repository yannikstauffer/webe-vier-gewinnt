package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameActionDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.level.TimedAction;
import ch.ffhs.webe.hs2023.viergewinnt.game.level.TimedActionScheduler;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.MessageSources;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.concurrent.TimeUnit;

import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL2;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL3;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState.IN_PROGRESS;

@Slf4j
@Controller
public class GameController {
    private static final long LEVEL_TWO_DURATION_IN_SEC = 5;

    private final GameService gameService;
    private final UserService userService;
    private final StompMessageService messageService;
    private final TimedActionScheduler timedActionScheduler;

    @Autowired
    public GameController(final GameService gameService,
                          final UserService userService,
                          final StompMessageService messageService,
                          final TimedActionScheduler timedActionScheduler) {
        this.gameService = gameService;
        this.userService = userService;
        this.messageService = messageService;
        this.timedActionScheduler = timedActionScheduler;
    }

    @MessageMapping(MessageSources.GAMES + "/create")
    public void createGame(final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.createGame(sender);

        this.messageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
        this.notifyPlayers(game);
    }

    @MessageMapping(MessageSources.GAMES + "/join")
    public void joinGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.joinGame(request.getGameId(), sender);

        this.messageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
        this.notifyPlayers(game);
    }

    @MessageMapping(MessageSources.GAMES + "/control")
    public void gameControl(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.controlGame(request, sender);

        this.messageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
        this.notifyPlayers(game);
    }

    @MessageMapping(MessageSources.GAMES + "/action")
    public void gameAction(@Payload final GameActionDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.updateGameBoard(request.getGameId(), request.getColumn(), sender, request.getMessage());

        this.clearCustomLevelSettings(game);

        this.notifyPlayers(game);

        this.processCustomLevelSettings(game);

    }

    private void notifyPlayers(final Game game) {
        if (game.getUserOne() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserOne().getId()), GameStateDto.of(game));
        }
        if (game.getUserTwo() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserTwo().getId()), GameStateDto.of(game));
        }
    }

    private void clearCustomLevelSettings(final Game game) {
        this.timedActionScheduler.cancel(game);
    }

    private void processCustomLevelSettings(final Game game) {
        this.addLevel2TimedDiscDrop(game);
        this.level3Implementation(game);
    }

    private void level3Implementation(final Game game) {
        if (game.getGameLevel() != LEVEL3 || game.getGameState() != IN_PROGRESS) {
            return;
        }
        //todo: implement level 3
        log.info("LEVEL3: Not yet implemented");

    }

    private void addLevel2TimedDiscDrop(final Game game) {
        if (game.getGameLevel() != LEVEL2 || game.getGameState() != IN_PROGRESS) {
            return;
        }
        final var user = game.getNextMove() == game.getUserOne().getId() ? game.getUserOne() : game.getUserTwo();

        final var timedAction = new TimedAction(game, GameController.LEVEL_TWO_DURATION_IN_SEC, TimeUnit.SECONDS) {
            @Override
            public void action() {
                final var game = GameController.this.gameService.dropRandomDisc(this.game.getId(), user);
                GameController.log.debug("LEVEL2: Dropped random disc for user {} in game {}", user.getId(), game.getId());
                GameController.this.notifyPlayers(game);
                GameController.this.processCustomLevelSettings(game);
            }
        };

        this.timedActionScheduler.schedule(timedAction);
    }

}
