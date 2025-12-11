/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.RosaryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RosaryLogRepository extends JpaRepository<RosaryLog, UUID> {
    Optional<RosaryLog> findByUserIdAndDate(UUID userId, LocalDate date);

    List<RosaryLog> findAllByUserIdOrderByDateDesc(UUID userId);

    Page<RosaryLog> findAllByUserIdAndCompletedTrue(UUID userId, Pageable pageable);

    boolean existsByUserIdAndDate(UUID userId, LocalDate date);

    void deleteAllByUserId(UUID id);

    @Query("""
            SELECT r
            FROM RosaryLog r
            WHERE r.user.id = :userId
            AND r.completed = true
            ORDER BY r.date DESC
            """)
    List<RosaryLog> findCompletedLogsDesc(UUID userId);

    @Query("SELECT COUNT(r) FROM RosaryLog r WHERE r.user.id = :userId AND r.completed = true")
    int countCompletedByUserId(@Param("userId") UUID userId);
}
