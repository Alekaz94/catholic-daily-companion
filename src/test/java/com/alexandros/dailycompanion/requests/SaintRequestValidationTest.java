package com.alexandros.dailycompanion.requests;

import com.alexandros.dailycompanion.dto.SaintRequest;
import com.alexandros.dailycompanion.ValidationTestBase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaintRequestValidationTest extends ValidationTestBase {

    @Test
    void validSaintRequestShouldPassValidation() {
        SaintRequest saintRequest = new SaintRequest(
                "St Francis",
                200,
                250,
                MonthDay.of(10, 4),
                "Saint of the Catholic church",
                "Animals and environment",
                450,
                "http://picture.com"
        );

        SaintRequest saintRequestTwo = new SaintRequest(
                "St Thomas",
                250,
                350,
                MonthDay.of(11, 5),
                "Saint of the Catholic church",
                "Test patronage",
                550,
                null
        );

        Set<ConstraintViolation<SaintRequest>> violations = validator.validate(saintRequest);
        Set<ConstraintViolation<SaintRequest>> violationsTwo = validator.validate(saintRequestTwo);
        assertTrue(violations.isEmpty());
        assertTrue(violationsTwo.isEmpty());
    }

    @Test
    void nullFieldsShouldFailValidation() {
        SaintRequest saintRequest = new SaintRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<SaintRequest>> violations = validator.validate(saintRequest);
        assertEquals(7, violations.size());
    }

    @Test
    void nameTooShortShouldFailValidation() {
        SaintRequest saintRequest = new SaintRequest(
                "S",
                200,
                250,
                MonthDay.of(10, 4),
                "Saint of the Catholic church",
                "Animals and environment",
                450,
        null
        );

        SaintRequest saintRequestTwo = new SaintRequest(
                null,
                250,
                350,
                MonthDay.of(11, 5),
                "Saint of the Catholic church",
                "Test patronage",
                550,
                null
        );

        Set<ConstraintViolation<SaintRequest>> violations = validator.validate(saintRequest);
        Set<ConstraintViolation<SaintRequest>> violationsTwo = validator.validate(saintRequestTwo);

        assertEquals(1, violations.size());
        assertEquals(1, violationsTwo.size());
    }

    @Test
    void biographyTooShortShouldFailValidation() {
        SaintRequest saintRequest = new SaintRequest(
                "St Francis",
                200,
                250,
                MonthDay.of(10, 4),
                "Saint",
                "Animals and environment",
                450,
                null
        );

        SaintRequest saintRequestTwo = new SaintRequest(
                "St Thomas",
                250,
                350,
                MonthDay.of(11, 5),
                null,
                "Test patronage",
                550,
                null
        );

        Set<ConstraintViolation<SaintRequest>> violations = validator.validate(saintRequest);
        Set<ConstraintViolation<SaintRequest>> violationsTwo = validator.validate(saintRequestTwo);

        assertEquals(1, violations.size());
        assertEquals(1, violationsTwo.size());
    }
}
