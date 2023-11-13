import React, {useState} from 'react';
import {useStompClient, useSubscription} from "react-stomp-hooks";
import {useLocation, useNavigate} from 'react-router-dom';

const Lobby = ({userId}) => {
    const [games, setGames] = useState([]);
    const stompClient = useStompClient();
    const navigate = useNavigate();
    const location = useLocation();

    const onGamesReceived = (message) => {
        const updatedGames = JSON.parse(message.body);

        console.log("update in lobby:" + updatedGames);

        setGames(updatedGames);
    };

    const onLobbyGameReceived = (message) => {
        const data = JSON.parse(message.body);
        if (Array.isArray(data)) {
            setGames(data);
        } else {
            setGames((oldGames) => {
                const otherGames = oldGames.filter(g => g.id !== data.id);
                return [...otherGames, data];
            });

            if (data.userOne?.id === userId || data.userTwo?.id === userId) {
                navigate(`/game/${data.id}`, {state: {prevPath: location.pathname}});
            }
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
