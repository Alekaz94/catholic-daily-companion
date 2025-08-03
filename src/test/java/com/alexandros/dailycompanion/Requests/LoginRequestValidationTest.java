package com.alexandros.dailycompanion.Requests;

import com.alexandros.dailycompanion.DTO.LoginRequest;
import com.alexandros.dailycompanion.ValidationTestBase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginRequestValidationTest extends ValidationTestBase {

    @Test
    void validLoginRequestShouldPassValidation() {
        LoginRequest loginRequest = new LoginRequest("user@email.com", "password");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullFieldsShouldNotPassValidation() {
        LoginRequest loginRequest = new LoginRequest("user@email.com", null);
        LoginRequest loginRequestTwo = new LoginRequest(null, "password");
        LoginRequest loginRequestThree = new LoginRequest(null, null);

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);
        Set<ConstraintViolation<LoginRequest>> violationsTwo = validator.validate(loginRequestTwo);
        Set<ConstraintViolation<LoginRequest>> violationsThree = validator.validate(loginRequestThree);

        assertEquals(1, violations.size());
        assertEquals(1, violationsTwo.size());
        assertEquals(2, violationsThree.size());
    }
}
