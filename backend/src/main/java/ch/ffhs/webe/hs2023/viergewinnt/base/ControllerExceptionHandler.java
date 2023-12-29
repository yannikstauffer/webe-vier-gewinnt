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
    protected ErrorResponseDto handleException(final Exception exception) {
        final var vierGewinntException = this.asVierGewinntException(exception);
        log.error(exception.getMessage(), exception);

        return ErrorResponseDto.of(vierGewinntException);
    }

    protected VierGewinntException asVierGewinntException(final Exception exception) {
        return exception instanceof final VierGewinntException vierGewinntException
                ? vierGewinntException
                : VierGewinntException.of(ErrorCode.UNKNOWN, exception);
    }
}
