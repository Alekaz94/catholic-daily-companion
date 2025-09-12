package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.JournalEntry;
import com.alexandros.dailycompanion.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Page<JournalEntry> findAllByUserId(UUID id, Pageable pageable);

    List<JournalEntry> findAllByUserId(UUID id);

    List<JournalEntry> findEntriesByUserEmailAndCreatedAt(String email, LocalDate createdAt);
}
