package com.alexandros.dailycompanion.Model;

import com.alexandros.dailycompanion.Enum.Roles;
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
public class JournalEntryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void testJournalEntryPersistence() {
        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Kazalis");
        user.setEmail("alex@example.com");
        user.setPassword("secret");
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);

        testEntityManager.persist(user);

        JournalEntry entry = new JournalEntry();
        entry.setCreatedAt(LocalDate.now());
        entry.setUpdatedAt(LocalDate.now());
        entry.setTitle("Hello there");
        entry.setContent("Obi-wan Kenobi");
        entry.setUser(user);

        JournalEntry savedEntry = testEntityManager.persistAndFlush(entry);
        assertNotNull(savedEntry.getId());
        assertEquals("Hello there", savedEntry.getTitle());
        assertEquals("Obi-wan Kenobi", savedEntry.getContent());
    }

    @Test
    void testJournalEntriesWithUser() {
        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Kazalis");
        user.setEmail("alex@example.com");
        user.setPassword("secret");
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);

        User savedUser = testEntityManager.persistFlushFind(user);

        JournalEntry entry = new JournalEntry();
        entry.setTitle("My Day");
        entry.setContent("It was a good day.");
        entry.setUser(user);

        JournalEntry savedEntry = testEntityManager.persistAndFlush(entry);

        assertNotNull(savedEntry.getUser());
        assertEquals("Alex", savedEntry.getUser().getFirstName());

        savedUser = testEntityManager.find(User.class, savedUser.getId());
        assertEquals(1, savedUser.getJournalEntries().size());
        assertEquals("My Day", savedUser.getJournalEntries().get(0).getTitle());
    }

}
