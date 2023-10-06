package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "games")
public class Game {
    private static final String COL_USER_ONE_ID = "user_one_id";
    private static final String COL_USER_TWO_ID = "user_two_id";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = COL_USER_ONE_ID, nullable = false, updatable = false)
    private User userOne;

    @ManyToOne
    @JoinColumn(name = COL_USER_TWO_ID, nullable = false, updatable = false)
    private User userTwo;
}