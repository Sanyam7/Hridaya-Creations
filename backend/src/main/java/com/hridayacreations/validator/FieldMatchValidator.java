package com.hridayacreations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

/**
 * Implementation of {@link FieldMatch}. Reads the two named properties from the validated bean and
 * asserts equality. The violation is reported against the {@code second} field for friendlier UX.
 */
public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(FieldMatch constraint) {
        this.firstFieldName = constraint.first();
        this.secondFieldName = constraint.second();
        this.message = constraint.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object firstValue = wrapper.getPropertyValue(firstFieldName);
        Object secondValue = wrapper.getPropertyValue(secondFieldName);

        boolean matches = Objects.equals(firstValue, secondValue);
        if (!matches) {
            // Attach the violation to the second field instead of the whole object.
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(secondFieldName)
                    .addConstraintViolation();
        }
        return matches;
    }
}
