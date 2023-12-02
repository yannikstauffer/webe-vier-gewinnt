package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameActionDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.level.LevelService;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @Mock
    private LevelService levelService;

    @Mock
    private GameMessagesProxy gameMessagesProxy;

    @Mock
    private Principal principal;

    @InjectMocks
    private GameController gameController;

    static GameActionDto gameActionDto(final int gameId, final int column) {
        return GameActionDto.builder()
                .gameId(gameId)
                .column(column)
                .build();
    }

    @Test
    void createGame() {
        // arrange
        final var email = "user@example.com";
        final var user = new User();
        user.setEmail(email);
        final var game = new Game();
        when(principal.getName()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(gameService.createGame(user)).thenReturn(game);

        // act
        gameController.createGame(principal);

        // assert
        verify(gameService).createGame(user);
        verify(gameMessagesProxy).notifyAll(game);
    }

    @Test
    void joinGame() {
        // arrange
        final var gameId = 1;
        final var email = "user@example.com";
        final var request = new GameRequestDto();
        request.setGameId(gameId);
        final var user = new User();
        user.setEmail(email);
        final var game = new Game();
        when(principal.getName()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(gameService.joinGame(gameId, user)).thenReturn(game);

        // act
        gameController.joinGame(request, principal);

        // assert
        verify(gameService).joinGame(gameId, user);
        verify(gameMessagesProxy).notifyAll(game);
    }

    @Test
    void gameControl() {
        // arrange
        final var gameId = 1;
        final var email = "user@example.com";
        final var request = new GameRequestDto();
        request.setGameId(gameId);
        final var user = new User();
        user.setEmail(email);
        final var game = new Game();
        when(principal.getName()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(gameService.controlGame(request, user)).thenReturn(game);
        when(levelService.applyLevelModifications(game)).thenReturn(java.util.Optional.of(game));

        // act
        gameController.gameControl(request, principal);

        // assert
        verify(gameService).controlGame(request, user);
        verify(levelService).applyLevelModifications(game);
        verify(gameMessagesProxy).notifyAll(game);
    }

    @Test
    void gameAction() {
        // arrange
        final var gameId = 1;
        final var column = 2;
        final var email = "user@example.com";
        final var actionDto = gameActionDto(gameId, column);
        final var user = new User();
        user.setEmail(email);
        final var game = new Game();
        when(principal.getName()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(gameService.dropDisc(gameId, column, user)).thenReturn(game);
        when(levelService.applyLevelModifications(game)).thenReturn(java.util.Optional.of(game));

        // act
        gameController.gameAction(actionDto, principal);

        // assert
        verify(gameService).dropDisc(gameId, column, user);
        verify(levelService).applyLevelModifications(game);
        verify(gameMessagesProxy).notifyPlayers(game);
    }
}
