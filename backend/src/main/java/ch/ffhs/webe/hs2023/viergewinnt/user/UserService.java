package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public interface UserService {
    User registerNewUserAccount(UserDto userDto) throws VierGewinntException;

    User getUserById(final int id) throws VierGewinntException;

    User getUserByEmail(final String id) throws VierGewinntException;

    Optional<User> findUserById(final int id);

    User getCurrentlyAuthenticatedUser() throws VierGewinntException;
}
