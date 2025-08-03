package com.alexandros.dailycompanion.Model;

import com.alexandros.dailycompanion.Enum.Roles;
import com.alexandros.dailycompanion.Model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void testUserPersistence() {
        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Kazalis");
        user.setEmail("alex@example.com");
        user.setPassword("secret");
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);

        User savedUser = testEntityManager.persistAndFlush(user);

        assertNotNull(savedUser.getId());
        assertEquals("Alex", savedUser.getFirstName());
        assertEquals("Kazalis", savedUser.getLastName());
        assertEquals("alex@example.com", savedUser.getEmail());
        assertEquals(Roles.USER, savedUser.getRole());
    }

    @Test
    void testUserWithJournalEntries() {
        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Kazalis");
        user.setEmail("alex@example.com");
        user.setPassword("secret");
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setRole(Roles.USER);

        JournalEntry entry = new JournalEntry();
        entry.setTitle("My Day");
        entry.setContent("It was a good day.");
        entry.setUser(user);

        user.setJournalEntries(List.of(entry));

        User savedUser = testEntityManager.persistAndFlush(user);
        assertEquals(1, savedUser.getJournalEntries().size());
        assertEquals("My Day", savedUser.getJournalEntries().get(0).getTitle());
    }
}
