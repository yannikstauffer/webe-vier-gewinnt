package ch.ffhs.webe.hs2023.viergewinnt.game.values;

// Dient zur Anzeige im Game
public enum GameBoardState {
    WAITING_FOR_PLAYERS,
    PLAYER_HAS_WON,
    DRAW,
    MOVE_EXPECTED,
    READY_TO_START,
    PLAYER_QUIT,
    PLAYER_DISCONNECTED,
    NOT_STARTED
}
