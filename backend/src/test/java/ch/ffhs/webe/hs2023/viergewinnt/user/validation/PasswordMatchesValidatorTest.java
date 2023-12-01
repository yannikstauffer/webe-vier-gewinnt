package ch.ffhs.webe.hs2023.viergewinnt.user.validation;

import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordMatchesValidatorTest {

    PasswordMatchesValidator passwordMatchesValidator;

    @BeforeEach
    void setUp() {
        this.passwordMatchesValidator = new PasswordMatchesValidator();
    }

    @Test
    void isValid() {
        final var dto = LoginDto.builder().password("foo").matchingPassword("foo").build();
        final var result = this.passwordMatchesValidator.isValid(dto, null);
        assertThat(result).isTrue();

    }

    @Test
    void isValid_whenInvalid_returnsFalse() {
        final var dto = LoginDto.builder().password("foo").matchingPassword("bar").build();
        final var result = this.passwordMatchesValidator.isValid(dto, null);
        assertThat(result).isFalse();

    }
}