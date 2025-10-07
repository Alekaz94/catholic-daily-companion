package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.model.Feedback;

import java.util.List;

public class FeedbackDtoMapper {

    public static FeedbackDto toFeedbackDto (Feedback feedback) {
        if(feedback == null) {
            return null;
        }

        return new FeedbackDto(
                feedback.getId(),
                feedback.getCategory(),
                feedback.getMessage(),
                feedback.getEmail(),
                feedback.getSubmittedAt(),
                feedback.isFixed()
        );
    }

    public static List<FeedbackDto> toFeedbackDto (List<Feedback> feedbacks) {
        return feedbacks.stream().map(FeedbackDtoMapper::toFeedbackDto).toList();
    }
}
