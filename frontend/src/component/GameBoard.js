import React, {useState} from 'react';
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
    const [gameBoardState, setGameBoardState] = useState('NOT_STARTED');
    const {t} = useTranslation();
    const stompClient = useStompClient();
    const [statusMessage, setStatusMessage] = useState(t('game.state.wait'));
    const [buttonState, setButtonState] = useState('start');
    const [playerOneId, setPlayerOneId] = useState(null);
    const [playerTwoId, setPlayerTwoId] = useState(null);

    const updateGame = (updatedGame) => {
        setBoard(updatedGame.board);
        setNextMove(updatedGame.nextMove);
        setGameBoardState(updatedGame.gameBoardState);

        if (!playerOneId || !playerTwoId) {
            setPlayerOneId(updatedGame.userOne?.id);
            setPlayerTwoId(updatedGame.userTwo?.id);
        }

        setStatusMessage(getGameStatusMessage(updatedGame.gameBoardState, updatedGame.nextMove));

        if (updatedGame.gameBoardState === 'READY_TO_START' || updatedGame.gameBoardState === 'NOT_STARTED') {
            setButtonState('start');
        } else if (updatedGame.gameBoardState === 'PLAYER_HAS_WON' || updatedGame.gameBoardState === 'DRAW') {
            setButtonState('newGame');
        }
    };

    useSubscription("/user/queue/game", (message) => {
        const updatedGame = JSON.parse(message.body);
        console.log("update:", updatedGame);
        updateGame(updatedGame);
    });

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

    const getButtonText = () => {
        switch (buttonState) {
            case 'start':
                return t('game.button.newGame');
            case 'newGame':
                return t('game.button.newGame');
            default:
                return t('game.button.newGame');
        }
    };

    const handleButtonClick = () => {
        if (gameBoardState === 'READY_TO_START' || gameBoardState === 'PLAYER_HAS_WON' || gameBoardState === 'DRAW') {
            setBoard(createEmptyBoard());
            setGameBoardState('NOT_STARTED');
        }

        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: `/4gewinnt/games/control`,
                body: JSON.stringify({
                    gameId: gameId,
                    message: buttonState
                }),
            });
        }
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
            <button onClick={handleButtonClick} disabled={isGameActive()}>{getButtonText()}</button>
        </div>
    );
};

export default GameBoard;
