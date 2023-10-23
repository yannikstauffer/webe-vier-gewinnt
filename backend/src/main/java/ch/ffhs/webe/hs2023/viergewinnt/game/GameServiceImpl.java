package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final UserService userService;

    @Autowired
    public GameServiceImpl(final GameRepository gameRepository, final UserService userService) {
        this.gameRepository = gameRepository;
        this.userService = userService;
    }

    @Override
    public Game createGame(final User creator) {
        final Game newGame = new Game();
        newGame.setStatus(GameState.WAITING_FOR_PLAYERS);

        newGame.setUserOne(creator);

        final Game savedGame = this.gameRepository.save(newGame);
        log.debug("Saved new game with ID: " + savedGame.getId());

        return savedGame;
    }

    @Override
    public List<Game> getAllGames() {
        final Iterable<Game> gamesIterable = this.gameRepository.findAll();

        final List<Game> games = new ArrayList<>();
        gamesIterable.forEach(games::add);

        return games;
    }

    @Override
    public Game joinGame(final GameRequestDto request) {
        final Game game = this.gameRepository.findById(request.getGame().getId())
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.isFull()) {
            throw VierGewinntException.of(ErrorCode.GAME_FULL, "Das Spiel ist bereits voll!");
        }

        game.setUserTwo(this.userService.getCurrentlyAuthenticatedUser());
        game.setStatus(GameState.IN_PROGRESS);
        this.gameRepository.save(game);

        return game;
    }

    @Override
    public void leftGame(final GameRequestDto request) {
        final Game game = this.gameRepository.findById(request.getGame().getId())
                .orElseThrow(() -> VierGewinntException.of(ErrorCode.GAME_NOT_FOUND, "Spiel nicht gefunden!"));

        if (game.getUserOne().getId() == this.userService.getCurrentlyAuthenticatedUser().getId()) {
            this.gameRepository.deleteById(request.getGame().getId());
        } else if (game.getUserTwo().getId() == this.userService.getCurrentlyAuthenticatedUser().getId()) {
            game.setUserTwo(null);
            game.setStatus(GameState.WAITING_FOR_PLAYERS);
            this.gameRepository.save(game);
        }
    }

    @Override
    public void deleteAllGames() {
        this.gameRepository.deleteAll();
    }
}
