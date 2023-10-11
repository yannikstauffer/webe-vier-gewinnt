package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/games/create")
    @SendTo("/topic/lobby/games/create")
    public GameResponseDto createGame(@Payload GameRequestDto request) {
        return gameService.createGame(request);
    }

    @MessageMapping("/games/all")
    @SendTo("/topic/lobby/games/all")
    public List<GameResponseDto> getAllGames() {
        return gameService.getAllGames();
    }

    @MessageMapping("/games/deleteAll")
    @SendTo("/topic/lobby/games/all")
    public List<GameResponseDto> deleteAllGames() {
        gameService.deleteAllGames();
        return gameService.getAllGames();
    }

    @MessageMapping("/games/join")
    @SendTo("/topic/lobby/games/joined")
    public GameResponseDto joinGame(@Payload GameRequestDto request) {
        return gameService.joinGame(request);
    }
}