package ch.ffhs.webe.hs2023.viergewinnt.websocket.values;

import lombok.NoArgsConstructor;

/**
 * New queues must be registered in {@link ch.ffhs.webe.hs2023.viergewinnt.config.WebsocketConfig}
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Queues {
    public static final String USERS = "/queue/users";
    public static final String CHAT = "/queue/chat";
    public static final String CHATS = "/queue/chats";
    public static final String ERROR = "/queue/error";
    public static final String GAME = "/queue/game";
    public static final String GAMES = "/queue/games";
}
