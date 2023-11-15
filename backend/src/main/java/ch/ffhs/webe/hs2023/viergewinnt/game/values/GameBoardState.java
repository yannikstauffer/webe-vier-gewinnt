package ch.ffhs.webe.hs2023.viergewinnt.game.values;

// Dient zur Anzeige im Game
public enum GameBoardState {
    WAITING_FOR_PLAYERS,     // Wartet auf weiteren Spieler
    PLAYER_HAS_WON,          // Ein Spieler hat gewonnen
    DRAW,                    // Das Spiel endet unentschieden
    MOVE_EXPECTED,           // Spielzug erwartet
    READY_TO_START,          // Das Spiel ist bereit zum spielen
    PLAYER_QUIT,             // Ein Spieler hat das Spiel verlassen
    PLAYER_DISCONNECTED,     // Ein Spieler hat die Verbindung verloren
    NOT_STARTED              // Das Spiel wurde noch nicht gestartet
}
