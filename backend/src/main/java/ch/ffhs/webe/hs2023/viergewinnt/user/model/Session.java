package ch.ffhs.webe.hs2023.viergewinnt.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "sessions")
public class Session {
    @Id
    @Column(updatable = false)
    private String sessionId;
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    private User user;

    public static Session of(final User user, final String sessionId) {
        return Session.builder()
                .user(user)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
