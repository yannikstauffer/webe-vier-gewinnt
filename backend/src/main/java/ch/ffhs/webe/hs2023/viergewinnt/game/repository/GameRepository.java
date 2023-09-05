package ch.ffhs.webe.hs2023.viergewinnt.game.repository;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Integer> {
}
