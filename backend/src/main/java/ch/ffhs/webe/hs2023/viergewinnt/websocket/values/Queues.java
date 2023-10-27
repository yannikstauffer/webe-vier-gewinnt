package ch.ffhs.webe.hs2023.viergewinnt.websocket.values;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Queues {
    public static final String CHAT = "/queue/chat";
    public static final String ERROR = "/queue/error";
}
