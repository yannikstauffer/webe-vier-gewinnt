import React, { useEffect, useState } from 'react';
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import './GameBoard.css';
import ConfirmDialog from './ConfirmDialog';

const ROWS = 6;
const COLUMNS = 7;
const EMPTY = 'E';

const createEmptyBoard = () => {
    return Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(EMPTY));
};

const GameBoard = ({ initialGameId, userId }) => {
    const [gameId, setGameId] = useState(initialGameId);
    const [board, setBoard] = useState(createEmptyBoard());
    const [nextMove, setNextMove] = useState(null);
    const [gameBoardState, setGameBoardState] = useState('NOT_STARTED');
    const [statusMessage, setStatusMessage] = useState('');
    const [playerOneId, setPlayerOneId] = useState(null);
    const [playerTwoId, setPlayerTwoId] = useState(null);
    const { t } = useTranslation();
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const [showConfirmDialog, setShowConfirmDialog] = useState(false);

    const onGameUpdateReceived = (message) => {
        const updatedGame = JSON.parse(message.body);
        updateGame(updatedGame);
    };

    useSubscription("/user/queue/game", onGameUpdateReceived);

    const updateGame = (updatedGame) => {
        setGameId(updatedGame.gameId);
        setBoard(updatedGame.board);
        setNextMove(updatedGame.nextMove);
        setGameBoardState(updatedGame.gameBoardState);
        setStatusMessage(getGameStatusMessage(updatedGame.gameBoardState, updatedGame.nextMove));
        setPlayerOneId(updatedGame.userOne?.id);
        setPlayerTwoId(updatedGame.userTwo?.id);
    };

    const getGameStatusMessage = (gameBoardState, nextMove) => {
        switch (gameBoardState) {
            case 'PLAYER_HAS_WON':
                return nextMove === userId ? t('game.state.win') : t('game.state.lose');
            case 'DRAW':
                return t('game.state.draw');
            case 'MOVE_EXPECTED':
                return nextMove === userId ? t('game.state.yourTurn') : t('game.state.notYourTurn');
            default:
                return t('game.state.wait');
        }
    };

    const handleButtonClick = () => {
        if (stompClient && stompClient.connected) {
            let message;
            if (gameBoardState === 'READY_TO_START' || gameBoardState === 'NOT_STARTED') {
                message = 'start';
            } else {
                message = 'restart';
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

    useEffect(() => {
        // Clean-up beim Verlassen der Komponente
        return () => {
            if (stompClient && stompClient.connected) {
                stompClient.publish({
                    destination: `/4gewinnt/games/control`,
                    body: JSON.stringify({
                        gameId: gameId,
                        message: 'left'
                    }),
                });
            }
        };
    }, [stompClient, gameId]);

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
        }
        navigate('/lobby');
    };

    const dropDisc = (column) => {
        if (nextMove === userId) {
            stompClient.publish({
                destination: "/4gewinnt/games/action",
                body: JSON.stringify({
                    gameId: gameId,
                    column: column,
                    playerId: userId,
                }),
            });
        }
    };

    const isGameActive = () => {
        return gameBoardState === 'MOVE_EXPECTED';
    };

    return (
        <div>
            <table>
                <tbody>
                {board.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                        {row.map((cell, colIndex) => {
                            let cellClass = 'empty';
                            if (cell !== EMPTY) {
                                if (cell === playerOneId) {
                                    cellClass = 'player-one';
                                } else if (cell === playerTwoId) {
                                    cellClass = 'player-two';
                                }
                            }
                            return (
                                <td key={colIndex} onClick={() => isGameActive() && nextMove === userId && dropDisc(colIndex)}>
                                    <div className={`cell ${cellClass}`}></div>
                                </td>
                            );
                        })}
                    </tr>
                ))}
                </tbody>
            </table>
            <p>{statusMessage}</p>
            <button onClick={handleButtonClick} disabled={isGameActive()}>{t('game.button.control')}</button>
            <button onClick={leaveButtonClick}>{t('game.button.leaveGame')}</button>

            <ConfirmDialog
                open={showConfirmDialog}
                onClose={() => setShowConfirmDialog(false)}
                onConfirm={confirmLeave}
            />
        </div>
    );
};

export default GameBoard;
