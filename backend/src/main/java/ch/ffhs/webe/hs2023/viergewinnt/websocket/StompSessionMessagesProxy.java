package ch.ffhs.webe.hs2023.viergewinnt.websocket;

import ch.ffhs.webe.hs2023.viergewinnt.chat.ChatService;
import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.ChatsDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.GameService;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.dto.GameStateDto;
import ch.ffhs.webe.hs2023.viergewinnt.game.model.Game;
import ch.ffhs.webe.hs2023.viergewinnt.user.UserService;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserUpdateDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.UserUpdateType;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Queues;
import ch.ffhs.webe.hs2023.viergewinnt.websocket.values.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class StompSessionMessagesProxy {
    private final UserService userService;
    private final ChatService chatService;
    private final StompMessageService stompMessageService;
    private final GameService gameService;

    @Autowired
    public StompSessionMessagesProxy(final StompMessageService messageService, final UserService userService, final ChatService chatService, GameService gameService) {
        this.stompMessageService = messageService;
        this.userService = userService;
        this.chatService = chatService;
        this.gameService = gameService;
    }

    void publishAllChatsTo(final User recipient) {
        final var publicMessages = this.chatService.getPublicMessages(LocalDateTime.now().minusMinutes(10));
        final var privateMessages = this.chatService.getPrivateMessages(recipient);
        final var chats = ChatsDto.of(privateMessages, publicMessages);
        this.stompMessageService.sendToUser(Queues.CHATS, recipient, chats);
    }

    void publishAllUsersTo(final User recipient) {
        final var users = this.userService.getAllWithSession();
        this.stompMessageService.sendToUser(Queues.USERS, recipient, UserDto.of(users));
    }


    void publishAllGamesTo(final User recipient) {
        final var games = this.gameService.getAllGames();
        this.stompMessageService.sendToUser(Queues.GAMES, recipient, GameDto.of(games));
    }

    void publishUserUpdate(final User user, final UserUpdateType userUpdateType) {
        this.stompMessageService.send(Topics.USERS, UserUpdateDto.of(user, userUpdateType));
    }

    void publishUserLeftGames(final User recipient, List<Game> games) {
        games.forEach(game -> {
            this.stompMessageService.sendToUser(Queues.GAME, recipient, GameDto.of(game));
            this.stompMessageService.send(Topics.LOBBY_GAMES, GameDto.of(game));
        });
    }

    void publishGameUpdate(final User recipient, Game game) {
        this.stompMessageService.sendToUser(Queues.GAME, recipient, GameStateDto.of(game));
    }

}
