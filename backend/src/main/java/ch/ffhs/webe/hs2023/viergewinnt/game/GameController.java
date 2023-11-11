package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameActionDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
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
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final StompMessageService messageService;

    @Autowired
    public GameController(final GameService gameService, final UserService userService, final StompMessageService messageService) {
        this.gameService = gameService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @MessageMapping(MessageSources.GAMES + "/create")
    public void createGame(final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.createGame(sender);

        this.messageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
    }

    @MessageMapping(MessageSources.GAMES + "/join")
    public void joinGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.joinGame(request.getGameId(), sender);

        this.messageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
    }

    @MessageMapping(MessageSources.GAMES + "/control")
    public void gameControl(@Payload final GameRequestDto request, Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = gameService.controlGame(request, sender);

        notifyPlayers(game);
    }

    @MessageMapping(MessageSources.GAMES + "/action")
    public void gameAction(@Payload final GameActionDto request, Principal user) {
        final var sender = userService.getUserByEmail(user.getName());
        final var game = gameService.updateGameBoard(request.getGameId(), request.getColumn(), sender);

        notifyPlayers(game);
    }

    private void notifyPlayers(Game game) {
        if (game == null) {
            return;
        }

        if (game.getUserOne() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserOne().getId()), GameStateDto.of(game));
        }
        if (game.getUserTwo() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserTwo().getId()), GameStateDto.of(game));
        }

    }
}
