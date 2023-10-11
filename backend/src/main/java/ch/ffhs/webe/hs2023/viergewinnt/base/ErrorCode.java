package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    // die Werte aus dem Enum müssen in der Datei messages.properties definiert werden
    USER_NOT_FOUND("error.user.notFound"),
    EMAIL_ALREADY_EXISTS("error.user.emailAlreadyExists"),
    INVALID_CREDENTIALS("error.user.invalidCredentials"),
    GAME_NOT_FOUND("error.user.gameNotFound"),
    GAME_FULL("error.user.gameIsFull"),
    UNKNOWN("error.unknown");

    private final String internationalizedMessageKey;

    @Override
    public String toString() {
        return this.name();
    }

}
