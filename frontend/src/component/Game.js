import React, {useEffect} from 'react';
import {useLocation, useNavigate, useParams} from 'react-router-dom';
import GameBoard from './GameBoard';
import './Game.css';

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
        <div className="game-layout">
            <GameBoard initialGameId={gameId} userId={userId}/>
        </div>
    );
};

export default Game;