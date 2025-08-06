package com.alexandros.dailycompanion.requests;

import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.ValidationTestBase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JournalEntryRequestTest extends ValidationTestBase {

    @Test
    void validJournalEntryRequestShouldPassValidation() {
        JournalEntryRequest entryRequest = new JournalEntryRequest(
          "Title",
          "Content"
        );

        Set<ConstraintViolation<JournalEntryRequest>> violations = validator.validate(entryRequest);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullFieldsShouldFailValidation() {
        JournalEntryRequest entryRequest = new JournalEntryRequest(
                null,
                null
        );

        Set<ConstraintViolation<JournalEntryRequest>> violations = validator.validate(entryRequest);

        assertEquals(2, violations.size());
    }
}
