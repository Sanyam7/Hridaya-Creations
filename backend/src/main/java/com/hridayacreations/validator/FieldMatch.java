package com.hridayacreations.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint asserting that two properties hold equal values (e.g. {@code password} and
 * {@code confirmPassword}). Repeatable to validate multiple field pairs on one type.
 */
@Documented
@Constraint(validatedBy = FieldMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldMatch.List.class)
public @interface FieldMatch {

    String message() default "The fields do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Name of the first property. */
    String first();

    /** Name of the second property that must equal the first. */
    String second();

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        FieldMatch[] value();
    }
}
