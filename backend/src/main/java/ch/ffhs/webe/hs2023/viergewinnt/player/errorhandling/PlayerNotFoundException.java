package ch.ffhs.webe.hs2023.viergewinnt.player.errorhandling;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(final int id) {
        super("Player with id " + id + " not found.");
    }
}
