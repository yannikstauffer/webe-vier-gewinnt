package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SystemMessageCode {
    SERVER_IS_SHUTTING_DOWN("system.message.serverIsShuttingDown");

    private final String internationalizedMessageKey;

    @Override
    public String toString() {
        return this.name();
    }
}
