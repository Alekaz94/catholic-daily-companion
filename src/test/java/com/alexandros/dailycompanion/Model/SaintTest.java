package com.alexandros.dailycompanion.Model;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.MonthDay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SaintTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void testSaintPersistence() {
        Saint saint = new Saint();
        saint.setName("St Francis");
        saint.setBirthYear(200);
        saint.setDeathYear(250);
        saint.setFeastDay(MonthDay.now());
        saint.setBiography("Saint of the Catholic church");
        saint.setPatronage("Animals and environment");
        saint.setCanonizationYear(506);
        saint.setImageUrl("http://saint-francis.com");

        Saint savedSaint = testEntityManager.persistAndFlush(saint);

        assertNotNull(savedSaint);
        assertEquals("St Francis", savedSaint.getName());
        assertEquals(200, savedSaint.getBirthYear());
        assertEquals(250, savedSaint.getDeathYear());
        assertEquals(MonthDay.now(), savedSaint.getFeastDay());
        assertEquals("Saint of the Catholic church", savedSaint.getBiography());
        assertEquals("Animals and environment", savedSaint.getPatronage());
        assertEquals(506, savedSaint.getCanonizationYear());
        assertEquals("http://saint-francis.com", savedSaint.getImageUrl());
    }
}
