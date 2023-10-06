package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.Getter;

import java.util.UUID;

@Getter
public class VierGewinntException extends RuntimeException {
    private final ErrorCode errorCode;
    private final UUID identifier;

    public static VierGewinntException of(final ErrorCode errorCode, final String message) {
        return new VierGewinntException(UUID.randomUUID(), errorCode, message);
    }

    public static VierGewinntException of(final ErrorCode errorCode, final Throwable throwable) {
        return new VierGewinntException(UUID.randomUUID(), errorCode, throwable);
    }

    private VierGewinntException(final UUID identifier, final ErrorCode errorCode, final String message) {
        super(String.format("[%s: %s]: %s", errorCode, identifier, message));
        this.errorCode = errorCode;
        this.identifier = identifier;
    }

    private VierGewinntException(final UUID identifier, final ErrorCode errorCode, final Throwable throwable) {
        super(String.format("[%s: %s]: %s", errorCode, identifier, throwable.getMessage()), throwable);
        this.errorCode = errorCode;
        this.identifier = identifier;
    }
}
