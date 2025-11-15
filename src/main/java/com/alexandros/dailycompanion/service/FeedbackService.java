package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.FeedbackRequest;
import com.alexandros.dailycompanion.dto.FeedbackUpdateRequest;
import com.alexandros.dailycompanion.mapper.FeedbackDtoMapper;
import com.alexandros.dailycompanion.model.Feedback;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {
    private final static Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    private final FeedbackRepository feedbackRepository;
    private final ServiceHelper serviceHelper;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, ServiceHelper serviceHelper) {
        this.feedbackRepository = feedbackRepository;
        this.serviceHelper = serviceHelper;
    }

    public void submitFeedback(UUID userId, FeedbackRequest feedbackRequest) {
        Feedback feedback = new Feedback();
        feedback.setCategory(feedbackRequest.category());
        feedback.setMessage(feedbackRequest.message());
        feedback.setEmail(feedbackRequest.email());
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setFixed(false);

        if(userId != null) {
            User user = serviceHelper.getUserByIdOrThrow(userId);
            feedback.setUser(user);
        }
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
}
