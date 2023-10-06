package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    PLAYER_NOT_FOUND("PLAYER_NOT_FOUND"),
    UNKNOWN("UNKNOWN");

    private final String code;

}
