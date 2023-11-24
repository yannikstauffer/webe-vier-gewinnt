import React, {useEffect, useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useTranslation} from 'react-i18next';
import {useNavigate} from 'react-router-dom';
import './GameBoard.css';
import ConfirmDialog from './ConfirmDialog';

const ROWS = 6;
const COLUMNS = 7;
const EMPTY = 'E';

const createEmptyBoard = () => {
    return Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(EMPTY));
};

const GameLevel = {
    LEVEL1: "LEVEL1",
    LEVEL2: "LEVEL2",
    LEVEL3: "LEVEL3"
};

const GameBoard = ({initialGameId, userId}) => {
    const [gameId, setGameId] = useState(initialGameId);
    const [board, setBoard] = useState(createEmptyBoard());
    const [nextMove, setNextMove] = useState(null);
    const [gameBoardState, setGameBoardState] = useState('NOT_STARTED');
    const [statusMessage, setStatusMessage] = useState('');
    const [buttonState, setButtonState] = useState('start');
    const [playerOneId, setPlayerOneId] = useState(null);
    const [playerTwoId, setPlayerTwoId] = useState(null);
    const {t} = useTranslation();
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const [showConfirmDialog, setShowConfirmDialog] = useState(false);
    const [gameLevel, setGameLevel] = useState(GameLevel.LEVEL1);
    const [countdown, setCountdown] = useState(null);
    const [currentRound, setCurrentRound] = useState(0);
    const [roundsUntilSpecialDisc, setRoundsUntilSpecialDisc] = useState(generateRandomRounds());

    const onGameUpdateReceived = (message) => {
        const updatedGame = JSON.parse(message.body);
        console.log("update game:", updatedGame);
        updateGame(updatedGame);
    };

    useSubscription("/user/queue/game", onGameUpdateReceived);

    function generateRandomRounds() {
        return Math.floor(Math.random() * 7) + 3;
    }

    useEffect(() => {
        if (gameLevel === GameLevel.LEVEL2 && nextMove === userId && gameBoardState === 'MOVE_EXPECTED') {
            setCountdown(5);
            const timer = setInterval(() => {
                setCountdown(prevCountdown => {
                    if (prevCountdown === 1) {
                        clearInterval(timer);
                    }
                    return Math.max(prevCountdown - 1, 0);
                });
            }, 1000);

            return () => clearInterval(timer);
        }
    }, [gameLevel, nextMove, userId, board, gameBoardState]);

    const calculateNextMoveColumn = () => {
        const availableColumns = [];

        for (let col = 0; col < COLUMNS; col++) {
            if (board[0][col] === 0) {
                availableColumns.push(col);
            }
        }

        if (availableColumns.length > 0) {
            const randomIndex = Math.floor(Math.random() * availableColumns.length);
            return availableColumns[randomIndex];
        } else {
            return -1;
        }
    };

    const updateGame = (updatedGame) => {
        setGameId(updatedGame.gameId);
        setBoard(updatedGame.board);
        setNextMove(updatedGame.nextMove);
        setGameBoardState(updatedGame.gameBoardState);
        setStatusMessage(getGameStatusMessage(updatedGame.gameBoardState, updatedGame.nextMove));
        setGameLevel(updatedGame.gameLevel)

        if (updatedGame.userOne != null) {
            setPlayerOneId(updatedGame.userOne?.id);
        }

        if (updatedGame.userTwo != null) {
            setPlayerTwoId(updatedGame.userTwo?.id);
        }

        if (updatedGame.gameBoardState === 'READY_TO_START' || updatedGame.gameBoardState === 'NOT_STARTED') {
            setButtonState('start');
        } else if (updatedGame.gameBoardState === 'PLAYER_HAS_WON' || updatedGame.gameBoardState === 'DRAW') {
            setButtonState('restart');
        } else if (updatedGame.gameBoardState === 'READY_TO_CONTINUE') {
            setButtonState('continue')
        }
    };

    const getGameStatusMessage = (gameBoardState, nextMove) => {
        switch (gameBoardState) {
            case 'WAITING_FOR_PLAYERS':
                return t('game.state.wait');
            case 'PLAYER_HAS_WON':
                return nextMove === userId ? t('game.state.win') : t('game.state.lose');
            case 'DRAW':
                return t('game.state.draw');
            case 'MOVE_EXPECTED':
                return nextMove === userId ? t('game.state.yourTurn') : t('game.state.notYourTurn');
            case 'READY_TO_START':
                return t('game.state.ready');
            case 'PLAYER_QUIT':
                return t('game.state.quit');
            case 'PLAYER_DISCONNECTED':
                return t('game.state.disconnected');
            case 'READY_TO_CONTINUE':
                return t('game.state.continue');

        }
    };

    const handleLevelClick = (level) => {
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: gameId,
                    message: level
                }),
            });
        }
    };


    const handleButtonClick = () => {
        if (stompClient && stompClient.connected) {
            let message;
            if (gameBoardState === 'READY_TO_START' || gameBoardState === 'NOT_STARTED') {
                message = 'start';
            } else if (gameBoardState === 'READY_TO_CONTINUE') {
                message = 'continue';
            } else {
                message = 'restart'
            }
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: gameId,
                    message: message
                }),
            });
        }
    };

    const getHandleButtonText = () => {
        switch (buttonState) {
            case 'start':
                return t('game.button.newGame');
            case 'restart':
                return t('game.button.newGame');
            case 'continue':
                return t('game.button.continue');
            default:
                return t('game.button.newGame');
        }
    };

    const leaveButtonClick = () => {
        setShowConfirmDialog(true);
    };

    const confirmLeave = () => {
        setShowConfirmDialog(false);
        setBoard(createEmptyBoard());
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: gameId,
                    message: 'leave'
                }),
            });

            navigate('/lobby');
        }
    };

    const dropDisc = (column) => {
        let message = ''
        if (nextMove === userId) {
            setCurrentRound(currentRound + 1);
            console.log("current: " + currentRound + " until:" + roundsUntilSpecialDisc);

            if (gameLevel === GameLevel.LEVEL3 && currentRound >= roundsUntilSpecialDisc) {
                message = "specialDisc"
                column = calculateNextMoveColumn();

                setCurrentRound(0);
                setRoundsUntilSpecialDisc(generateRandomRounds());
            }

            stompClient.publish({
                destination: "/4gewinnt/games/action",
                body: JSON.stringify({
                    gameId: gameId,
                    column: column,
                    playerId: userId,
                    message: message
                }),
            });
        }
    };

    const isGameActive = () => {
        return gameBoardState === 'MOVE_EXPECTED' || gameBoardState === 'PLAYER_DISCONNECTED';
    };

    return (
        <div>
            <p>{t('game.level.activated')} {gameLevel}</p>
            <table>
                <tbody>
                {board.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                        {row.map((cell, colIndex) => {
                            let cellClass = 'empty';
                            if (cell !== EMPTY) {
                                if (cell === playerOneId && playerTwoId !== null) {
                                    cellClass = 'player-one';
                                } else if (cell === playerTwoId && playerOneId !== null) {
                                    cellClass = 'player-two';
                                } else if (cell === -5) {
                                    cellClass = 'special-disc'
                                }
                            }
                            return (
                                <td key={colIndex}
                                    onClick={() => isGameActive() && nextMove === userId && dropDisc(colIndex)}>
                                    <div className={`cell ${cellClass}`}></div>
                                </td>
                            );
                        })}
                    </tr>
                ))}
                </tbody>
            </table>
            <p>{statusMessage}</p>
            {countdown && <p>{t('game.level.countdown')} {countdown}</p>}
            <div className="button-container">
                <div className="button-group">
                    <button onClick={() => handleLevelClick(GameLevel.LEVEL1)}>Level 1</button>
                    <button onClick={() => handleLevelClick(GameLevel.LEVEL2)}>Level 2</button>
                    <button onClick={() => handleLevelClick(GameLevel.LEVEL3)}>Level 3</button>
                </div>
                <div className="button-group">
                    <button onClick={handleButtonClick} disabled={isGameActive()}>{getHandleButtonText()}</button>
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
