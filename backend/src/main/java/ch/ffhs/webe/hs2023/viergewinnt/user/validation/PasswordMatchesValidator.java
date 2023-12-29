package ch.ffhs.webe.hs2023.viergewinnt.user.validation;

import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
        // not required for this validator
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        final LoginDto user = (LoginDto) obj;
        return user.getPassword().equals(user.getMatchingPassword());
    }
}