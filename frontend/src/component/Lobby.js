import React, {useEffect, useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useLocation, useNavigate} from 'react-router-dom';

const Lobby = ({userId}) => {
    const [games, setGames] = useState([]);
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const location = useLocation();

    useSubscription("/topic/lobby/games/create", (message) => {
        const newGame = JSON.parse(message.body);
        setGames(oldGames => [...oldGames, newGame]);

        if (newGame.creatorId == userId) {
            navigate(`/game/${newGame.gameId}`, {state: {prevPath: location.pathname}});
        }

    });

    useSubscription("/topic/lobby/games/all", (message) => {
        const allGames = JSON.parse(message.body);
        console.log("Received payload allGames:", allGames);
        setGames(allGames);
    });

    useSubscription("/topic/lobby/games/joined", (message) => {
        const joinedGame = JSON.parse(message.body);

        if (joinedGame.userTwoId == userId) {
            navigate(`/game/${joinedGame.gameId}`, {state: {prevPath: location.pathname}});
        }
    });

    useEffect(() => {
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: "/4gewinnt/games/all",
            });
        }
    }, [stompClient]);

    useEffect(() => {

        const prevPath = sessionStorage.getItem('prevPath');
        console.log("Fetched prevPath from sessionStorage:", prevPath);
        if (prevPath && prevPath.startsWith('/game/')) {
            console.log("Spiel wurde verlassen.");

            const gameId = prevPath.split("/")[2];

            if (stompClient && stompClient.connected) {
                stompClient.publish({
                    destination: "/4gewinnt/games/left",
                    body: JSON.stringify({
                        gameId: gameId,
                        userId: userId,
                        message: "Das Spiel wurde verlassen"
                    })
                });
            }
        }
    }, [location, stompClient]);

    const createGame = () => {
        if (stompClient) {
            const gameRequest = {
                action: 'create',
            };
            console.log("Requesting to create a new game");
            stompClient.publish({
                destination: "/4gewinnt/games/create",
                userId: userId,
                body: JSON.stringify(gameRequest)
            });
        }
    };

    const joinGame = (gameId) => {
        if (stompClient) {
            const joinRequest = {
                action: 'join',
                gameId: gameId.toString(),
            };
            console.log("Requesting to join game with ID:", gameId);
            stompClient.publish({
                destination: "/4gewinnt/games/join",
                userId: userId,
                body: JSON.stringify(joinRequest)
            });
        }
    };

    const deleteAllGames = () => {
        if (stompClient) {
            console.log("Requesting to delete all games");
            stompClient.publish({
                destination: "/4gewinnt/games/deleteAll"
            });
        }
    };

    return (
        <div>
            <h1>Lobby</h1>
            <button onClick={createGame}>Neues Spiel erstellen</button>
            <button onClick={deleteAllGames}>Liste löschen</button>
            <ul>
                {games.map((gameData) => (
                    <li key={gameData.gameId} onClick={() => joinGame(gameData.gameId)}>
                        Spiel ID: {gameData.gameId} - Ersteller: {gameData.creatorName} - Status: {gameData.status}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Lobby;