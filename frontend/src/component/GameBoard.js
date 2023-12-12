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
    const [showDialog, setShowDialog] = useState({
        show: false,
        text: null,
        onConfirm: null,
        showButtons: true
    });
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

    const [previousBoard, setPreviousBoard] = useState(createEmptyBoard());
    const [lastDroppedDisc, setLastDroppedDisc] = useState({ row: null, col: null });


    const onGameUpdateReceived = (message) => {
        const updatedGame = JSON.parse(message.body);
        console.log("update game:", updatedGame);
        updateGame(updatedGame);
    };

    useSubscription("/user/queue/game", onGameUpdateReceived);


    const updateGame = (updatedGame) => {
        const gameModel = new GameModel(updatedGame);
        const newBoard = gameModel.board;

        for (let row = 0; row < ROWS; row++) {
            for (let col = 0; col < COLUMNS; col++) {
                if (newBoard[row][col] !== previousBoard[row][col]) {
                    setLastDroppedDisc({ row, col });
                    break;
                }
            }
        }

        setPreviousBoard(newBoard);
        setGame(gameModel);
        handleLevel2(gameModel);

        if (gameModel.playerHasLeft() && !gameModel.bothUsersAreConnected()) {
            popupDialog(t('game.confirm.playerLeft'), confirmLeave);
        }
    };

    const handleLevel2 = (updatedGame) => {
        if (updatedGame.gameLevel === GameLevel.LEVEL2) {
            if (updatedGame.hasNextMove(userId)) {
                setCountdown(5);
                const timer = setInterval(() => {
                    setCountdown(prevCountdown => {
                        if (prevCountdown <= 1 || prevCountdown === null) {
                            clearInterval(timer);
                        }
                        const nextCountdown = Math.max(prevCountdown - 1, 0);
                        return nextCountdown === 0 ? null : nextCountdown;
                    });
                }, 1000);

                return () => clearInterval(timer);
            } else {
                setCountdown(null);
            }
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

    const popupDialog = (text, onConfirm) => {
        setShowDialog({...showDialog, text: text, show: true, onConfirm: onConfirm});
    };

    const confirmLeave = () => {
        closeDialog();
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

    const getCellClass = (cell, rowIndex, colIndex) => {
        let classes = 'cell';

        if (cell === EMPTY) {
            classes += ' empty';
        } else {
            if (cell === game.userOne?.id && game.userTwo !== null) {
                classes += ' player-one';
            } else if (cell === game.userTwo?.id && game.userOne !== null) {
                classes += ' player-two';
            } else if (cell === ANONYMOUS_DISC) {
                classes += ' special-disc';
            }

            if (lastDroppedDisc && lastDroppedDisc.row === rowIndex && lastDroppedDisc.col === colIndex) {
                classes += ' falling';
            }
        }

        return classes;
    }

    const getLevelButtonClass = (gameLevel) => {
        return game.gameLevel === gameLevel ? 'selected-button' : '';
    }

    const closeDialog = () => {
        setShowDialog({...showDialog, show: false});
    }

    const leaveGame = () => {
        if(game.isNotYetStarted() && !game.isReadyToStart()) {
            confirmLeave();
        } else {
            popupDialog(t('game.confirm.quit'), confirmLeave)
        }
    }

    const rulesDom = () => {
        return (<div className="flex-column rules">
            <h3>{t("game.rules.title")}</h3>
            <ul>
                <li>{t("game.rules.description.lineOne")}</li>
                <li>{t("game.rules.description.lineTwo")}</li>
                <li>{t("game.rules.description.lineThree")}</li>
                <li>{t("game.rules.description.lineFour")}</li>
                <li>{t("game.rules.description.lineFive")}</li>
            </ul>
            <ul>
                <li><strong>{t("game.rules.levelOne.title")}: </strong>
                    {t("game.rules.levelOne.text")}</li>
                <li><strong>{t("game.rules.levelTwo.title")}: </strong>
                    {t("game.rules.levelTwo.text")}</li>
                <li><strong>{t("game.rules.levelThree.title")}: </strong>
                    {t("game.rules.levelThree.text")}</li>
            </ul>
        </div>);
    }

    return (
        <div>
            <div>
                <ConfirmDialog
                    open={showDialog.show}
                    text={showDialog.text}
                    onClose={() => closeDialog()}
                    onConfirm={showDialog.onConfirm}
                />
                <table hidden={game.isNotYetStarted()}>
                    <tbody>
                    {game.board.map((row, rowIndex) => (
                        <tr key={rowIndex}>
                            {row.map((cell, colIndex) => {
                                return (
                                    <td key={colIndex}
                                        onClick={() => dropDisc(colIndex)}>
                                        <div className={getCellClass(cell, rowIndex, colIndex)}></div>
                                    </td>
                                );
                            })}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <p className="heavy">{getGameStatusMessage()}</p>
            {!game.isNotYetStarted()
                && <p className="heavy">{t('game.level.activated')} {game.gameLevel}</p> }
            {!game.isNotYetStarted()
                && game.gameLevel === GameLevel.LEVEL2
                && countdown && <p className="heavy">{t('game.level.countdown')} {countdown}</p>}
            {!game.isNotYetStarted()
                && game.gameLevel === GameLevel.LEVEL3
                && <p className="heavy">{t('game.level.rounds')} {getLevel3RoundsCount()}</p>}
            <div className="flex-column">
                {game.isNotYetStarted() &&
                    <div className="flex-row">
                        <button className={getLevelButtonClass(GameLevel.LEVEL1)}
                                onClick={() => handleLevelClick(GameLevel.LEVEL1)}>Level 1
                        </button>
                        <button className={getLevelButtonClass(GameLevel.LEVEL2)}
                                onClick={() => handleLevelClick(GameLevel.LEVEL2)}>Level 2
                        </button>
                        <button className={getLevelButtonClass(GameLevel.LEVEL3)}
                                onClick={() => handleLevelClick(GameLevel.LEVEL3)}>Level 3
                        </button>
                    </div>
                }
                <div className="flex-row">
                    <button onClick={handleButtonClick}
                            disabled={!(game.isReadyToContinue() || game.isReadyToStart())}>{getHandleButtonText()}</button>
                    <button onClick={() => popupDialog(rulesDom(), null)}>{t('game.button.rules')}</button>
                    <button
                        onClick={leaveGame}>{t('game.button.quitGame')}</button>
                </div>
            </div>
        </div>
    );
};

export default GameBoard;
