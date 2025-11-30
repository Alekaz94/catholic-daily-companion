/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.AdminUserOverviewDto;
import com.alexandros.dailycompanion.dto.AuditLogDto;
import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.Feedback;
import com.alexandros.dailycompanion.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final JournalEntryService journalEntryService;
    private final RosaryLogService rosaryLogService;
    private final AuditLogService auditLogService;
    private final ServiceHelper serviceHelper;
    private final UserService userService;
    private final FeedbackService feedbackService;

    @Autowired
    public AdminService(JournalEntryService journalEntryService, RosaryLogService rosaryLogService, AuditLogService auditLogService, ServiceHelper serviceHelper, UserService userService, FeedbackService feedbackService) {
        this.journalEntryService = journalEntryService;
        this.rosaryLogService = rosaryLogService;
        this.auditLogService = auditLogService;
        this.serviceHelper = serviceHelper;
        this.userService = userService;
        this.feedbackService = feedbackService;
    }

    public AdminUserOverviewDto getUserOverview(UUID userId) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();

        if (!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's data.");
        }

        UserDto userDto = userService.getUser(userId);

        int journalCount = journalEntryService.getAmountOfEntries(userId);
        int rosaryCount = rosaryLogService.getAmountOfPrayedRosaries(userId);
        int feedbackCount = feedbackService.getFeedbackCountByUserEmail(userId);
        List<FeedbackDto> feedbacks = feedbackService.getAllFeedbackByUserEmail(userId);
        List<LocalDate> rosaryDates = rosaryLogService.getCompletedDates(userId);
        List<AuditLogDto> auditLogs = auditLogService.getAllAuditLogsForUser(userId);

        return new AdminUserOverviewDto(
                userDto,
                journalCount,
                rosaryCount,
                feedbackCount,
                feedbacks,
                rosaryDates,
                auditLogs
        );
    }
}
