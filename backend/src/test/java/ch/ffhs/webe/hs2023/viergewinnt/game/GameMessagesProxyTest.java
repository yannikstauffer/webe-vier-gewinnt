package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.game;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameMessagesProxyTest {

    @Mock
    StompMessageService messageService;
    @Mock
    UserService userService;

    @InjectMocks
    GameMessagesProxy gameMessagesProxy;

    @Test
    void notifyPlayers() {
        // arrange
        final var game = game(1);
        final var expectedQueue = Queues.GAME;
        final var expectedDto = GameStateDto.of(game);
        when(this.userService.getUserById(game.getUserOne().getId())).thenReturn(game.getUserOne());
        when(this.userService.getUserById(game.getUserTwo().getId())).thenReturn(game.getUserTwo());

        // act
        this.gameMessagesProxy.notifyPlayers(game);

        // assert
        verify(this.userService).getUserById(game.getUserOne().getId());
        verify(this.userService).getUserById(game.getUserTwo().getId());
        verify(this.messageService).sendToUser(expectedQueue, game.getUserOne(), expectedDto);
        verify(this.messageService).sendToUser(expectedQueue, game.getUserTwo(), expectedDto);
    }

    @Test
    void notifyPlayers_withNoPlayersInGame_DoesNotSend() {
        // arrange
        final var game = mock(Game.class);
        when(game.getUserOne()).thenReturn(null);
        when(game.getUserTwo()).thenReturn(null);

        // act
        this.gameMessagesProxy.notifyPlayers(game);

        // assert
        verifyNoInteractions(this.messageService, this.userService);
    }

    @Test
    void testNotifyAll() {
        // arrange
        final var game = game(2);
        final var expectedTopic = Topics.LOBBY_GAMES;
        final var expectedDto = GameDto.of(game);
        final var gameMessagesProxySpy = spy(this.gameMessagesProxy);

        // act
        gameMessagesProxySpy.notifyAll(game);

        // assert
        verify(this.messageService).send(expectedTopic, expectedDto);
        verify(gameMessagesProxySpy).notifyPlayers(game);
    }
}