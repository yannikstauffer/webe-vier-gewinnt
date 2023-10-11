import React, { useState } from 'react'
import { useParams } from 'react-router-dom';

const Game = ({ userId }) => {
    const { gameId } = useParams();

    return (
        <div>
            <h2>Spiel ID: {gameId}</h2>
        </div>
    );
}

export default Game