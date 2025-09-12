package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.Saint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Page<Saint> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Saint> findAllByFeastDay(MonthDay monthDay);
}
