package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.Getter;

@Getter
public class VierGewinntException extends RuntimeException {
    private final ErrorCode errorCode;

    public VierGewinntException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
