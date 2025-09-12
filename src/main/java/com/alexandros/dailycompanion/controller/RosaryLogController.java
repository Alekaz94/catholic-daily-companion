package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.service.RosaryLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rosary")
public class RosaryLogController {

    private final RosaryLogService rosaryLogService;

    @Autowired
    public RosaryLogController(RosaryLogService rosaryLogService) {
        this.rosaryLogService = rosaryLogService;
    }

    @PostMapping("/{userId}/complete")
    public ResponseEntity<RosaryLogDto> completeToday(@PathVariable UUID userId) {
        RosaryLogDto rosaryLog = rosaryLogService.markCompleted(userId);
        return ResponseEntity.ok(rosaryLog);
    }

    @PostMapping("/{userId}/completed-today")
    public ResponseEntity<Boolean> isCompletedToday(@PathVariable UUID userId) {
        boolean completed = rosaryLogService.isCompletedToday(userId);
        return ResponseEntity.ok(completed);
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<RosaryLogDto>> getHistory(@PathVariable UUID userId) {
        List<RosaryLogDto> logs = rosaryLogService.getHistory(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{userId}/streak")
    public ResponseEntity<Integer> getStreak(@PathVariable UUID userId) {
        int streak = rosaryLogService.getStreak(userId);
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/{userId}/rosary-dates")
    public ResponseEntity<List<String>> getRosaryDates(@PathVariable UUID userId) {
        List<String> completedDates = rosaryLogService.getCompletedDates(userId)
                .stream()
                .map(LocalDate::toString)
                .toList();
        return ResponseEntity.ok(completedDates);
    }

    @GetMapping("/{userId}/completed-on/{date}")
    public ResponseEntity<Boolean> isCompletedOn(@PathVariable UUID userId, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        boolean completed = rosaryLogService.isCompletedOn(userId, localDate);
        return ResponseEntity.ok(completed);
    }
}
