package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    // die Werte aus dem Enum müssen im Frontend unter /public/locales/* in den translation.json definiert werden
    RECEIVER_NOT_SET("error.chat.receiverNotSet"),
    RECEIVER_ON_PUBLIC_MESSAGE("error.chat.receiverShouldNotBeSetOnPublicMessage"),
    MESSAGE_TEXT_EMPTY("error.chat.messageTextEmpty"),
    USER_NOT_FOUND("error.user.notFound"),
    EMAIL_ALREADY_EXISTS("error.user.emailAlreadyExists"),
    INVALID_CREDENTIALS("error.user.invalidCredentials"),
    GAME_NOT_FOUND("error.game.notFound"),
    GAME_FULL("error.game.isFull"),
    GAMEBOARD_READ_ERROR("error.game.cannotRead"),
    GAMEBOARD_WRITE_ERROR("error.game.cannotWrite"),
    GAME_NOT_READY("error.game.notReady"),
    INVALID_MOVE("error.game.invalidMove"),
    UNKNOWN("error.unknown");

    private final String internationalizedMessageKey;

    @Override
    public String toString() {
        return this.name();
    }

}
