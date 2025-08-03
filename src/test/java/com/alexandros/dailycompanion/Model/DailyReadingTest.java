package com.alexandros.dailycompanion.Model;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DailyReadingTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void testDailyReadingPersistence() {
        DailyReading reading = new DailyReading();
        reading.setCreatedAt(LocalDate.now());
        reading.setFirstReading("Exodus");
        reading.setSecondReading("Hebrews");
        reading.setPsalm("Psalm 123");
        reading.setGospel("John");

        DailyReading savedReading = testEntityManager.persistAndFlush(reading);

        assertNotNull(savedReading);
        assertEquals(LocalDate.now(), savedReading.getCreatedAt());
        assertEquals("Exodus", savedReading.getFirstReading());
        assertEquals("Hebrews", savedReading.getSecondReading());
        assertEquals("Psalm 123", savedReading.getPsalm());
        assertEquals("John", savedReading.getGospel());
    }
}
