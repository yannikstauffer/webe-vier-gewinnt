const GameState = {
    WAITING_FOR_PLAYERS: 'WAITING_FOR_PLAYERS',
    IN_PROGRESS: 'IN_PROGRESS',
    PAUSED: 'PAUSED',
    PLAYER_LEFT: 'PLAYER_LEFT',
    PLAYER_HAS_WON: 'PLAYER_HAS_WON',
    DRAW: 'DRAW'
};

const UserState = {
    CONNECTED: 'CONNECTED',
    DISCONNECTED: 'DISCONNECTED',
    QUIT: 'QUIT'
}

const GameLevel = {
    LEVEL1: 'LEVEL1',
    LEVEL2: 'LEVEL2',
    LEVEL3: 'LEVEL3'
}

class GameModel {
    constructor({gameId, board, nextMove, gameLevel, gameState, userOne, userOneState, userTwo, userTwoState}) {
        this._gameId = gameId;
        this._board = board;
        this._nextMove = nextMove;
        this._gameLevel = gameLevel;
        this._gameState = gameState;
        this._userOne = userOne;
        this._userOneState = userOneState;
        this._userTwo = userTwo;
        this._userTwoState = userTwoState;
    }

    get gameLevel() {
        return this._gameLevel;
    }

    get gameId() {
        return this._gameId;
    }

    get board() {
        return this._board;
    }

    set board(board) {
        this._board = board;
    }

    get userOne() {
        return this._userOne;
    }

    get userTwo() {
        return this._userTwo;
    }

    bothUsersAreConnected() {
        // todo cover: disconnected vs quit vs connected
        return this._userOne != null && this._userTwo != null
            && this._userOneState === UserState.CONNECTED && this._userTwoState === UserState.CONNECTED;
    }

    isWaitingForPlayers() {
        return this._gameState === GameState.WAITING_FOR_PLAYERS
            && !this.bothUsersAreConnected();
    }

    isReadyToStart() {
        return this._gameState === GameState.WAITING_FOR_PLAYERS
            && this.bothUsersAreConnected();
    }

    isNotYetStarted() {
        return this.isWaitingForPlayers() || this.isReadyToStart();
    }

    isMoveExpected() {
        return this._gameState === GameState.IN_PROGRESS
            && this.bothUsersAreConnected();
    }

    hasNextMove(userId) {
        return this.isMoveExpected() && this._nextMove === userId;
    }

    isPaused() {
        return this._gameState === GameState.PAUSED
            && this.anyUserDisconnected();
    }

    anyUserDisconnected() {
        return this._userOneState === UserState.DISCONNECTED
            || this._userTwoState === UserState.DISCONNECTED;
    }

    isReadyToContinue() {
        return (this._gameState === GameState.PAUSED || this.playerHasLeft())
            && this.bothUsersAreConnected();
    }

    playerHasLeft() {
        return this._gameState === GameState.PLAYER_LEFT
            || this._userOneState === UserState.QUIT
            || this._userTwoState === UserState.QUIT;
    }

    playerHasWon() {
        return this._gameState === GameState.PLAYER_HAS_WON;
    }

    isWinner(userId) {
        return this.playerHasWon() && this._nextMove === userId;
    }

    isDraw() {
        return this._gameState === GameState.DRAW;
    }

    isFinished() {
        return this.playerHasWon() || this.isDraw();
    }

    isClosed() {
        return this.isFinished() || this.playerHasLeft();
    }
}

export {GameModel, GameState, UserState, GameLevel};