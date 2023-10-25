package ch.ffhs.webe.hs2023.viergewinnt.user.model;

import ch.ffhs.webe.hs2023.viergewinnt.user.values.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    private int id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String password;

    private List<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Session> sessions;

    public void addSession(final String sessionId) {
        final var session = Session.of(this, sessionId);
        this.sessions.add(session);
    }

    public void removeSession(final String sessionId) {
        this.sessions.removeIf(session -> session.getSessionId().equals(sessionId));
    }

    public List<Session> getSessions() {
        return Collections.unmodifiableList(this.sessions);
    }
}
