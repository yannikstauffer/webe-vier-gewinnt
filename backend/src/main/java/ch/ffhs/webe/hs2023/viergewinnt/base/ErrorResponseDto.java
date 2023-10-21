package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseDto {
    private String code;
    private String messageKey;
    private String identifier;

    public static ErrorResponseDto of(final VierGewinntException exception) {
        return ErrorResponseDto.builder()
                .code(exception.getErrorCode().toString())
                .messageKey(exception.getErrorCode().getInternationalizedMessageKey())
                .identifier(exception.getIdentifier().toString())
                .build();
    }
}
