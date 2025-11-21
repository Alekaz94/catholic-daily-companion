/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.RosaryLog;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.RosaryLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RosaryLogServiceTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RosaryLogRepository rosaryLogRepository;

    @Mock
    private ServiceHelper serviceHelper;

    @InjectMocks
    private RosaryLogService rosaryLogService;

    private User user;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRole(Roles.USER);
    }

    @Test
    void markCompleted_ShouldCreateNewLogIfNoneExists() {
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);
        when(rosaryLogRepository.findByUserIdAndDate(user.getId(), today))
                .thenReturn(Optional.empty());
        when(rosaryLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RosaryLogDto result = rosaryLogService.markCompleted(user.getId(), "127.0.0.1");

        assertTrue(result.completed());
        assertEquals(today, result.date());
    }

    @Test
    void markCompleted_ShouldUpdateExistingLogIfAlreadyCompleted() {
        RosaryLog existing = new RosaryLog(user, today, true);
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);
        when(rosaryLogRepository.findByUserIdAndDate(user.getId(), today))
                .thenReturn(Optional.of(existing));
        when(rosaryLogRepository.save(any())).thenReturn(existing);

        RosaryLogDto result = rosaryLogService.markCompleted(user.getId(), "127.0.0.1");

        assertTrue(result.completed());
        verify(rosaryLogRepository, times(1)).save(existing);
    }

    @Test
    void markCompleted_ShouldMarkIncompleteLogAsCompleted() {
        RosaryLog existing = new RosaryLog(user, today, false);
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);
        when(rosaryLogRepository.findByUserIdAndDate(user.getId(), today))
                .thenReturn(Optional.of(existing));
        when(rosaryLogRepository.save(any())).thenReturn(existing);

        RosaryLogDto result = rosaryLogService.markCompleted(user.getId(), "127.0.0.1");

        assertTrue(result.completed());
    }

    @Test
    void isCompletedToday_ShouldReturnTrueWhenCompleted() {
        RosaryLog log = new RosaryLog(user, today, true);
        when(rosaryLogRepository.findByUserIdAndDate(user.getId(), today))
                .thenReturn(Optional.of(log));

        boolean result = rosaryLogService.isCompletedToday(user.getId());
        assertTrue(result);
    }

    @Test
    void isCompletedToday_ShouldReturnFalseWhenNotCompleted() {
        when(rosaryLogRepository.findByUserIdAndDate(user.getId(), today))
                .thenReturn(Optional.empty());

        boolean result = rosaryLogService.isCompletedToday(user.getId());
        assertFalse(result);
    }

    @Test
    void getHistory_ShouldReturnMappedDtos() {
        RosaryLog log = new RosaryLog(user, today, true);
        when(rosaryLogRepository.findAllByUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(log));

        List<RosaryLogDto> result = rosaryLogService.getHistory(user.getId());

        assertEquals(1, result.size());
        assertEquals(today, result.getFirst().date());
    }

    @Test
    void getStreak_ShouldReturnCorrectStreak() {
        LocalDate d1 = today;
        LocalDate d2 = today.minusDays(1);
        LocalDate d3 = today.minusDays(2);

        RosaryLog log1 = new RosaryLog(user, d1, true);
        RosaryLog log2 = new RosaryLog(user, d2, true);
        RosaryLog log3 = new RosaryLog(user, d3, true);

        when(rosaryLogRepository.findAllByUserIdAndCompletedTrueOrderByDateDesc(user.getId()))
                .thenReturn(List.of(log1, log2, log3));

        int streak = rosaryLogService.getStreak(user.getId());

        assertEquals(3, streak);
    }

    @Test
    void getStreak_ShouldBreakWhenGapOccurs() {
        LocalDate d1 = today;
        LocalDate d2 = today.minusDays(1);
        LocalDate d4 = today.minusDays(3);

        RosaryLog log1 = new RosaryLog(user, d1, true);
        RosaryLog log2 = new RosaryLog(user, d2, true);
        RosaryLog logGap = new RosaryLog(user, d4, true);

        when(rosaryLogRepository.findAllByUserIdAndCompletedTrueOrderByDateDesc(user.getId()))
                .thenReturn(List.of(log1, log2, logGap));

        int streak = rosaryLogService.getStreak(user.getId());

        assertEquals(2, streak);
    }

    @Test
    void getCompletedDates_ShouldReturnListOfDates() {
        RosaryLog log1 = new RosaryLog(user, today, true);
        RosaryLog log2 = new RosaryLog(user, today.minusDays(1), true);

        when(rosaryLogRepository.findAllByUserIdAndCompletedTrue(user.getId()))
                .thenReturn(List.of(log1, log2));

        List<LocalDate> result = rosaryLogService.getCompletedDates(user.getId());

        assertEquals(2, result.size());
    }

    @Test
    void isCompletedOn_ShouldReturnTrueIfCompleted() {
        when(rosaryLogRepository.existsByUserIdAndDate(user.getId(), today))
                .thenReturn(true);

        assertTrue(rosaryLogService.isCompletedOn(user.getId(), today));
    }

    @Test
    void isCompletedOn_ShouldReturnFalseIfNotCompleted() {
        when(rosaryLogRepository.existsByUserIdAndDate(user.getId(), today))
                .thenReturn(false);

        assertFalse(rosaryLogService.isCompletedOn(user.getId(), today));
    }
}
