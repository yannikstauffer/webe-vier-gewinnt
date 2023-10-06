package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {
    
    @MessageExceptionHandler(value = {Exception.class})
    @SendToUser("/queue/error")
    protected ErrorResponseDto handleRuntimeException(final Exception exception) {
        log.error(exception.getMessage(), exception);
        final var errorCode = this.extractErrorCode(exception);
        return ErrorResponseDto.builder()
                .code(errorCode.getCode())
                .message(exception.getMessage())
                .build();
    }

    private ErrorCode extractErrorCode(final Exception exception) {
        return exception.getClass().isAssignableFrom(VierGewinntException.class)
                ? ((VierGewinntException) exception).getErrorCode()
                : ErrorCode.UNKNOWN;
    }
}
