package ch.ffhs.webe.hs2023.viergewinnt.game.values;

// Dient zur Anzeige in der Lobby
public enum GameState {
    WAITING_FOR_PLAYERS,  // Zeigt an, dass Spiel betreten werden kann
    IN_PROGRESS,          // Zeigt an, dass ein Spiel am laufen ist
    PAUSED,               // Spiel ist pausiert und kann weitergespielt werden (z.B disconnect von einem Benutzer)
    NEVER_STARTED,        // Spiel wurde nie gestartet und die Benutzer haben verlassen
    NOT_FINISHED,         // Spiel wurde nicht beendet, aber alle Spieler haben das Spiel verlassen
    FINISHED              // Spiel wurde fertig gespielt.
}
