package ch.ffhs.webe.hs2023.viergewinnt.game.level;

import ch.ffhs.webe.hs2023.viergewinnt.game.GameMessagesProxy;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static ch.ffhs.webe.hs2023.viergewinnt.game.GameBoardTest.gameBoard;
import static ch.ffhs.webe.hs2023.viergewinnt.game.level.LevelService.LEVEL_THREE_ROUND_COUNT;
import static ch.ffhs.webe.hs2023.viergewinnt.game.model.GameTest.game;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL1;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL2;
import static ch.ffhs.webe.hs2023.viergewinnt.game.values.GameLevel.LEVEL3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LevelServiceTest {

    @Mock
    TimedActionScheduler timedActionScheduler;
    @Mock
    GameMessagesProxy gameMessagesProxy;
    @Mock
    GameService gameService;

    @InjectMocks
    LevelService levelService;

    @Test
    void applyLevelModifications_withLevel1_noExecution() {
        // arrange
        final var game = mock(Game.class);
        when(game.isMoveExpected()).thenReturn(true);
        when(game.getGameLevel()).thenReturn(LEVEL1);

        // act
        final var result = this.levelService.applyLevelModifications(game);

        // assert
        assertThat(result).isEmpty();
        verifyNoInteractions(this.timedActionScheduler, this.gameMessagesProxy, this.gameService);
    }

    @Test
    void applyLevelModifications_withGameNoMoveExpected_noExecution() {
        // arrange
        final var game = mock(Game.class);
        when(game.isMoveExpected()).thenReturn(false);

        // act
        final var result = this.levelService.applyLevelModifications(game);

        // assert
        assertThat(result).isEmpty();
        verifyNoInteractions(this.timedActionScheduler, this.gameMessagesProxy, this.gameService);
    }

    @Test
    void applyLevelModifications_withLevel2() {
        // arrange
        final var game = game(2);
        game.setGameLevel(LEVEL2);

        // act
        final var result = this.levelService.applyLevelModifications(game);

        // assert
        assertThat(result).isEmpty();
        verify(this.gameService, times(0)).dropRandomAnonymousDisc(game.getId());
        verify(this.timedActionScheduler, times(1)).schedule(any());
    }

    @Test
    void applyLevelModifications_withLevel3() {
        // arrange
        final var game = game(3);
        game.setGameLevel(LEVEL3);
        game.setBoard(gameBoard(5));
        final var gameWithRandomDisc = game(30);
        when(this.gameService.dropRandomAnonymousDisc(game.getId())).thenReturn(gameWithRandomDisc);

        // act
        final var result = this.levelService.applyLevelModifications(game);

        // assert
        assertThat(result).contains(gameWithRandomDisc);
        verify(this.gameService, times(1)).dropRandomAnonymousDisc(game.getId());
        verify(this.timedActionScheduler, times(0)).schedule(any());
    }

    @ParameterizedTest
    @MethodSource("applyLevelModifications_withLevel3AndIgnoredRoundCount_returnsEmptyArguments")
    void applyLevelModifications_withLevel3AndIgnoredRoundCount_returnsEmpty(final int playerDiscCount) {
        // arrange
        final var game = game(3);
        game.setGameLevel(LEVEL3);
        game.setBoard(gameBoard(playerDiscCount));

        // act
        final var result = this.levelService.applyLevelModifications(game);

        // assert
        assertThat(result).isEmpty();
        verify(this.gameService, times(0)).dropRandomAnonymousDisc(game.getId());
        verify(this.timedActionScheduler, times(0)).schedule(any());
    }

    static List<Arguments> applyLevelModifications_withLevel3AndIgnoredRoundCount_returnsEmptyArguments() {
        return IntStream.range(0, 100)
                .filter(playerDiscCount -> playerDiscCount % LEVEL_THREE_ROUND_COUNT != 0)
                .mapToObj(Arguments::of)
                .toList();
    }


}