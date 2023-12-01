package ch.ffhs.webe.hs2023.viergewinnt.game.values;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ControlMessage {
    public static final String START = "start";
    public static final String RESTART = "restart";
    public static final String CONTINUE = "continue";
    public static final String LEAVE = "leave";
    public static final String LEVEL1 = "LEVEL1";
    public static final String LEVEL2 = "LEVEL2";
    public static final String LEVEL3 = "LEVEL3";
}
