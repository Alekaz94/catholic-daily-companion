/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final JournalEntryService journalEntryService;
    private final RosaryLogService rosaryLogService;
    private final ServiceHelper serviceHelper;
    private final UserService userService;
    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    @Autowired
    public AdminService(JournalEntryService journalEntryService, RosaryLogService rosaryLogService, ServiceHelper serviceHelper, UserService userService, FeedbackService feedbackService, UserRepository userRepository) {
        this.journalEntryService = journalEntryService;
        this.rosaryLogService = rosaryLogService;
        this.serviceHelper = serviceHelper;
        this.userService = userService;
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    public Page<AdminUserListDto> getAllUsersForAdmin(String query, int page, int size, String sortBy, String  sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        if(query == null || query.trim().isEmpty()) {
            return userRepository.findAllUsersForAdmin(pageable);
        } else {
            return userRepository.searchUsersForAdmin(query, pageable);
        }
    }

    public AdminUserOverviewDto getUserOverview(UUID userId, int feedbackPage, int feedbackSize, String feedbackSort) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();

        if (!currentUser.getRole().equals(Roles.ADMIN) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's data.");
        }

        UserDto userDto = userService.getUser(userId);

        int feedbackCount = feedbackService.getFeedbackCountByUserEmail(userId);

        Page<FeedbackDto> feedbacks = feedbackService.getAllFeedbackByUserEmail(userId, feedbackPage, feedbackSize, feedbackSort);

        return new AdminUserOverviewDto(
                userDto,
                feedbackCount,
                feedbacks
        );
    }
}
