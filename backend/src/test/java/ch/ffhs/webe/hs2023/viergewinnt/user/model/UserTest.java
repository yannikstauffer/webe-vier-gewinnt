package ch.ffhs.webe.hs2023.viergewinnt.user.model;

import java.util.List;

public class UserTest {
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