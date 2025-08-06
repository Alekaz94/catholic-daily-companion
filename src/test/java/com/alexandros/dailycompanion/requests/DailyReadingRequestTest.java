package com.alexandros.dailycompanion.requests;

import com.alexandros.dailycompanion.dto.DailyReadingRequest;
import com.alexandros.dailycompanion.ValidationTestBase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DailyReadingRequestTest extends ValidationTestBase {

    @Test
    void validDailyReadingRequestShouldPassValidation() {
        DailyReadingRequest readingRequest = new DailyReadingRequest(
                "Exodus",
                "Hebrews",
                "Psalm 123",
                "John"
        );

        DailyReadingRequest readingRequestTwo = new DailyReadingRequest(
                "Leviticus",
                null,
                "Psalm 12",
                "Matthew"
        );

        Set<ConstraintViolation<DailyReadingRequest>> violations = validator.validate(readingRequest);
        Set<ConstraintViolation<DailyReadingRequest>> violationsTwo = validator.validate(readingRequestTwo);
        assertTrue(violations.isEmpty());
        assertTrue(violationsTwo.isEmpty());
    }

    @Test
    void nullFieldsShouldFailValidation() {
        DailyReadingRequest readingRequest = new DailyReadingRequest(
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<DailyReadingRequest>> violations = validator.validate(readingRequest);
        assertEquals(3, violations.size());
    }
}
