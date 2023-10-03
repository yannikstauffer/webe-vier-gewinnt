package ch.ffhs.webe.hs2023.viergewinnt.game.repository;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends CrudRepository<Game, Integer> {
    @Query("SELECT g FROM Game g WHERE g.playerOne = :player OR g.playerTwo = :player")
    List<Game> findByPlayer(@Param("player") Player player);
}
