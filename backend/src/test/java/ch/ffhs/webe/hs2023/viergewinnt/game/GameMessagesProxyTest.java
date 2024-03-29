package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState;
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
        final var user1 = game.getUserOne();
        final var user2 = game.getUserTwo();
        when(this.userService.getUserById(user1.getId())).thenReturn(user1);
        when(this.userService.getUserById(user2.getId())).thenReturn(user2);

        // act
        this.gameMessagesProxy.notifyPlayers(game);

        // assert
        verify(this.userService).getUserById(user1.getId());
        verify(this.userService).getUserById(user2.getId());
        verify(this.messageService).sendToUser(expectedQueue, user1, expectedDto);
        verify(this.messageService).sendToUser(expectedQueue, user2, expectedDto);
    }


    @Test
    void notifyPlayers_doesNotNotify_ifPlayerStateIsNotConnected() {
        // arrange
        final var game = game(1);
        final var user1 = game.getUserOne();
        final var user2 = game.getUserTwo();
        game.setUserState(user1.getId(), UserState.DISCONNECTED);
        game.setUserState(user2.getId(), UserState.QUIT);

        // act
        this.gameMessagesProxy.notifyPlayers(game);

        // assert
        verifyNoInteractions(this.messageService, this.userService);
    }

    @Test
    void notifyPlayers_withNoPlayersInGame_DoesNotSend() {
        // arrange
        final var game = mock(Game.class);
        when(game.isUserOneConnected()).thenReturn(false);
        when(game.isUserTwoConnected()).thenReturn(false);

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