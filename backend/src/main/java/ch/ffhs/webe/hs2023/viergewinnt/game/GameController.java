package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
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

    @MessageMapping("/games/create")
    @SendTo("/topic/lobby/games/create")
    public Game createGame(final Principal user) {
        final var sender = this.userService.getUserByEmail(user.getName());

        return this.gameService.createGame(sender);
    }

    @MessageMapping("/games/all")
    @SendTo("/topic/lobby/games/all")
    public List<Game> getAllGames() {
        return this.gameService.getAllGames();
    }

    @MessageMapping("/games/deleteAll")
    @SendTo("/topic/lobby/games/all")
    public List<Game> deleteAllGames() {
        this.gameService.deleteAllGames();
        return this.gameService.getAllGames();
    }

    @MessageMapping("/games/join")
    @SendTo("/topic/lobby/games/joined")
    public Game joinGame(@Payload final GameRequestDto request) {
        return this.gameService.joinGame(request);
    }

    @MessageMapping("/games/left")
    @SendTo("/topic/lobby/games/all")
    public List<Game> leftGame(@Payload final GameRequestDto request) {
        this.gameService.leftGame(request);
        return this.gameService.getAllGames();
    }
}
