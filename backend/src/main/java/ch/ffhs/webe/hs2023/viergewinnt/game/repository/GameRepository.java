package ch.ffhs.webe.hs2023.viergewinnt.game.repository;

import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.game.values.UserState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends CrudRepository<Game, Integer> {

    @Query("""
            SELECT g
            FROM games g
            WHERE g.userOne.id = :userId OR g.userTwo.id = :userId
            AND (g.gameState = 'IN_PROGRESS' OR g.gameState = 'PAUSED' OR g.gameState = 'PLAYER_LEFT' OR g.gameState = 'WAITING_FOR_PLAYERS')
            """)
    List<Game> findCurrentlyActiveForUserId(@Param("userId") int userId);

    @Query("""
            SELECT g
            FROM games g
            WHERE (g.gameState = 'IN_PROGRESS' OR g.gameState = 'PAUSED' OR g.gameState = 'PLAYER_LEFT' OR g.gameState = 'WAITING_FOR_PLAYERS')
            """)
    List<Game> findCurrentlyActive();

    @Query("SELECT g from games g WHERE g.userOneState = :userState OR g.userTwoState = :userState")
    List<Game> findByUserState(@Param("userState") UserState userState);

}
