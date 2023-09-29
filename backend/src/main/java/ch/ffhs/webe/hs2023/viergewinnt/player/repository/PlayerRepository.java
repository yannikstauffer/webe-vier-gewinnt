package ch.ffhs.webe.hs2023.viergewinnt.player.repository;

import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;
import org.springframework.data.repository.CrudRepository;

public interface PlayerRepository extends CrudRepository<Player, Integer> {
}
