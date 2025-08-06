package com.alexandros.dailycompanion.requests;

import com.alexandros.dailycompanion.dto.UserRequest;
import com.alexandros.dailycompanion.ValidationTestBase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserRequestsValidationTest extends ValidationTestBase {

    @Test
    void validUserRequestShouldPassValidation() {
        UserRequest userRequest = new UserRequest("User", "Test", "user@example.com", "password");

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(userRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullFieldsShouldFailValidation() {
        UserRequest userRequest = new UserRequest("User", "Test", "user@example.com", null);
        UserRequest userRequestTwo = new UserRequest("User", "Test", null, "password");
        UserRequest userRequestThree = new UserRequest("User", null, "user@example.com", "password");
        UserRequest userRequestFour = new UserRequest(null, "Test", "user@example.com", "password");
        UserRequest userRequestFive = new UserRequest(null, null, null, null);

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(userRequest);
        Set<ConstraintViolation<UserRequest>> violationsTwo = validator.validate(userRequestTwo);
        Set<ConstraintViolation<UserRequest>> violationsThree = validator.validate(userRequestThree);
        Set<ConstraintViolation<UserRequest>> violationsFour = validator.validate(userRequestFour);
        Set<ConstraintViolation<UserRequest>> violationsFive = validator.validate(userRequestFive);

        assertEquals(1, violations.size());
        assertEquals(1, violationsTwo.size());
        assertEquals(1, violationsThree.size());
        assertEquals(1, violationsFour.size());
        assertEquals(4, violationsFive.size());
    }

    @Test
    void passwordTooShortShouldFailValidation() {
        UserRequest userRequest = new UserRequest("User", "Test", "user@example.com", "pass");

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(userRequest);

        assertFalse(violations.isEmpty());
        assertTrue(violations
                .stream()
                .anyMatch(violation -> violation.getMessage().contains("Password must be atleast 8 characters long")));
    }
}
