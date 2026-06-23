package com.hridayacreations.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a password is sufficiently strong: at least 8 characters including an uppercase
 * letter, a lowercase letter, a digit and a special character, with no whitespace.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String message() default "Password must be at least 8 characters and include an uppercase letter, "
            + "a lowercase letter, a digit and a special character, with no spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
