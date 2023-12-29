package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameActionDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.level.LevelService;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final LevelService levelService;
    private final GameMessagesProxy gameMessagesProxy;

    @Autowired
    public GameController(final GameService gameService,
                          final UserService userService,
                          final LevelService levelService,
                          final GameMessagesProxy gameMessagesProxy) {
        this.gameService = gameService;
        this.userService = userService;
        this.levelService = levelService;
        this.gameMessagesProxy = gameMessagesProxy;
    }

    @MessageMapping(MessageSources.GAMES + "/create")
    public void createGame(final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.createGame(sender);

        this.gameMessagesProxy.notifyAll(game);
    }

    @MessageMapping(MessageSources.GAMES + "/join")
    public void joinGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.joinGame(request.getGameId(), sender);

        this.gameMessagesProxy.notifyAll(game);
    }

    @MessageMapping(MessageSources.GAMES + "/control")
    public void gameControl(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.controlGame(request, sender);


        final var modifiedGame = this.levelService.applyLevelModifications(game)
                .orElse(game);

        this.gameMessagesProxy.notifyAll(modifiedGame);
    }

    @MessageMapping(MessageSources.GAMES + "/action")
    public void gameAction(@Payload final GameActionDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.dropDisc(request.getGameId(), request.getColumn(), sender);

        final var modifiedGame = this.levelService.applyLevelModifications(game)
                .orElse(game);

        this.gameMessagesProxy.notifyPlayers(modifiedGame);

    }


}
