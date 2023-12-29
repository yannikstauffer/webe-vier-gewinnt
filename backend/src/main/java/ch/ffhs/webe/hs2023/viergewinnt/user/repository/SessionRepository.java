package ch.ffhs.webe.hs2023.viergewinnt.user.repository;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.Session;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    @Modifying
    @Query("DELETE FROM sessions s WHERE s.user = :user")
    void deleteByUser(@Param("user") User user);

    @Query("SELECT s FROM sessions s WHERE s.user = :user AND s.sessionId = :sessionId")
    Optional<Session> findByUserAndSessionId(@Param("user") User user, @Param("sessionId") String sessionId);
}
