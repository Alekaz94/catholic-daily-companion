package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.model.Feedback;
import com.alexandros.dailycompanion.service.FeedbackService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

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
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
