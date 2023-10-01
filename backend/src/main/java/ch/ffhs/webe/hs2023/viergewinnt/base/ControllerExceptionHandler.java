package ch.ffhs.webe.hs2023.viergewinnt.base;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerErrorHandler {
    @ExceptionHandler(value = {VierGewinntException.class})
    protected ErrorResponseDto handleVierGewinntException(final VierGewinntException exception) {
        return ErrorResponseDto.builder()
                .code(exception.getErrorCode().getCode())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ErrorResponseDto handleRuntimeException(final RuntimeException ex) {
        return ErrorResponseDto.builder()
                .code(ErrorCode.UNKNOWN.getCode())
                .message(ex.getMessage())
                .build();
    }
}
