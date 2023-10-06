package ch.ffhs.webe.hs2023.viergewinnt.player;

import ch.ffhs.webe.hs2023.viergewinnt.player.model.Player;
import ch.ffhs.webe.hs2023.viergewinnt.player.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("PlayerService")
class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(final PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getPlayerById(final int id) {
        return this.findPlayerById(id).orElseGet(() -> this.playerRepository.save(new Player(id)));
//        return this.playerRepository.findById(id)
//                .orElseThrow(() -> new VierGewinntException(
//                        ErrorCode.PLAYER_NOT_FOUND,
//                        "Player with id " + id + " not found"));
    }

    @Override
    public Optional<Player> findPlayerById(final int id) {
        return this.playerRepository.findById(id);
    }
}
