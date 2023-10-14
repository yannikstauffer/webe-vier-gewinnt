package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameResponseDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final UserService userService;

    @Autowired
    public GameService(GameRepository gameRepository, final UserService userService) {
        this.gameRepository = gameRepository;
        this.userService = userService;
    }

    public GameResponseDto createGame(GameRequestDto request) {
        Game newGame = new Game();
        newGame.setStatus(GameState.WAITING_FOR_PLAYERS);

        newGame.setUserOne(userService.getCurrentlyAuthenticatedUser());

        Game savedGame = gameRepository.save(newGame);
        log.debug("Saved new game with ID: " + savedGame.getId());

        return GameResponseDto.of(savedGame);
    }

    public List<GameResponseDto> getAllGames() {
        Iterable<Game> gamesIterable = gameRepository.findAll();

        List<Game> games = new ArrayList<>();
        gamesIterable.forEach(games::add);

        return games.stream()
                .map(GameResponseDto::of)
                .collect(Collectors.toList());
    }

    public GameResponseDto joinGame(GameRequestDto request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.getUserOne() != null && game.getUserTwo() == null) {
            game.setUserTwo(userService.getCurrentlyAuthenticatedUser());
            game.setStatus(GameState.IN_PROGRESS);
            gameRepository.save(game);
        } else {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Das Spiel ist bereits voll!");
        }

        return GameResponseDto.of(game);
    }

    public void leftGame(GameRequestDto request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.getUserOne().getId() == userService.getCurrentlyAuthenticatedUser().getId()) {
            gameRepository.deleteById(request.getGameId());
        } else if (game.getUserTwo().getId() == userService.getCurrentlyAuthenticatedUser().getId()) {
            game.setUserTwo(null);
            game.setStatus(GameState.WAITING_FOR_PLAYERS);
            gameRepository.save(game);
        }
    }


    public void deleteAllGames() {
        gameRepository.deleteAll();
    }
}
