package ch.ffhs.webe.hs2023.viergewinnt.user.repository;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
