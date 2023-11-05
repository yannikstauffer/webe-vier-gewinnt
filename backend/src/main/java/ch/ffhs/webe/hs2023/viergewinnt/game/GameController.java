package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameActionDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
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
    @SendTo(Topics.LOBBY_GAMES)
    public GameDto createGame(final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.createGame(sender);
        return GameDto.of(game);
    }

    @MessageMapping(MessageSources.GAMES + "/all")
    @SendTo(Topics.LOBBY_GAMES + "/all")
    public List<GameDto> getAllGames() {
        return this.allGames();
    }

    @MessageMapping(MessageSources.GAMES + "/deleteAll")
    @SendTo(Topics.LOBBY_GAMES + "/all")
    public List<GameDto> deleteAllGames() {
        this.gameService.deleteAllGames();
        return this.allGames();
    }

    @MessageMapping(MessageSources.GAMES + "/join")
    @SendTo(Topics.LOBBY_GAMES)
    public GameDto joinGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.joinGame(request.getGameId(), sender);

        return GameDto.of(game);
    }

    @MessageMapping(MessageSources.GAMES + "/left")
    @SendTo(Topics.LOBBY_GAMES + "/all")
    public List<GameDto> leftGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        this.gameService.leftGame(request.getGameId(), sender);
        return this.allGames();
    }

    @MessageMapping(MessageSources.GAMES + "/control")
    public void gameAction(@Payload final GameRequestDto request, Principal user) {
        final var game = gameService.getGameById(request.getGameId());

        if (game.getUserOne() != null && game.getUserTwo() != null) {
            this.gameService.startGame(game);
        }

        this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserOne().getId()), GameStateDto.of(game));

        if (game.getUserTwo() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserTwo().getId()), GameStateDto.of(game));
        }
    }

    @MessageMapping(MessageSources.GAMES + "/action")
    public void gameAction(@Payload final GameActionDto request, Principal user) {
        final var sender = userService.getUserByEmail(user.getName());
        final var game = gameService.updateGameBoard(request.getGameId(), request.getColumn(), sender);

        this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserOne().getId()), GameStateDto.of(game));
        this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserTwo().getId()), GameStateDto.of(game));
    }


    private List<GameDto> allGames() {
        final var games = this.gameService.getAllGames();
        return GameDto.of(games);
    }
}
