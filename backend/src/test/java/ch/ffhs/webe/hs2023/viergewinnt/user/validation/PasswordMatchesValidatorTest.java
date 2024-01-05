package ch.ffhs.webe.hs2023.viergewinnt.user.validation;

import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        final var context = mock(ConstraintValidatorContext.class);
        final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        final var builderContext = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        when(context.getDefaultConstraintMessageTemplate()).thenReturn("baz");
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        when(builder.addPropertyNode(any())).thenReturn(builderContext);

        final var dto = LoginDto.builder().password("foo").matchingPassword("bar").build();
        final var result = this.passwordMatchesValidator.isValid(dto, context);
        assertThat(result).isFalse();

    }
}