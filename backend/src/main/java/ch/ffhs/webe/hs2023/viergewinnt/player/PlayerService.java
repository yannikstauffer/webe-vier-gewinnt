package ch.ffhs.webe.hs2023.viergewinnt.player;

import ch.ffhs.webe.hs2023.viergewinnt.player.errorhandling.PlayerNotFoundException;
import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;

import java.util.Optional;

public interface PlayerService {
    Player getPlayerById(final int id) throws PlayerNotFoundException;

    Optional<Player> findPlayerById(final int id);

}
