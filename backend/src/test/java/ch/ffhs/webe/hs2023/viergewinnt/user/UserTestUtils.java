package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.Session;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;

public class UserTestUtils {
    public static User user(final int id) {
        return user(id, null);
    }

    public static User user(final int id, final List<Session> sessions) {
        final var builder = User.builder()
                .id(id)
                .firstName("foo")
                .lastName("bar");
        if (sessions != null) builder.sessions(sessions);

        return builder.build();
    }
}
