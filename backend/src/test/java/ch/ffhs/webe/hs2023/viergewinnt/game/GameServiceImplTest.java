package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameRequestDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.repository.GameRepository;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.GameState;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;

import static ch.ffhs.webe.hs2023.viergewinnt.game.GameBoard.COLUMN_COUNT;
import static ch.ffhs.webe.hs2023.viergewinnt.game.GameBoard.ROW_COUNT;
import static ch.ffhs.webe.hs2023.viergewinnt.game.GameBoardTest.fullGameBoard;
import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.game;
import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.gameWithoutUsers;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.CONTINUE;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEAVE;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEVEL1;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEVEL2;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.LEVEL3;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.RESTART;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.ControlMessage.START;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState.CONNECTED;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState.DISCONNECTED;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState.QUIT;
import static ch.ffhs.webe.hs2023.viergewinnt.user.model.UserTest.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    GameRepository gameRepository;

    @Mock
    UserService userService;

    @InjectMocks
    GameServiceImpl gameService;

    @Test
    void createGame() {
        final var user = user(100);
        this.stubRepository(null);

        final var game = this.gameService.createGame(user);

        assertThat(game.getUserOne()).isEqualTo(user);
    }

    @Test
    void getAllGames() {
        this.gameService.getAllGames();

        verify(this.gameRepository).findCurrentlyActive();
    }

    @Test
    void joinGame() {
        // arrange
        final var user = user(100);
        final var game = gameWithoutUsers(1);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.joinGame(game.getId(), user);

        // assert
        assertThat(updatedGame.getUsers()).contains(user);
        verify(this.gameRepository).save(game);
        verify(this.userService).setCurrentGameId(user.getId(), game.getId());
    }

    @Test
    void joinGame_onFullGame_throwsException() {
        // arrange
        final var user = user(200);
        final var game = game(2);
        this.stubRepository(game);

        // act + assert
        assertThatThrownBy(() -> this.gameService.joinGame(game.getId(), user))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Game is full");


    }

    @Test
    void joinGame_onFullGameWithExistingUser_resetsUserState() {
        // arrange
        final var game = game(3);
        final var user1 = game.getUserOne();
        game.setUserState(user1.getId(), DISCONNECTED);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.joinGame(game.getId(), user1);

        // assert
        assertThat(updatedGame.getUserOne()).isEqualTo(user1);
        assertThat(updatedGame.getUserOneState()).isEqualTo(CONNECTED);
        verify(this.gameRepository).save(game);
        verify(this.userService).setCurrentGameId(user1.getId(), game.getId());


    }

    @Test
    void controlGame_throwsException_withUnknownMessage() {
        // arrange
        final var game = game(4, GameState.WAITING_FOR_PLAYERS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, "foo");
        this.stubRepository(game);

        // act + assert
        assertThatThrownBy(() -> this.gameService.controlGame(dto, user1))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Unknown game message foo");
    }

    @Test
    void controlGame_withMessageStart() {
        // arrange
        final var game = game(4, GameState.WAITING_FOR_PLAYERS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, START);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.IN_PROGRESS);
        verify(game).start();
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageRestart() {
        // arrange
        final var game = game(5, GameState.DRAW);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, RESTART);
        this.stubRepository(game);

        // act
        final var newGame = this.gameService.controlGame(dto, user1);

        // assert
        verify(game).copyAsNew();
        assertThat(newGame.getGameState()).isEqualTo(GameState.IN_PROGRESS);
        assertThat(newGame.getUsers()).containsAll(game.getUsers());
        verify(this.gameRepository, times(1)).save(newGame);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageContinue() {
        // arrange
        final var game = game(6, GameState.PAUSED);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, CONTINUE);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.IN_PROGRESS);
        verify(game).resume();
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLeaveAndWaitingForPlayers() {
        // arrange
        final var game = game(7, GameState.WAITING_FOR_PLAYERS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, LEAVE);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.WAITING_FOR_PLAYERS);
        assertThat(game.getUserOne()).isNull();
        assertThat(game.getUserOneState()).isNull();
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLeaveAndInProgress() {
        // arrange
        final var game = game(7, GameState.IN_PROGRESS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, LEAVE);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.PLAYER_LEFT);
        verify(game).setUserState(user1.getId(), QUIT);
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLeaveAndFinished() {
        // arrange
        final var game = game(8, GameState.DRAW);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, LEAVE);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.DRAW);
        verify(game).setUserState(user1.getId(), QUIT);
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLeaveDeletesGame_ifNoMoreUserPresent() {
        // arrange
        final var game = game(9, GameState.IN_PROGRESS);
        final var dto = gameRequestDto(game, LEAVE);
        this.stubRepository(game);

        // act
        var updatedGame = this.gameService.controlGame(dto, game.getUserOne());
        updatedGame = this.gameService.controlGame(dto, game.getUserTwo());

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.DELETED);
        verify(this.gameRepository, times(2)).findById(game.getId());
        verify(this.gameRepository, times(1)).save(game);
        verify(this.gameRepository, times(1)).delete(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLevel1() {
        // arrange
        final var game = game(10, GameState.WAITING_FOR_PLAYERS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, LEVEL1);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.WAITING_FOR_PLAYERS);
        assertThat(updatedGame.getGameLevel()).isEqualTo(GameLevel.LEVEL1);
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLevel2() {
        // arrange
        final var game = game(11, GameState.WAITING_FOR_PLAYERS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, LEVEL2);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.WAITING_FOR_PLAYERS);
        assertThat(updatedGame.getGameLevel()).isEqualTo(GameLevel.LEVEL2);
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void controlGame_withMessageLevel3() {
        // arrange
        final var game = game(12, GameState.WAITING_FOR_PLAYERS);
        final var user1 = game.getUserOne();
        final var dto = gameRequestDto(game, LEVEL3);
        this.stubRepository(game);

        // act
        final var updatedGame = this.gameService.controlGame(dto, user1);

        // assert
        assertThat(updatedGame.getGameState()).isEqualTo(GameState.WAITING_FOR_PLAYERS);
        assertThat(updatedGame.getGameLevel()).isEqualTo(GameLevel.LEVEL3);
        verify(this.gameRepository, times(1)).save(game);
        verifyNoMoreInteractions(this.gameRepository);
    }

    @Test
    void getGameById() {
        this.stubRepository(game(123));

        this.gameService.getGameById(123);

        verify(this.gameRepository, times(1)).findById(123);
    }

    @Test
    void setUserAsDisconnected() {
        // arrange
        final var game1 = game(1, GameState.WAITING_FOR_PLAYERS);
        final var game2 = game(2, GameState.IN_PROGRESS);
        final var game3 = game(3, GameState.PAUSED);
        final var game4 = game(4, GameState.DRAW);
        final var game5 = game(5, GameState.PLAYER_HAS_WON);
        final var games = List.of(game1, game2, game3, game4, game5);
        final var expectedUpdatedGames = List.of(game1, game2, game3);

        // act
        this.gameService.setUserAsDisconnected(game1.getUserOne(), games);

        // assert
        assertThat(game1.getUserOne()).isNull();
        assertThat(game1.getUserOneState()).isNull();
        assertThat(game1.getGameState()).isEqualTo(GameState.WAITING_FOR_PLAYERS);
        assertThat(game2.getUserOneState()).isEqualTo(DISCONNECTED);
        assertThat(game2.getGameState()).isEqualTo(GameState.PAUSED);
        assertThat(game3.getUserOneState()).isEqualTo(DISCONNECTED);
        assertThat(game3.getGameState()).isEqualTo(GameState.PAUSED);
        verify(game4, never()).setUserState(anyInt(), any());
        verify(game5, never()).setUserState(anyInt(), any());
        verify(this.gameRepository, times(1)).saveAll(expectedUpdatedGames);
    }

    @Test
    void getGamesForUser() {
        final var games = List.of(game(1), game(2));
        when(this.gameRepository.findCurrentlyActiveForUserId(100)).thenReturn(games);

        final var actual = this.gameService.getGamesForUser(100);

        verify(this.gameRepository).findCurrentlyActiveForUserId(100);
        assertThat(actual).containsAll(games);
    }

    @Test
    void dropPlayerDisc() {
        final var game = game(1, GameState.IN_PROGRESS);

        this.gameService.dropPlayerDisc(game, 3, game.getUserOne());

        assertBoardContains(game.getBoard(), 0, 3, game.getUserOne().getId());
        assertThat(game.getNextMove()).isEqualTo(game.getUserTwo().getId());
    }

    @Test
    void dropPlayerDisc_setGameStatePlayerHasWon_ifPlayerWins() {
        final var game = game(1, GameState.IN_PROGRESS);
        final var board = new GameBoard();
        board.addDisc(3, game.getUserOne().getId());
        board.addDisc(3, game.getUserOne().getId());
        board.addDisc(3, game.getUserOne().getId());
        game.setBoard(board);

        this.gameService.dropPlayerDisc(game, 3, game.getUserOne());

        assertThat(game.getGameState()).isEqualTo(GameState.PLAYER_HAS_WON);
        assertThat(game.getNextMove()).isEqualTo(game.getUserOne().getId());
    }

    @Test
    void dropPlayerDisc_setGameStateDraw_ifBoardGetsFull() {
        final var game = game(1, GameState.IN_PROGRESS);
        //        final var board = new GameBoard();
//
//        var dropCount = 0;
//        for (int i = 0; i < GameBoard.ROW_COUNT; i++) {
//            for (int j = 0; j < COLUMN_COUNT && dropCount < amount; j++) {
//                board.addDisc(j, discNumber);
//                dropCount++;
//            }
//        }
//        return board;
        final var board = GameBoardTest.gameBoard(COLUMN_COUNT * ROW_COUNT - 1, -1);
        game.setBoard(board);

        this.gameService.dropPlayerDisc(game, COLUMN_COUNT - 1, game.getUserOne());

        assertThat(game.getGameState()).isEqualTo(GameState.DRAW);
        assertThat(game.getNextMove()).isEqualTo(game.getUserOne().getId());
    }

    @Test
    void dropPlayerDisc_throwsException_ifNoMoveIsExpected() {
        final var game = game(1, GameState.WAITING_FOR_PLAYERS);

        assertThatThrownBy(() -> this.gameService.dropPlayerDisc(game, 3, game.getUserOne()))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("The game state should be IN_PROGRESS");

    }

    @Test
    void dropPlayerDisc_throwsException_ifUserIsNotPartOfGame() {
        final var game = game(1, GameState.IN_PROGRESS);

        assertThatThrownBy(() -> this.gameService.dropPlayerDisc(game, 3, user(200)))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("The current user is not part of this game.");

    }

    @Test
    void dropPlayerDisc_throwsException_ifUserIsNull() {
        final var game = game(1, GameState.IN_PROGRESS);

        assertThatThrownBy(() -> this.gameService.dropPlayerDisc(game, 3, null))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Player was not set.");

    }

    @Test
    void dropPlayerDisc_throwsException_ifUserDoesNotHaveNextMove() {
        final var game = game(1, GameState.IN_PROGRESS);

        assertThatThrownBy(() -> this.gameService.dropPlayerDisc(game, 3, game.getUserTwo()))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("The current user is not allowed to drop a disc.");

    }

    @Test
    void gameBoardWithDisc() {
        final var game = game(1, GameState.IN_PROGRESS);

        final var board = this.gameService.gameBoardWithDisc(game, 0, 1234);

        assertBoardContains(board, 0, 0, 1234);
    }

    @Test
    void gameBoardWithDisc_throwsException_ifGameBoardIsFull() {
        final var game = game(1, GameState.IN_PROGRESS);
        game.setBoard(fullGameBoard());

        assertThatThrownBy(() -> this.gameService.gameBoardWithDisc(game, 0, 1234))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Invalid move. Column is already full.");
    }

    @Test
    void dropRandomDisc() {
        // arrange
        final var game = game(1, GameState.IN_PROGRESS);
        this.stubRepository(game);
        final var user1 = game.getUserOne();
        final var originalBoard = game.getBoard();
        final var spyGameService = spy(this.gameService);

        // act
        final var updated = spyGameService.dropRandomDisc(game.getId(), user1);

        // assert
        verify(spyGameService).dropPlayerDisc(eq(game), anyInt(), eq(user1));
        assertThat(originalBoard.asListObject()).isNotEmpty();
        assertThat(updated.getBoard().asListObject()).isNotEmpty();
        assertThat(originalBoard.asListObject()).doesNotContainSequence(updated.getBoard().asListObject());
        assertThat(updated.getBoard().contains(user1.getId())).isTrue();
        verify(this.gameRepository).save(game);
    }

    @Test
    void dropRandomDisc_onNoMoveExpected_returnsUneditedObject() {
        // arrange
        final var game = game(1, GameState.WAITING_FOR_PLAYERS);
        this.stubRepository(game);
        final var user1 = game.getUserOne();
        final var spyGameService = this.gameService;

        // act
        final var updated = spyGameService.dropRandomDisc(game.getId(), user1);

        // assert
        assertThat(updated).isEqualTo(game);
        verify(this.gameRepository, never()).save(game);
    }

    @Test
    void dropRandomDisc_throwsException_ifGameBoardIsFull() {
        // arrange
        final var game = game(1, GameState.IN_PROGRESS);
        game.setBoard(fullGameBoard());
        this.stubRepository(game);

        // act
        assertThatThrownBy(() -> this.gameService.dropRandomDisc(game.getId(), game.getUserOne()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GameState does not match GameBoard.");

    }

    @Test
    void dropRandomAnonymousDisc() {
        // arrange
        final var game = game(1, GameState.IN_PROGRESS);
        this.stubRepository(game);
        final var originalBoard = game.getBoard();
        final var spyGameService = spy(this.gameService);

        // act
        final var updated = spyGameService.dropRandomAnonymousDisc(game.getId());

        // assert
        verify(spyGameService).gameBoardWithDisc(eq(game), anyInt(), eq(GameBoard.ANONYMOUS_DISC_NUMBER));
        assertThat(originalBoard.asListObject()).isNotEmpty();
        assertThat(updated.getBoard().asListObject()).isNotEmpty();
        assertThat(originalBoard.asListObject()).doesNotContainSequence(updated.getBoard().asListObject());
        assertThat(updated.getBoard().contains(GameBoard.ANONYMOUS_DISC_NUMBER)).isTrue();
        verify(this.gameRepository).save(game);
    }

    @Test
    void dropRandomAnonymousDisc_throwsException_ifGameBoardIsFull() {
        // arrange
        final var game = game(1, GameState.IN_PROGRESS);
        game.setBoard(fullGameBoard());
        this.stubRepository(game);

        // act
        assertThatThrownBy(() -> this.gameService.dropRandomAnonymousDisc(game.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GameState does not match GameBoard.");

    }

    @Test
    void dropDisc() {
        // arrange
        final var game = game(1, GameState.IN_PROGRESS);
        this.stubRepository(game);
        final var spyGameService = spy(this.gameService);
        final var gameId = game.getId();
        final var user1 = game.getUserOne();

        // act
        final var updated = spyGameService.dropDisc(gameId, 3, user1);

        // assert
        verify(spyGameService).dropPlayerDisc(game, 3, user1);
        verify(this.gameRepository).save(game);
        assertBoardContains(updated.getBoard(), 0, 3, user1.getId());
    }

    @Test
    void setAllConnectedUsersAsDisconnected() {
        // arrange
        final var game1 = game(1, GameState.IN_PROGRESS);
        final var game2 = game(2, GameState.PAUSED);
        final var game3 = game(3, GameState.WAITING_FOR_PLAYERS);
        final var game4 = game(4, GameState.DRAW);
        final var game5 = game(5, GameState.PLAYER_LEFT);
        game5.setUserState(game4.getUserOne().getId(), QUIT);
        final var games = List.of(game1, game2, game3, game4, game5);
        when(this.gameRepository.findByUserState(CONNECTED)).thenReturn(games);

        // act
        this.gameService.setAllConnectedUsersAsDisconnected();

        // assert
        assertThat(game1.getUserOneState()).isEqualTo(DISCONNECTED);
        assertThat(game1.getUserTwoState()).isEqualTo(DISCONNECTED);
        assertThat(game2.getUserOneState()).isEqualTo(DISCONNECTED);
        assertThat(game2.getUserTwoState()).isEqualTo(DISCONNECTED);
        assertThat(game3.getUserOneState()).isEqualTo(DISCONNECTED);
        assertThat(game3.getUserTwoState()).isEqualTo(DISCONNECTED);
        assertThat(game4.getUserOneState()).isEqualTo(DISCONNECTED);
        assertThat(game4.getUserTwoState()).isEqualTo(DISCONNECTED);
        assertThat(game5.getUserOneState()).isEqualTo(QUIT);
        assertThat(game5.getUserTwoState()).isEqualTo(DISCONNECTED);
        verify(this.gameRepository).saveAll(games);

    }


    static void assertBoardContains(final GameBoard gameBoard, final int rowId, final int columnId, final int discNumber) {
        final var listBoard = gameBoard.asListObject();
        if (rowId >= listBoard.size()) {
            fail("rowId out of bounds");
        }

        final var reverseRowId = listBoard.size() - 1 - rowId;
        final var row = listBoard.get(reverseRowId);
        if (columnId >= row.size()) {
            fail("columnId out of bounds");
        }

        final var actual = row.get(columnId);

        assertThat(actual)
                .withFailMessage("Expected column " + columnId
                        + " on row " + rowId
                        + " to be " + discNumber
                        + " but was " + actual + ":\n"
                        + listBoard.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n")))
                .isEqualTo(discNumber);
    }

    private void stubRepository(final Game findGame) {
        lenient().when(this.gameRepository.save(any())).thenAnswer(invocation ->
                invocation.getArgument(0, Game.class));

        if (findGame != null) {
            when(this.gameRepository.findById(findGame.getId())).thenReturn(java.util.Optional.of(findGame));
        }
    }


    static GameRequestDto gameRequestDto(final Game game, final String message) {
        return GameRequestDto.builder().gameId(game.getId()).message(message).build();
    }

}