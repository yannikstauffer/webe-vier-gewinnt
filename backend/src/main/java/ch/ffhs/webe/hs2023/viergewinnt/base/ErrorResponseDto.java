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
    private String message;

    public ErrorResponseDto of(final VierGewinntException exception) {
        return ErrorResponseDto.builder()
                .code(exception.getErrorCode().getCode())
                .message(exception.getMessage())
                .build();
    }
}
