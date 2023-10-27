package ch.ffhs.webe.hs2023.viergewinnt.websocket.values;

import lombok.NoArgsConstructor;

/**
 * New topics must be registered in {@link ch.ffhs.webe.hs2023.viergewinnt.config.WebsocketConfig}
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Topics {
    public static final String USERS = "/topic/users";
    public static final String LOBBY_CHAT = "/topic/lobby/chat";
    public static final String LOBBY_GAMES = "/topic/lobby/games";
}
