package com.hridayacreations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementation of {@link StrongPassword}. Null is treated as invalid; use {@code @NotBlank}
 * separately if a clearer "required" message is desired in addition.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && STRONG_PASSWORD.matcher(value).matches();
    }
}
