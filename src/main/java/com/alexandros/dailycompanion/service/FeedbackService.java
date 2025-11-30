/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.dto.FeedbackUpdateRequest;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.FeedbackDtoMapper;
import com.alexandros.dailycompanion.model.Feedback;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {
    private final static Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    private final FeedbackRepository feedbackRepository;
    private final ServiceHelper serviceHelper;
    private final AuditLogService auditLogService;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, ServiceHelper serviceHelper, AuditLogService auditLogService) {
        this.feedbackRepository = feedbackRepository;
        this.serviceHelper = serviceHelper;
        this.auditLogService = auditLogService;
    }

    public void submitFeedback(UUID userId, FeedbackRequest feedbackRequest, String ipAddress) {
        User user = serviceHelper.getAuthenticatedUser();

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setCategory(feedbackRequest.category());
        feedback.setMessage(feedbackRequest.message());
        feedback.setEmail(feedbackRequest.email());
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setFixed(false);

        if(userId != null) {
            User currentUser = serviceHelper.getUserByIdOrThrow(userId);
            feedback.setUser(user);
        }

        auditLogService.logAction(
                userId,
                "FEEDBACK SUBMITTED",
                "Feedback",
                feedback.getId(),
                "Feedback submitted",
                ipAddress
        );

        feedbackRepository.save(feedback);

        logger.info("Feedback created | id={} | user={} | category={}", feedback.getId(), userId, feedbackRequest.category());
    }

    public List<FeedbackDto> getAllFeedback() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return FeedbackDtoMapper.toFeedbackDto(feedbacks);
    }

    public FeedbackDto getSpecificFeedback(UUID id) {
        Feedback feedback = feedbackRepository.findById(id).orElseThrow(()
                -> new IllegalArgumentException("Could not find feedback!"));
        return FeedbackDtoMapper.toFeedbackDto(feedback);
    }

    public FeedbackDto updateIsFixed(UUID id, FeedbackUpdateRequest feedbackUpdateRequest) {
        Feedback feedback = feedbackRepository.findById(id).orElseThrow(()
                -> new IllegalArgumentException("Could not find feedback!"));

        feedback.setFixed(feedbackUpdateRequest.isFixed());
        feedbackRepository.save(feedback);

        logger.info("Feedback updated | id={} | fixed={}", id, feedbackUpdateRequest.isFixed());
        return FeedbackDtoMapper.toFeedbackDto(feedback);
    }

    public int getFeedbackCountByUserEmail(UUID userId) throws AccessDeniedException {
        User user = serviceHelper.getAuthenticatedUser();
        User currentUser = serviceHelper.getUserByIdOrThrow(userId);

        if(!user.getRole().equals(Roles.ADMIN) && !user.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's data.");
        }

        return feedbackRepository.findAllByUserEmail(currentUser.getEmail()).size();
    }

    public List<FeedbackDto> getAllFeedbackByUserEmail(UUID userId) throws AccessDeniedException {
        User user = serviceHelper.getAuthenticatedUser();
        User currentUser = serviceHelper.getUserByIdOrThrow(userId);

        if(!user.getRole().equals(Roles.ADMIN) && !user.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's data.");
        }

        List<Feedback> feedbacks = feedbackRepository.findAllByUserEmail(currentUser.getEmail());
        return FeedbackDtoMapper.toFeedbackDto(feedbacks);
    }
}
