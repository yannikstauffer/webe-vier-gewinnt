package ch.ffhs.webe.hs2023.viergewinnt.chat.model;

import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    private int id;
    private String text;
    private MessageType messageType;
    private LocalDateTime sentAt;

    @Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private Player sender;

    @Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "receiver_id", insertable = false, updatable = false)
    private Player receiver;

    public Optional<Player> getReceiver() {
        return Optional.ofNullable(this.receiver);
    }

}
