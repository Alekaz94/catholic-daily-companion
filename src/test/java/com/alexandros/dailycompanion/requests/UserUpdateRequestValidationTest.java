package com.alexandros.dailycompanion.requests;

import com.alexandros.dailycompanion.dto.UserUpdateRequest;
import com.alexandros.dailycompanion.ValidationTestBase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserUpdateRequestValidationTest extends ValidationTestBase {

    @Test
    void validUserUpdateRequestShouldPassValidation() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("password", "password123");

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(updateRequest);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shortNewPasswordShouldNotPassValidation() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("password", "notPass");

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(updateRequest);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(violation -> violation.getMessage().contains("Password must be atleast 8 characters long")));

    }
}
