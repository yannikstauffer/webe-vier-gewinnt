import React, {useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useTranslation} from 'react-i18next';
import {useNavigate} from 'react-router-dom';
import './GameBoard.css';
import ConfirmDialog from './ConfirmDialog';
import {GameModel, GameLevel} from "../model/GameModel";

const ROWS = 6;
const COLUMNS = 7;
const EMPTY = 0;
const ANONYMOUS_DISC = -5;

const createEmptyBoard = () => {
    return Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(EMPTY));
};

const roundsUntilSpecialDisc = 5;

const GameBoard = ({initialGameId, userId}) => {
    const {t} = useTranslation();
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const [showConfirmDialog, setShowConfirmDialog] = useState(false);
    const [countdown, setCountdown] = useState(null);
    const [game, setGame] = useState(new GameModel({
        gameId: initialGameId,
        board: createEmptyBoard(),
        nextMove: null,
        gameLevel: GameLevel.LEVEL1,
        gameState: 'WAITING_FOR_PLAYERS',
        userOne: null,
        userOneState: null,
        userTwo: null,
        userTwoState: null
    }));

    const onGameUpdateReceived = (message) => {
        const updatedGame = JSON.parse(message.body);
        console.log("update game:", updatedGame);
        updateGame(updatedGame);
    };

    useSubscription("/user/queue/game", onGameUpdateReceived);


    const updateGame = (updatedGame) => {
        const gameModel = new GameModel(updatedGame);
        setGame(gameModel);
        handleLevel2(gameModel);
    };

    const handleLevel2 = (updatedGame) => {
        if (updatedGame.gameLevel === GameLevel.LEVEL2 && updatedGame.hasNextMove(userId)) {
            setCountdown(5);
            const timer = setInterval(() => {
                setCountdown(prevCountdown => {
                    if (prevCountdown === 1) {
                        clearInterval(timer);
                    }
                    const nextCountdown = Math.max(prevCountdown - 1, 0);
                    return nextCountdown === 0 ? null : nextCountdown;
                });
            }, 1000);

            return () => clearInterval(timer);
        }
    }

    const getGameStatusMessage = () => {
        if (game.isWaitingForPlayers()) {
            return t('game.state.wait');
        } else if (game.isReadyToStart()) {
            return t('game.state.ready');
        } else if (game.isMoveExpected()) {
            return game.hasNextMove(userId) ? t('game.state.yourTurn') : t('game.state.notYourTurn')
        } else if (game.isPaused()) {
            return t('game.state.disconnected');
        } else if (game.isReadyToContinue()) {
            return t('game.state.continue');
        } else if (game.playerHasLeft()) {
            return t('game.state.quit');
        } else if (game.playerHasWon()) {
            return game.isWinner(userId) ? t('game.state.win') : t('game.state.lose');
        } else if (game.isDraw()) {
            return t('game.state.draw');
        } else {
            return '';
        }
    };

    const handleLevelClick = (level) => {
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: game.gameId,
                    message: level
                }),
            });
        }
    };

    const handleButtonClick = () => {
        let message;
        if (game.isReadyToStart()) {
            message = 'start';
        } else if (game.isReadyToContinue()) {
            message = 'continue';
        } else if (game.isFinished()) {
            message = 'restart'
        }

        if (message && stompClient && stompClient.connected) {
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: game.gameId,
                    message: message
                }),
            });
        }
    };

    const getHandleButtonText = () => {
        if (game.isReadyToStart()) {
            return t('game.button.newGame');
        } else if (game.isFinished()) {
            return t('game.button.newGame');
        } else if (game.isReadyToContinue()) {
            return t('game.button.continue');
        }

        return t('game.button.newGame');
    };

    const leaveButtonClick = () => {
        setShowConfirmDialog(true);
    };

    const confirmLeave = () => {
        setShowConfirmDialog(false);
        game.board = createEmptyBoard();
        setGame(game);
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: game.gameId,
                    message: 'leave'
                }),
            });

            navigate('/lobby');
        }
    };

    const dropDisc = (column) => {
        if (game.hasNextMove(userId) && stompClient && stompClient.connected) {

            stompClient.publish({
                destination: "/4gewinnt/games/action",
                body: JSON.stringify({
                    gameId: game.gameId,
                    column: column,
                    playerId: userId
                }),
            });

        }
    };

    const getLevel3RoundsCount = () => {
        return roundsUntilSpecialDisc - (getPlayerDiscCount() % roundsUntilSpecialDisc);
    }
    const getPlayerDiscCount = () => {
        return game.board.flat().filter(cell => (cell !== ANONYMOUS_DISC && cell !== 0)).length;
    }

    const getCellClass = (cell) => {
        const baseClass = 'cell';
        if (cell === EMPTY) {
            return baseClass + ' empty'
        }
        if (cell === game.userOne?.id && game.userTwo !== null) {
            return baseClass + ' player-one';
        }
        if (cell === game.userTwo?.id && game.userOne !== null) {
            return baseClass + ' player-two';
        }
        if (cell === ANONYMOUS_DISC) {
            return baseClass + ' special-disc'
        }
    }

    const getLevelButtonClass = (gameLevel) => {
        return game.gameLevel === gameLevel ? 'selected' : '';
    }

    return (
        <div>
            <p>{t('game.level.activated')} {game.gameLevel}</p>
            <table>
                <tbody>
                {game.board.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                        {row.map((cell, colIndex) => {
                            return (
                                <td key={colIndex}
                                    onClick={() => dropDisc(colIndex)}>
                                    <div className={getCellClass(cell)}></div>
                                </td>
                            );
                        })}
                    </tr>
                ))}
                </tbody>
            </table>
            <p>{getGameStatusMessage()}</p>
            {countdown && <p>{t('game.level.countdown')} {countdown}</p>}
            {game.gameLevel === GameLevel.LEVEL3 && <p>{t('game.level.rounds')} {getLevel3RoundsCount()}</p>}
            <div className="button-container">
                {game.isNotYetStarted() &&
                    <div className="button-group">
                        <button className={getLevelButtonClass(GameLevel.LEVEL1)} onClick={() => handleLevelClick(GameLevel.LEVEL1)}>Level 1</button>
                        <button className={getLevelButtonClass(GameLevel.LEVEL2)} onClick={() => handleLevelClick(GameLevel.LEVEL2)}>Level 2</button>
                        <button className={getLevelButtonClass(GameLevel.LEVEL3)} onClick={() => handleLevelClick(GameLevel.LEVEL3)}>Level 3</button>
                    </div>
                }
                <div className="button-group">
                    <button onClick={handleButtonClick}
                            disabled={!(game.isReadyToContinue() || game.isReadyToStart())}>{getHandleButtonText()}</button>
                    <button onClick={leaveButtonClick}>{t('game.button.quitGame')}</button>
                </div>
            </div>
            <ConfirmDialog
                open={showConfirmDialog}
                onClose={() => setShowConfirmDialog(false)}
                onConfirm={confirmLeave}
            />
        </div>
    );
};

export default GameBoard;
