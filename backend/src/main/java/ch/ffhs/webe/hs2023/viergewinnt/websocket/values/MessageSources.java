package ch.ffhs.webe.hs2023.viergewinnt.websocket.values;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MessageSources {
    public static final String MESSAGE = "/message";
    public static final String PRIVATE_MESSAGE = "/private-message";
    public static final String GAMES = "/games";

}
