import React, {useState} from 'react';
import './GameBoard.css';

const ROWS = 6;
const COLUMNS = 7;
const EMPTY = 'E';
const PLAYER_ONE = '1';
const PLAYER_TWO = '2';

const createEmptyBoard = () => {
    return Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(EMPTY));
};

const GameBoard = () => {
    const [board, setBoard] = useState(createEmptyBoard);
    const [currentPlayer, setCurrentPlayer] = useState(PLAYER_ONE);

    const dropDisc = (column) => {
        for (let row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] === EMPTY) {
                const newBoard = [...board];
                newBoard[row][column] = currentPlayer;
                setBoard(newBoard);
                setCurrentPlayer(currentPlayer === PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE);
                return;
            }
        }
    };

    return (
        <div>
            <table>
                <tbody>
                {board.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                        {row.map((cell, colIndex) => (
                            <td key={colIndex} onClick={() => dropDisc(colIndex)}>
                                <div
                                    className={cell === EMPTY ? 'empty' : cell === PLAYER_ONE ? 'player-one' : 'player-two'}></div>
                            </td>
                        ))}
                    </tr>
                ))}
                </tbody>
            </table>
            <p>Current Player: {currentPlayer === PLAYER_ONE ? 'Player 1' : 'Player 2'}</p>
        </div>
    );
};

export default GameBoard;
