package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.MessageSources;
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

    @Autowired
    public GameController(final GameService gameService, final UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @MessageMapping(MessageSources.GAMES + "/create")
    @SendTo(Topics.LOBBY_GAMES + "/create")
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
    @SendTo(Topics.LOBBY_GAMES + "/joined")
    public GameDto joinGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());
        final var game = this.gameService.joinGame(request, sender);
        return GameDto.of(game);
    }

    @MessageMapping(MessageSources.GAMES + "/left")
    @SendTo(Topics.LOBBY_GAMES + "/all")
    public List<GameDto> leftGame(@Payload final GameRequestDto request, final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());

        this.gameService.leftGame(request, sender);

        return this.allGames();
    }

    private List<GameDto> allGames() {
        final var games = this.gameService.getAllGames();
        return GameDto.of(games);
    }
}
