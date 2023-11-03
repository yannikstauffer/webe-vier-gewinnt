import React, { useState, useEffect } from 'react';
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { useTranslation } from 'react-i18next';
import './GameBoard.css';

const ROWS = 6;
const COLUMNS = 7;
const EMPTY = 'E';

const createEmptyBoard = () => {
    return Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(EMPTY));
};

const GameBoard = ({ gameId, userId }) => {
    const [board, setBoard] = useState(createEmptyBoard());
    const [nextMove, setNextMove] = useState(null);
    const [gameBoardState, setGameBoardState] = useState(null);
    const { t } = useTranslation();
    const stompClient = useStompClient();

    const [statusMessage, setStatusMessage] = useState('');

    useSubscription("/user/queue/game", (message) => {
        const updatedGame = JSON.parse(message.body);
        console.log("update:", updatedGame);
        setBoard(updatedGame.board);
        setNextMove(updatedGame.nextMove);
        setGameBoardState(updatedGame.gameBoardState);
    });

    useEffect(() => {
        setStatusMessage(getGameStatusMessage());
    }, [gameBoardState, nextMove]);

    const getGameStatusMessage = () => {
        switch(gameBoardState) {
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
                        {row.map((cell, colIndex) => (
                            <td key={colIndex} onClick={() => nextMove === userId && dropDisc(colIndex)}>
                                <div className={`cell ${cell === EMPTY ? 'empty' : cell === '1' ? 'player-one' : 'player-two'}`}></div>
                            </td>
                        ))}
                    </tr>
                ))}
                </tbody>
            </table>
            <p>{statusMessage}</p>
        </div>
    );
};

export default GameBoard;
