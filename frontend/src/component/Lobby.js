import React, {useEffect, useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useLocation, useNavigate} from 'react-router-dom';

const Lobby = ({userId}) => {
    const [games, setGames] = useState([]);
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const location = useLocation();

    useSubscription("/topic/lobby/games", (message) => {
        const Game = JSON.parse(message.body);
        setGames((oldGames) => [...oldGames, Game]);

        if (Game.userOne?.id === userId) {
            navigate(`/game/${Game.id}`, {state: {prevPath: location.pathname}});
        } else if (Game.userTwo?.id === userId){
            navigate(`/game/${Game.id}`, {state: {prevPath: location.pathname}});
        }
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
            };
            console.log("Requesting to create a new game");
            stompClient.publish({
                destination: "/4gewinnt/games/create",
            });
        }
    };

    const joinGame = (gameId) => {
        if (stompClient) {
            const joinRequest = {
                gameId: gameId,
            };
            console.log("Requesting to join game with ID:", gameId);
            stompClient.publish({
                destination: "/4gewinnt/games/join",
                body: JSON.stringify(joinRequest),
            });
        }
    };

    const deleteAllGames = () => {
        if (stompClient) {
            console.log("Requesting to delete all games");
            stompClient.publish({
                destination: "/4gewinnt/games/deleteAll",
            });
        }
    };

    return (
        <div>
            <h2>Lobby</h2>
            <button onClick={createGame}>Neues Spiel erstellen</button>
            <button onClick={deleteAllGames}>Liste löschen</button>
            <ul>
                {games.map((gameData) => (
                    <li key={gameData.id} onClick={() => joinGame(gameData.id)}>
                        Spiel ID: {gameData.id} - Ersteller: {gameData.userOne?.firstName} - State: {gameData.gameState}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Lobby;
