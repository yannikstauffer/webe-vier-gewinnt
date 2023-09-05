package ch.ffhs.webe.hs2023.viergewinnt.game.model;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Game {
    @Id
    @Generated
    private int id;

    @ManyToOne
    @JoinColumn(name = "player_one_id")
    private User playerOne;

    @ManyToOne
    @JoinColumn(name = "player_two_id")
    private User playerTwo;
}