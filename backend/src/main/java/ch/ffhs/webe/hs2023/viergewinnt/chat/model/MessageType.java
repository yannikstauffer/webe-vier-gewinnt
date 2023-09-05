package ch.ffhs.webe.hs2023.viergewinnt.chat.model;

import lombok.Getter;

@Getter
public enum MessageType {
    PUBLIC(Values.PUBLIC),
    PRIVATE(Values.PRIVATE),
    SYSTEM(Values.SYSTEM);

    private final String value;

    MessageType(final String value) {
        this.value = value;
    }

    public static class Values {
        private Values() {
        }

        public static final String PUBLIC = "public";
        public static final String PRIVATE = "private";
        public static final String SYSTEM = "system";
    }
}
