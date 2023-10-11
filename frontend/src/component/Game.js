import {useEffect, useState} from 'react';
import {useLocation, useNavigate, useParams} from 'react-router-dom';
import {useStompClient} from "react-stomp-hooks";

const Game = ({userId}) => {
    const {gameId} = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const [previousLocation, setPreviousLocation] = useState();
    const stompClient = useStompClient();

    useEffect(() => {
        // Handler-Funktion für das popstate-Event todo: geht noch nicht
        const handlePopState = () => {
            console.log("Zurück-Button wurde geklickt oder es wurde zur Lobby navigiert");

            if (stompClient && stompClient.connected) {
                stompClient.publish({
                    destination: "/4gewinnt/games/left",
                    body: JSON.stringify({
                        gameId: gameId,
                        message: "Das Spiel wurde verlassen"
                    })
                });
            }

            alert("Sie haben das Spiel verlassen.");
        }

        window.addEventListener('popstate', handlePopState);

        return () => {
            window.removeEventListener('popstate', handlePopState);
        }
    }, [gameId, stompClient]);

    return (
        <div>
            <h2>Spiel ID: {gameId}</h2>
        </div>
    );
}

export default Game