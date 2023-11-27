import React, {useRef, useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useLocation, useNavigate} from 'react-router-dom';
import {useTranslation} from "react-i18next";
import './Lobby.css';
import {GameState} from "../model/GameModel";

class GameDto {
    constructor({id, gameState, userOne, userTwo, userOneState, userTwoState}) {
        this.id = id;
        this.gameState = gameState;
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.userOneState = userOneState;
        this.userTwoState = userTwoState;
    }

    containsUser(userId) {
        return this.userOne?.id === userId || this.userTwo?.id === userId;
    }

    isReadyToStart() {
        return this.userOne && this.userTwo && this.gameState === GameState.WAITING_FOR_PLAYERS;
    }

    isVisible(userId) {
        return this.gameState === GameState.IN_PROGRESS ||
            this.gameState === GameState.PAUSED ||
            this.gameState === GameState.WAITING_FOR_PLAYERS ||
            this.hasQuitButStillRejoinable(userId);
    }

    hasQuitButStillRejoinable(userId) {
        return this.gameState === GameState.PLAYER_LEFT && this.isConnected(this.otherUserId(userId));
    }

    isConnected(userId) {
        return (this.userOne?.id === userId && this.userOneState === 'CONNECTED') ||
            (this.userTwo?.id === userId && this.userTwoState === 'CONNECTED');
    }

    otherUserId(userId) {
        return this.userOne?.id === userId ? this.userTwo?.id : this.userOne?.id;
    }

    hasQuit(userId) {
        return (this.userOne?.id === userId && this.userOneState === 'QUIT') ||
            (this.userTwo?.id === userId && this.userTwoState === 'QUIT');
    }

    static ofGames(games) {
        return games.map(game => new GameDto(game));
    }

    static ofGame(game) {
        return new GameDto(game);
    }
}

const Lobby = ({userId}) => {
    const {t} = useTranslation();

    const [games, setGames] = useState([]);
    const lobbyGameTimeoutRef = useRef(null);
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const location = useLocation();
    let attemptingToJoinGameId = null;
    let isCreatingGame = false;

    const filterGamesByState = (games) => {
        return games.filter(game => game.isVisible(userId));
    };

    const onGameClick = (gameId) => {
        attemptingToJoinGameId = gameId;
        joinGame(gameId);
    };

    const onGamesReceived = (message) => {
        const updatedGameDtos = GameDto.ofGames(JSON.parse(message.body));
        const updatedGames = filterGamesByState(updatedGameDtos);
        console.log("update /user/queue/games:", updatedGames);
        setGames(updatedGames);
    };

    const onLobbyGameReceived = (message) => {
        if (!lobbyGameTimeoutRef.current) {
            lobbyGameTimeoutRef.current = setTimeout(() => {
                const updatedGame = GameDto.ofGame(JSON.parse(message.body));
                console.log("update /topic/lobby/games:", updatedGame);

                if (isCreatingGame && !attemptingToJoinGameId) {
                    onGameCreated(updatedGame.id);
                }

                setGames((oldGames) => {
                    const existingGameIndex = oldGames.findIndex(game => game.id === updatedGame.id);

                    if (existingGameIndex !== -1) {
                        const newGames = [...oldGames];
                        newGames[existingGameIndex] = updatedGame;
                        return filterGamesByState(newGames);
                    } else {
                        return filterGamesByState([...oldGames, updatedGame]);
                    }
                });

                if ((isCurrentUserPartOf(updatedGame)) &&
                    updatedGame.id === attemptingToJoinGameId) {
                    navigate(`/game/${updatedGame.id}`, {state: {prevPath: location.pathname}});
                    attemptingToJoinGameId = null;
                }

                lobbyGameTimeoutRef.current = null;
            }, 20);


        }
    };

    const isCurrentUserPartOf = (game) => {
        return game.userOne?.id === userId || game.userTwo?.id === userId;
    }

    useSubscription("/user/queue/games", onGamesReceived);
    useSubscription("/topic/lobby/games", onLobbyGameReceived);

    const createGame = () => {
        isCreatingGame = true;
        if (stompClient) {
            stompClient.publish({destination: "/4gewinnt/games/create"});
        }
    };

    const onGameCreated = (gameId) => {
        if (isCreatingGame) {
            attemptingToJoinGameId = gameId;
            isCreatingGame = false;
            joinGame(gameId);
            navigate(`/game/${gameId}`, {state: {prevPath: location.pathname}});
        }
    };

    const joinGame = (gameId) => {
        if (stompClient) {
            stompClient.publish({
                destination: "/4gewinnt/games/join",
                body: JSON.stringify({gameId}),
            });
        }
    };

    const getStateLabel = (game) => {
        if (game.isReadyToStart()) {
            return t("game.state.ready");
        } else if (game.gameState === GameState.IN_PROGRESS) {
            return t("game.state.inProgress");
        } else if (game.gameState === GameState.PAUSED || game.hasQuitButStillRejoinable(userId)) {
            return t("game.state.paused");
        } else if (game.gameState === GameState.WAITING_FOR_PLAYERS) {
            return t("game.state.wait");
        } else if (game.gameState === GameState.PLAYER_HAS_WON || game.gameState === GameState.DRAW) {
            return t("game.state.finished");
        } else if (game.gameState === GameState.PLAYER_LEFT) {
            return t("game.state.finished");
        }
    }

    const isJoinDisabled = (game) => {
        return game.gameState !== GameState.WAITING_FOR_PLAYERS
            && !(game.gameState === GameState.PAUSED && isCurrentUserPartOf(game))
            && !(game.hasQuitButStillRejoinable(userId));
    }

    return (
        <div className="lobby-layout">
            <h2>Lobby</h2>
            <button onClick={createGame}>Neues Spiel erstellen</button>
            <ul className="lobby-list">
                {games.map((game) => (
                    <li className="lobby-game" key={game.id}>
                        <label>{t("game.title")} {game.id}</label>
                        <div>{getStateLabel(game)}</div>
                        <label>{t("game.label.playerOne")}</label>
                        <div>{game.userOne?.firstName}</div>
                        <label>{t("game.label.playerTwo")}</label>
                        <div>{game.userTwo?.firstName}</div>
                        <button className="button join-button"
                                onClick={() => onGameClick(game.id)}
                                disabled={isJoinDisabled(game)}>
                            {t("game.button.joinGame")}
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Lobby;
