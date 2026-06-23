package com.hridayacreations.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the strong-password rule: 8+ chars with upper, lower, digit and special, no whitespace.
 */
class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Secret@123", "Aa1!aaaa", "P@ssw0rd!", "Xyz#2026ab"})
    void acceptsStrongPasswords(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",        // too short
            "alllower@123",   // no uppercase
            "ALLUPPER@123",   // no lowercase
            "NoDigits@abc",   // no digit
            "NoSpecial123",   // no special char
            "With Space@1A"   // contains whitespace
    })
    void rejectsWeakPasswords(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @Test
    void rejectsNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }
}
