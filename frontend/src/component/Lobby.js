import React, {useEffect, useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";

const Lobby = ({userId}) => {
    const [games, setGames] = useState([]);
    const stompClient = useStompClient();

    useSubscription("/topic/lobby/games/create", (message) => {
        const newGame = JSON.parse(message.body);
        console.log("Received payload newGame:", newGame);
        setGames(oldGames => [...oldGames, newGame]);
    });

    useSubscription("/topic/lobby/games/all", (message) => {
        const allGames = JSON.parse(message.body);
        console.log("Received payload allGames:", allGames);
        setGames(allGames);
    });

    useEffect(() => {
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: "/4gewinnt/games/all",
            });
        }
    }, [stompClient]);

    const createGame = () => {
        if (stompClient) {
            const gameRequest = {
                action: 'create',
                userId: userId
            };
            console.log("Requesting to create a new game");
            stompClient.publish({
                destination: "/4gewinnt/games/create",
                body: JSON.stringify(gameRequest)
            });
        }
    };

    const joinGame = (gameId) => {
        if (stompClient) {
            const joinRequest = {
                action: 'join',
                gameId: gameId,
                userId: userId
            };
            console.log("Requesting to join game with ID:", gameId);
            stompClient.publish({
                destination: "/4gewinnt/join-game",
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
            setGames([]);
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
}

export default Lobby;