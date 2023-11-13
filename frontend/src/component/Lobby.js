import React, {useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useLocation, useNavigate} from 'react-router-dom';

const Lobby = ({userId}) => {
    const [games, setGames] = useState([]);
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const location = useLocation();

    const filterGamesByState = (games) => {
        return games.filter(game =>
            game.gameState === 'IN_PROGRESS' ||
            game.gameState === 'PAUSED' ||
            game.gameState === 'WAITING_FOR_PLAYERS'
        );
    };

    const onGamesReceived = (message) => {
        const updatedGames = filterGamesByState(JSON.parse(message.body));
        console.log("update /user/queue/games:", updatedGames);
        setGames(updatedGames);
    };

    const onLobbyGameReceived = (message) => {
        const updatedGame = JSON.parse(message.body);

        console.log("update /topic/lobby/games:", updatedGame);

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

        if (updatedGame.userOne?.id === userId || updatedGame.userTwo?.id === userId) {
            navigate(`/game/${updatedGame.id}`, {state: {prevPath: location.pathname}});
        }
    };

    useSubscription("/user/queue/games", onGamesReceived);
    useSubscription("/topic/lobby/games", onLobbyGameReceived);

    const createGame = () => {
        if (stompClient) {
            stompClient.publish({destination: "/4gewinnt/games/create"});
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

    return (
        <div>
            <h2>Lobby</h2>
            <button onClick={createGame}>Neues Spiel erstellen</button>
            <ul>
                {games.map((game) => (
                    <li key={game.id} onClick={() => joinGame(game.id)}>
                        Spiel ID: {game.id} - Ersteller: {game.userOne?.firstName} - State: {game.gameState}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Lobby;
