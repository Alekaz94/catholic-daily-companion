/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.dto.FeedbackUpdateRequest;
import com.alexandros.dailycompanion.model.Feedback;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackRequest feedbackRequest;
    private User user;
    private Feedback feedback;

    @BeforeEach
    void setUp() {
        feedbackRequest = new FeedbackRequest("Bug", "Something broke", "user@mail.com", false);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@mail.com");

        feedback = new Feedback();
        feedback.setId(UUID.randomUUID());
        feedback.setMessage("Something broke");
        feedback.setCategory("Bug");
        feedback.setEmail("user@mail.com");
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setFixed(false);
    }

    @Test
    void submitFeedbackWithUser() {
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);

        feedbackService.submitFeedback(user.getId(), feedbackRequest, "127.0.0.1");
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    void submitFeedbackWithoutUser() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);

        feedbackService.submitFeedback(null, feedbackRequest, "127.0.0.1");
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    void getAllFeedback() {
        when(feedbackRepository.findAll()).thenReturn(List.of(feedback));

        List<FeedbackDto> result = feedbackService.getAllFeedback();

        assertEquals(1, result.size());
        assertEquals("Bug", result.get(0).category());
    }

    @Test
    void getSpecificFeedback() {
        when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));
        FeedbackDto feedbackDto = feedbackService.getSpecificFeedback(feedback.getId());
        assertEquals("Bug", feedbackDto.category());
    }

    @Test
    void getSpecificFeedbackNotFound() {
        when(feedbackRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> feedbackService.getSpecificFeedback(UUID.randomUUID()));
    }

    @Test
    void updateIsFixed() {
        when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));

        FeedbackUpdateRequest updateRequest = new FeedbackUpdateRequest(true);
        FeedbackDto feedbackDto = feedbackService.updateIsFixed(feedback.getId(), updateRequest);

        assertTrue(feedbackDto.isFixed());
        verify(feedbackRepository, times(1)).save(feedback);
    }
}
