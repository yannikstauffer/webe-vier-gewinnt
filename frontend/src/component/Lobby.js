import React, {useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";

const Lobby = ({userId}) => {
    const [games, setGames] = useState([]);
    const stompClient = useStompClient();

    useSubscription("/topic/lobby/games", (message) => {
        const payload = JSON.parse(message.body);
        console.log("Received payload:", payload);
        setGames(oldGames => [...oldGames, payload]);
    });

    const createGame = () => {
        if (stompClient) {
            const gameRequest = {
                action: 'create',
                userId: userId
            };
            console.log("Requesting to create a new game");
            stompClient.publish({
                destination: "/4gewinnt/create-game",
                body: JSON.stringify(gameRequest)
            });
        }
    }

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
    }

    return (
        <div>
            <h1>Lobby</h1>
            <button onClick={createGame}>Neues Spiel erstellen</button>
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
