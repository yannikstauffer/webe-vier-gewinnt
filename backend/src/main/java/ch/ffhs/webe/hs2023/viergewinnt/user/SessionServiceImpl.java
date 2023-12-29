package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.Session;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionServiceImpl implements SessionService {
    private final SessionRepository repository;

    @Autowired
    public SessionServiceImpl(final SessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void deleteAll() {
        this.repository.deleteAll();
    }

    @Override
    public void deleteByUser(final User user) {
        this.repository.deleteByUser(user);
    }

    @Override
    public void addSession(final User user, final String stompSessionId) throws VierGewinntException {
        final var newSession = Session.of(user, stompSessionId);
        this.repository.save(newSession);
    }

    @Override
    public void removeSession(final User user, final String stompSessionId) throws VierGewinntException {
        final var session = this.repository.findByUserAndSessionId(user, stompSessionId);
        session.ifPresent(
                s -> {
                    user.removeSession(s.getSessionId());
                    this.repository.deleteById(s.getSessionId());
                }
        );
    }
}
