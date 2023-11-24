package ch.ffhs.webe.hs2023.viergewinnt.game;

import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.StompMessageService;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameMessagesProxy {
    private final StompMessageService messageService;
    private final UserService userService;

    @Autowired
    GameMessagesProxy(final StompMessageService messageService, final UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    public void notifyPlayers(final Game game) {
        if (game.getUserOne() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserOne().getId()), GameStateDto.of(game));
        }
        if (game.getUserTwo() != null) {
            this.messageService.sendToUser(Queues.GAME, this.userService.getUserById(game.getUserTwo().getId()), GameStateDto.of(game));
        }
    }

    public void notifyAll(final Game game) {
        this.messageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
        this.notifyPlayers(game);
    }
}
