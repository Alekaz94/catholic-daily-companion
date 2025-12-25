/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.dto.FeedbackUpdateRequest;
import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.service.FeedbackService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller responsible for handling user feedback.
 * <p>
 * Provides endpoints for:
 * <ul>
 *     <li>Submitting feedback from users</li>
 *     <li>Retrieving feedback entries with pagination</li>
 *     <li>Viewing individual feedback details</li>
 *     <li>Updating feedback resolution status</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {
    private final static Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackService feedbackService;
    private final ServiceHelper serviceHelper;

    @Autowired
    public FeedbackController(FeedbackService feedbackService, ServiceHelper serviceHelper) {
        this.feedbackService = feedbackService;
        this.serviceHelper = serviceHelper;
    }

    /**
     * Submits new feedback to the system.
     * <p>
     * Feedback can be submitted by authenticated users or anonymously.
     * Client IP address is recorded for moderation and analytics purposes.
     *
     * @param userId          optional user identifier from request header
     * @param feedbackRequest feedback content and category
     * @param request         HTTP servlet request used to extract client IP
     * @return {@code 201 Created} if feedback is successfully submitted
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Void> submitFeedback(@RequestHeader(value = "user_id", required = false) UUID userId,
                                               @RequestBody FeedbackRequest feedbackRequest,
                                               HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        feedbackService.submitFeedback(userId, feedbackRequest, ipAddress);

        logger.info("POST /feedback | Feedback submitted | userId={} | category={}", userId, feedbackRequest.category());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieves a paginated list of all feedback entries.
     * <p>
     * Intended for administrative or moderation use.
     *
     * @param page zero-based page index (default: 0)
     * @param size number of feedback items per page (default: 10)
     * @param sort sort direction, either {@code asc} or {@code desc} (default: desc)
     * @return paginated response containing feedback entries
     */
    @GetMapping
    public ResponseEntity<PageResponse<FeedbackDto>> getAllFeedback(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "desc") String sort) {
        Page<FeedbackDto> feedback = feedbackService.getAllFeedback(page, size, sort);

        PageResponse<FeedbackDto> response = new PageResponse<>(
                feedback.getContent(),
                feedback.getNumber(),
                feedback.getSize(),
                feedback.getTotalElements(),
                feedback.getTotalPages(),
                feedback.isLast()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details for a specific feedback entry.
     *
     * @param id unique identifier of the feedback entry
     * @return feedback details
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDto> getSpecificFeedback(@PathVariable UUID id) {
        FeedbackDto feedbackDto = feedbackService.getSpecificFeedback(id);
        return ResponseEntity.ok(feedbackDto);
    }

    /**
     * Updates the resolution status of a feedback entry.
     * <p>
     * Typically used by administrators to mark feedback as fixed or unresolved.
     *
     * @param id                     unique identifier of the feedback entry
     * @param feedbackUpdateRequest  request containing updated resolution status
     * @return updated feedback details
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackDto> updateIsFixed(@PathVariable UUID id, @RequestBody FeedbackUpdateRequest feedbackUpdateRequest) {
        FeedbackDto feedbackDto = feedbackService.updateIsFixed(id, feedbackUpdateRequest);

        logger.info("PUT /feedback/{} | Updated 'fixed' status to {}", id, feedbackUpdateRequest.isFixed());
        return ResponseEntity.ok(feedbackDto);
    }

}
