package com.alexandros.dailycompanion.Repository;

import com.alexandros.dailycompanion.Model.DailyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyReadingRepository extends JpaRepository<DailyReading, UUID> {
    Optional<DailyReading> findByCreatedAt(LocalDate localDate);
}
