package ch.ffhs.webe.hs2023.viergewinnt.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    @InjectMocks
    ControllerExceptionHandler controllerExceptionHandler;

    @ParameterizedTest
    @MethodSource("asVierGewinntExceptionArgs")
    void handleException(final Exception exception, final ErrorCode expectedErrorCode) {
        // act
        final var actual = this.controllerExceptionHandler.handleException(exception);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getCode()).isEqualTo(expectedErrorCode.toString());
        assertThat(actual.getMessageKey()).isEqualTo(expectedErrorCode.getInternationalizedMessageKey());
    }

    @ParameterizedTest
    @MethodSource("asVierGewinntExceptionArgs")
    void asVierGewinntException(final Exception exception, final ErrorCode expectedErrorCode) {
        // act
        final var actual = this.controllerExceptionHandler.asVierGewinntException(exception);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getErrorCode()).isEqualTo(expectedErrorCode);
    }

    static Stream<Arguments> asVierGewinntExceptionArgs() {
        final var exception = new Exception("foo");
        final var runtimeException = new RuntimeException("bar");
        final var vierGewinntException = VierGewinntException.of(ErrorCode.USER_NOT_FOUND, "baz");

        return Stream.of(
                Arguments.of(exception, ErrorCode.UNKNOWN),
                Arguments.of(runtimeException, ErrorCode.UNKNOWN),
                Arguments.of(vierGewinntException, ErrorCode.USER_NOT_FOUND)
        );
    }
}