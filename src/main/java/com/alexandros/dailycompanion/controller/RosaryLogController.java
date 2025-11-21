/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.service.RosaryLogService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rosary")
public class RosaryLogController {

    private final static Logger logger = LoggerFactory.getLogger(RosaryLogController.class);
    private final RosaryLogService rosaryLogService;
    private final ServiceHelper serviceHelper;

    @Autowired
    public RosaryLogController(RosaryLogService rosaryLogService, ServiceHelper serviceHelper) {
        this.rosaryLogService = rosaryLogService;
        this.serviceHelper = serviceHelper;
    }

    @PostMapping("/{userId}/complete")
    public ResponseEntity<RosaryLogDto> completeToday(@PathVariable UUID userId, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        User user = serviceHelper.getAuthenticatedUser();
        RosaryLogDto rosaryLog = rosaryLogService.markCompleted(userId, ipAddress);
        logger.info("POST /rosary/{}/complete | user={} | ip={}", userId, user.getId(), ipAddress);
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
