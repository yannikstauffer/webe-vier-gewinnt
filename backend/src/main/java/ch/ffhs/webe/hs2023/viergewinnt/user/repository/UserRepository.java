package ch.ffhs.webe.hs2023.viergewinnt.user.repository;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    @Query("SELECT u FROM users u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM users u INNER JOIN sessions s ON u = s.user")
    List<User> findWithSession();

    @Query("SELECT COUNT (u) FROM users u")
    int countUsers();
}
