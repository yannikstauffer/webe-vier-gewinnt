package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

public interface SessionService {
    void deleteAll();

    void deleteByUser(final User user);

    void addSession(final User user, final String stompSessionId) throws VierGewinntException;
}
