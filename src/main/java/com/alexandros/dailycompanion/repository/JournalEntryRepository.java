/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.dto.JournalEntryLiteDto;
import com.alexandros.dailycompanion.model.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    @Query("SELECT new com.alexandros.dailycompanion.dto.JournalEntryLiteDto(j.id, j.createdAt, j.title) " +
            "FROM JournalEntry j WHERE j.user.id = :userId")
    Page<JournalEntryLiteDto> findAllLiteByUserId(@Param("userId") UUID userId, Pageable pageable);

    List<JournalEntry> findAllByUserId(UUID id);

    List<JournalEntry> findEntriesByUserEmailAndCreatedAt(String email, LocalDate createdAt);

    void deleteAllByUserId(UUID id);

    @Query("SELECT COUNT(j) FROM JournalEntry j WHERE j.user.id = :userId")
    int countByUserId(@Param("userId") UUID userId);
}
