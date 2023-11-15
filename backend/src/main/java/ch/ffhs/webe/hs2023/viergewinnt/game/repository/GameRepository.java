package ch.ffhs.webe.hs2023.viergewinnt.game.repository;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends CrudRepository<Game, Integer> {
    @Query("SELECT g FROM games g WHERE g.userOne = :user OR g.userTwo = :user")
    List<Game> findByPlayer(@Param("user") User user);

    @Query("SELECT g FROM games g WHERE g.userOne.id = :userId OR g.userTwo.id = :userId")
    List<Game> findGamesByUserId(@Param("userId") int userId);
}
