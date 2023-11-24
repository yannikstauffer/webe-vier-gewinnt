import React, {useState, useRef} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useLocation, useNavigate} from 'react-router-dom';
import {useTranslation} from "react-i18next";
import './Lobby.css';

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
        return games.filter(game =>
            game.gameState === 'IN_PROGRESS' ||
            game.gameState === 'PAUSED' ||
            game.gameState === 'WAITING_FOR_PLAYERS'
        );
    };

    const onGameClick = (gameId) => {
        attemptingToJoinGameId = gameId;
        joinGame(gameId);
    };

    const onGamesReceived = (message) => {
        const updatedGames = filterGamesByState(JSON.parse(message.body));
        console.log("update /user/queue/games:", updatedGames);
        setGames(updatedGames);
    };

    const onLobbyGameReceived = (message) => {
        if (!lobbyGameTimeoutRef.current) {
            lobbyGameTimeoutRef.current = setTimeout(() => {
                const updatedGame = JSON.parse(message.body);
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
        if (game.gameState === 'IN_PROGRESS') {
            return t("game.state.inProgress");
        } else if (game.gameState === 'PAUSED') {
            return t("game.state.paused");
        } else if (game.gameState === 'WAITING_FOR_PLAYERS') {
            return t("game.state.wait");
        } else if (game.gameState === 'FINISHED') {
            return t("game.state.finished");
        }
    }

    const isJoinDisabled = (game) => {
        return game.gameState !== 'WAITING_FOR_PLAYERS'
        && !(game.gameState === 'PAUSED' && isCurrentUserPartOf(game))
    }

    return (
        <div>
            <h2>Lobby</h2>
            <button onClick={createGame}>Neues Spiel erstellen</button>
            <ul className="lobby-list">
                {games.map((game) => (
                    <li className="lobby-game" key={game.id} onClick={() => onGameClick(game.id)}>
                        <label>{t("game.title")} {game.id}</label><div>{getStateLabel(game)}</div>
                        <label>{t("game.label.playerOne")}</label><div>{game.userOne?.firstName}</div>
                        <label>{t("game.label.playerTwo")}</label><div>{game.userTwo?.firstName}</div>
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
