package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.dto.FeedbackUpdateRequest;
import com.alexandros.dailycompanion.exception.GlobalExceptionHandler;
import com.alexandros.dailycompanion.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FeedbackControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private FeedbackController feedbackController;

    private FeedbackDto feedbackDto;
    private UUID feedbackId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(feedbackController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        feedbackId = UUID.randomUUID();
        feedbackDto = new FeedbackDto(feedbackId, "Bug", "Something is broken", "test@email.com", LocalDateTime.now(), false);
    }

    @Test
    void submitFeedback_success() throws Exception {
        FeedbackRequest request = new FeedbackRequest("Bug", "Something is broken", "test@email.com", false);

        mockMvc.perform(post("/api/v1/feedback")
                        .header("user_id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"Bug\",\"message\":\"Something is broken\",\"email\":\"test@email.com\",\"isFixed\":\"false\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllFeedback_success() throws Exception {
        when(feedbackService.getAllFeedback()).thenReturn(List.of(feedbackDto));

        mockMvc.perform(get("/api/v1/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Bug"))
                .andExpect(jsonPath("$[0].message").value("Something is broken"))
                .andExpect(jsonPath("$[0].email").value("test@email.com"))
                .andExpect(jsonPath("$[0].isFixed").value(false));
    }

    @Test
    void getSpecificFeedback_success() throws Exception {
        when(feedbackService.getSpecificFeedback(feedbackId)).thenReturn(feedbackDto);

        mockMvc.perform(get("/api/v1/feedback/{id}", feedbackId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Bug"))
                .andExpect(jsonPath("$.message").value("Something is broken"));
    }

    @Test
    void getSpecificFeedback_notFound_shouldReturn404() throws Exception {
        UUID feedbackId = UUID.randomUUID();
        when(feedbackService.getSpecificFeedback(feedbackId))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Feedback not found"));

        mockMvc.perform(get("/api/v1/feedback/{id}", feedbackId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Feedback not found"));
    }

    @Test
    void updateIsFixed_success() throws Exception {
        FeedbackUpdateRequest updateRequest = new FeedbackUpdateRequest(true);
        FeedbackDto updatedDto = new FeedbackDto(feedbackId, "UI", "Something is broken", "test@email.com", LocalDateTime.now(), true);

        when(feedbackService.updateIsFixed(feedbackId, updateRequest)).thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/feedback/{id}", feedbackId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isFixed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFixed").value(true));
    }

    @Test
    void updateIsFixed_notFound_shouldReturn404() throws Exception {
        UUID feedbackId = UUID.randomUUID();
        when(feedbackService.updateIsFixed(eq(feedbackId), any()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Feedback not found"));

        mockMvc.perform(put("/api/v1/feedback/{id}", feedbackId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isFixed\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Feedback not found"));
    }
}
