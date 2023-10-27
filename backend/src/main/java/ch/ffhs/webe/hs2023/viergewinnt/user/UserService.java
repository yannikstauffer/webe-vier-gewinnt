package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerNewUserAccount(LoginDto loginDto) throws VierGewinntException;

    User getUserById(final int id) throws VierGewinntException;

    User getUserByEmail(final String email) throws VierGewinntException;

    Optional<User> findUserById(final int id);

    List<User> getAllWithSession();

    void setSessionId(final String email, final String sessionId) throws VierGewinntException;

    void removeSessionId(final String email, String sessionId) throws VierGewinntException;
}
