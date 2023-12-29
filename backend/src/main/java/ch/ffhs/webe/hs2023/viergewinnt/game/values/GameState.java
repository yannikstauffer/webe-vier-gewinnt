package ch.ffhs.webe.hs2023.viergewinnt.game.values;

// Dient zur Anzeige in der Lobby
public enum GameState {
    WAITING_FOR_PLAYERS,  // Zeigt an, dass Spiel betreten werden kann
    IN_PROGRESS,          // Zeigt an, dass ein Spiel am laufen ist
    PAUSED,               // Spiel ist pausiert und kann weitergespielt werden (z.B disconnect von einem Benutzer)
    PLAYER_HAS_WON,          // Ein Spieler hat gewonnen
    DRAW,                    // Das Spiel endet unentschieden
    PLAYER_LEFT,           // Ein Spieler hat das Spiel verlassen
    DELETED               // Das Spiel wurde gelöscht
}