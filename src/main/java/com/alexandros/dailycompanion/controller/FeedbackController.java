/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.dto.FeedbackUpdateRequest;
import com.alexandros.dailycompanion.service.FeedbackService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {
    private final static Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> submitFeedback(@RequestHeader(value = "user_id", required = false) UUID userId,
                                            @RequestBody FeedbackRequest feedbackRequest) {
        feedbackService.submitFeedback(userId, feedbackRequest);

        logger.info("POST /feedback | Feedback submitted | userId={} | category={}", userId, feedbackRequest.category());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<FeedbackDto>> getAllFeedback() {
        List<FeedbackDto> feedbacks = feedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDto> getSpecificFeedback(@PathVariable UUID id) {
        FeedbackDto feedbackDto = feedbackService.getSpecificFeedback(id);
        return ResponseEntity.ok(feedbackDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackDto> updateIsFixed(@PathVariable UUID id, @RequestBody FeedbackUpdateRequest feedbackUpdateRequest) {
        FeedbackDto feedbackDto = feedbackService.updateIsFixed(id, feedbackUpdateRequest);

        logger.info("PUT /feedback/{} | Updated 'fixed' status to {}", id, feedbackUpdateRequest.isFixed());
        return ResponseEntity.ok(feedbackDto);
    }

}
