/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.dto.SaintListDto;
import com.alexandros.dailycompanion.model.Saint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaintRepository extends JpaRepository<Saint, UUID> {
    Optional<Saint> findByFeastDay(MonthDay monthDay);

    Optional<Saint> findByName(String name);

    Optional<Saint> findByPatronage(String patronage);

    Page<SaintListDto> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Saint> findAllByFeastDay(MonthDay monthDay);

    @Query("""
            SELECT new com.alexandros.dailycompanion.dto.SaintListDto(
                s.id,
                s.name,
                s.feastDay,
                s.imageUrl
            )
            FROM Saint s
            WHERE LOWER(s.name) LIKE LOWER(CONCAT('%',:query,'%'))
    """)
    Page<SaintListDto> findAllList(@Param("query") String query, Pageable pageable);
}
