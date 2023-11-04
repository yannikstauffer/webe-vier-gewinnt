import React, {useEffect, useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useTranslation} from 'react-i18next';
import './GameBoard.css';

const ROWS = 6;
const COLUMNS = 7;
const EMPTY = 'E';

const createEmptyBoard = () => {
    return Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(EMPTY));
};

const GameBoard = ({gameId, userId}) => {
    const [board, setBoard] = useState(createEmptyBoard());
    const [nextMove, setNextMove] = useState(null);
    const [gameBoardState, setGameBoardState] = useState(null);
    const {t} = useTranslation();
    const stompClient = useStompClient();
    const [statusMessage, setStatusMessage] = useState('');
    const [buttonState, setButtonState] = useState('start');
    const [playerOneId, setPlayerOneId] = useState(null);
    const [playerTwoId, setPlayerTwoId] = useState(null);

    useSubscription("/user/queue/game", (message) => {
        const updatedGame = JSON.parse(message.body);
        console.log("update:", updatedGame);
        setBoard(updatedGame.board);
        setNextMove(updatedGame.nextMove);
        setGameBoardState(updatedGame.gameBoardState);

        if (!playerOneId || !playerTwoId) {
            setPlayerOneId(updatedGame.userOne?.id);
            setPlayerTwoId(updatedGame.userTwo?.id);
        }

        if (updatedGame.gameBoardState === 'READY_TO_START') {
            setButtonState('start');
        }

        setBoard(updatedGame.board);
    });

    useEffect(() => {
        setStatusMessage(getGameStatusMessage());

        // Hier setzen wir den initialen Button-Zustand basierend auf dem initialen Spielzustand
        if (gameBoardState === 'READY_TO_START' || gameBoardState === 'NOT_STARTED') {
            setButtonState('start');
        } else if (gameBoardState === 'PLAYER_HAS_WON' || gameBoardState === 'DRAW') {
            setButtonState('newGame');
        }
    }, [gameBoardState, nextMove]);

    const getGameStatusMessage = () => {
        switch (gameBoardState) {
            case 'PLAYER_HAS_WON':
                return nextMove === userId ? t('game.state.win') : t('game.state.lose');
            case 'DRAW':
                return t('game.state.draw');
            case 'MOVE_EXPECTED':
                return nextMove === userId ? t('game.state.yourTurn') : t('game.state.notYourTurn');
            default:
                return 'game.state.wait';
        }
    };

    const getButtonText = () => {
        switch (gameBoardState) {
            case 'PLAYER_HAS_WON':
            case 'DRAW':
            case 'READY_TO_START':
                return t('game.button.newGame');
            case 'MOVE_EXPECTED':
                return t('game.button.break');
            case 'NOT_STARTED':
            default:
                return t('game.button.newGame');
        }
    };

    const handleButtonClick = () => {
        let actionName;

        if (gameBoardState === 'READY_TO_START' || gameBoardState === 'PLAYER_HAS_WON' || gameBoardState === 'DRAW') {
            actionName = 'start';
            setBoard(createEmptyBoard()); // Setzen Sie das Spielbrett zurück, wenn ein neues Spiel gestartet wird
        } else if (gameBoardState === 'MOVE_EXPECTED') {
            actionName = 'pause';
        }

        // Nur Nachrichten senden und den Zustand ändern, wenn der Button aktiv sein sollte
        if (gameBoardState !== 'NOT_STARTED') {
            if (stompClient && stompClient.connected) {
                stompClient.publish({
                    destination: `/4gewinnt/games/control`,
                    body: JSON.stringify({
                        gameId: gameId,
                        message: actionName
                    }),
                });
            }
        }
    };

    const dropDisc = (column) => {
        stompClient.publish({
            destination: "/4gewinnt/games/action",
            body: JSON.stringify({
                gameId: gameId,
                column: column,
                playerId: userId,
            }),
        });
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
                                <td key={colIndex} onClick={() => nextMove === userId && dropDisc(colIndex)}>
                                    <div className={`cell ${cellClass}`}></div>
                                </td>
                            );
                        })}
                    </tr>
                ))}
                </tbody>
            </table>
            <p>{statusMessage}</p>
            <button onClick={handleButtonClick} disabled={gameBoardState === 'NOT_STARTED'}>{getButtonText()}
            </button>
        </div>
    );
};

export default GameBoard;
