package com.alexandros.dailycompanion.Repository;

import com.alexandros.dailycompanion.Model.JournalEntry;
import com.alexandros.dailycompanion.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findAllByUserId(UUID id);
}
