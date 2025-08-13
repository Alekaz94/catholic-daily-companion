package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.RosaryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RosaryLogRepository extends JpaRepository<RosaryLog, UUID> {
    Optional<RosaryLog> findByUserIdAndDate(UUID userId, LocalDate date);

    List<RosaryLog> findAllByUserIdOrderByDateDesc(UUID userId);

    List<RosaryLog> findAllByUserIdAndCompletedTrueOrderByDateDesc(UUID userId);
}
