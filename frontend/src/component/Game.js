import React, {useEffect} from 'react';
import {useLocation, useNavigate, useParams} from 'react-router-dom';
import GameBoard from './GameBoard';

const Game = ({userId}) => {
    const {gameId} = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const handleBackButtonEvent = (e) => {
            navigate(location.pathname);
            e.preventDefault();
        };

        window.addEventListener('popstate', handleBackButtonEvent);

        return () => {
            sessionStorage.setItem('prevPath', location.pathname);
            window.removeEventListener('popstate', handleBackButtonEvent);
        };
    }, [navigate, location.pathname]);

    return (
        <div>
            <h2>Spiel ID: {gameId}</h2>
            <GameBoard />
        </div>
    );
};

export default Game;