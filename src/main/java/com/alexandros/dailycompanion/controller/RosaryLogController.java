/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.service.RosaryLogService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller responsible for tracking and retrieving Rosary completion data.
 * <p>
 * Supports daily completion tracking, streak calculation, and historical lookup
 * for authenticated users.
 */
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

    /**
     * Marks the Rosary as completed for the current day.
     *
     * @param userId  user identifier
     * @param request HTTP request used to extract client IP
     * @return created rosary log entry
     */
    @PostMapping("/{userId}/complete")
    public ResponseEntity<RosaryLogDto> completeToday(@PathVariable UUID userId, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        User user = serviceHelper.getAuthenticatedUser();
        RosaryLogDto rosaryLog = rosaryLogService.markCompleted(userId, ipAddress);
        logger.info("POST /rosary/{}/complete | user={} | ip={}", userId, user.getId(), ipAddress);
        return ResponseEntity.ok(rosaryLog);
    }

    /**
     * Checks whether the user has completed the Rosary today.
     *
     * @param userId user identifier
     * @return {@code true} if completed today, otherwise {@code false}
     */
    @PostMapping("/{userId}/completed-today")
    public ResponseEntity<Boolean> isCompletedToday(@PathVariable UUID userId) {
        boolean completed = rosaryLogService.isCompletedToday(userId);
        return ResponseEntity.ok(completed);
    }

    /**
     * Retrieves paginated Rosary completion history for a user.
     *
     * @param userId user identifier
     * @param page   zero-based page index (default: 0)
     * @param size   page size (default: 20)
     * @param sort   sort direction ({@code asc} or {@code desc})
     * @return paginated rosary history
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<PageResponse<RosaryLogDto>> getHistory(@PathVariable UUID userId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 @RequestParam(defaultValue = "desc") String sort) {

        Page<RosaryLogDto> logs = rosaryLogService.getHistory(userId, page, size, sort);

        PageResponse<RosaryLogDto> response = new PageResponse<>(
                logs.getContent(),
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages(),
                logs.isLast()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the current Rosary completion streak for a user.
     *
     * @param userId user identifier
     * @return number of consecutive days completed
     */
    @GetMapping("/{userId}/streak")
    public ResponseEntity<Integer> getStreak(@PathVariable UUID userId) {
        int streak = rosaryLogService.getStreak(userId);
        return ResponseEntity.ok(streak);
    }

    /**
     * Retrieves all dates on which the user completed the Rosary.
     *
     * @param userId user identifier
     * @return list of ISO-8601 formatted dates
     */
    @GetMapping("/{userId}/rosary-dates")
    public ResponseEntity<List<String>> getRosaryDates(@PathVariable UUID userId) {
        List<String> completedDates = rosaryLogService.getCompletedDates(userId)
                .stream()
                .map(LocalDate::toString)
                .toList();
        return ResponseEntity.ok(completedDates);
    }

    /**
     * Checks whether the Rosary was completed on a specific date.
     *
     * @param userId user identifier
     * @param date   date in {@code yyyy-MM-dd} format
     * @return {@code true} if completed on the given date
     */
    @GetMapping("/{userId}/completed-on/{date}")
    public ResponseEntity<Boolean> isCompletedOn(@PathVariable UUID userId, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        boolean completed = rosaryLogService.isCompletedOn(userId, localDate);
        return ResponseEntity.ok(completed);
    }
}
