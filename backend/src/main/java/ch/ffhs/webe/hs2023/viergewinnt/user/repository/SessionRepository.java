package ch.ffhs.webe.hs2023.viergewinnt.user.repository;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.Session;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    @Query("DELETE FROM sessions s WHERE s.user = :user")
    void deleteByUser(@Param("user") User user);
}
