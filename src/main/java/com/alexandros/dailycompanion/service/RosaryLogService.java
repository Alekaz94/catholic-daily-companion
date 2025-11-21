/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.enums.AuditAction;
import com.alexandros.dailycompanion.mapper.RosaryLogDtoMapper;
import com.alexandros.dailycompanion.model.RosaryLog;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.RosaryLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RosaryLogService {

    private final static Logger logger = LoggerFactory.getLogger(RosaryLogService.class);
    private final AuditLogService auditLogService;
    private final RosaryLogRepository rosaryLogRepository;
    private final ServiceHelper serviceHelper;

    @Autowired
    public RosaryLogService(AuditLogService auditLogService, RosaryLogRepository rosaryLogRepository, ServiceHelper serviceHelper) {
        this.auditLogService = auditLogService;
        this.rosaryLogRepository = rosaryLogRepository;
        this.serviceHelper = serviceHelper;
    }

    public RosaryLogDto markCompleted(UUID userId, String ipAddress) {
        LocalDate date = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        User user = serviceHelper.getUserByIdOrThrow(userId);

        RosaryLog rosaryLog = rosaryLogRepository
                .findByUserIdAndDate(userId, date)
                .orElse(new RosaryLog(user, date, true));

        boolean alreadyCompleted = rosaryLog.isCompleted();

        rosaryLog.setCompleted(true);
        rosaryLogRepository.save(rosaryLog);

        auditLogService.logAction(
                userId,
                AuditAction.MARK_ROSARY_COMPLETE.name(),
                "RosaryLog",
                rosaryLog.getId(),
                String.format("{\"user\": \"%s\", \"date\": \"%s\", \"alreadyCompleted\": %b}", userId, rosaryLog.getDate(), alreadyCompleted),
                ipAddress);
        logger.info("Marked rosary log as complete '{}' for user {}", date, user.getId());
        return RosaryLogDtoMapper.toRosaryDto(rosaryLog);
    }

    public boolean isCompletedToday(UUID userId) {
        LocalDate date = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        return rosaryLogRepository
                .findByUserIdAndDate(userId, date)
                .map(RosaryLog::isCompleted)
                .orElse(false);
    }

    public List<RosaryLogDto> getHistory(UUID userId) {
        List<RosaryLog> logs = rosaryLogRepository.findAllByUserIdOrderByDateDesc(userId);
        return RosaryLogDtoMapper.toRosaryLogDto(logs);
    }

    public int getStreak(UUID userId) {
        List<RosaryLog> logs = rosaryLogRepository.findAllByUserIdAndCompletedTrueOrderByDateDesc(userId);
        int streak = 0;
        LocalDate expectedDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();

        for(RosaryLog log : logs) {
            if(log.getDate().isEqual(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    public List<LocalDate> getCompletedDates(UUID userId) {
        return rosaryLogRepository.findAllByUserIdAndCompletedTrue(userId)
                .stream()
                .map(RosaryLog::getDate)
                .toList();
    }

    public boolean isCompletedOn(UUID userId, LocalDate date) {
        return rosaryLogRepository.existsByUserIdAndDate(userId, date);
    }
}
