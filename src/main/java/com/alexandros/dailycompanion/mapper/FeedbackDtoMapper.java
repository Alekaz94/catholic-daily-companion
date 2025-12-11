/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.FeedbackDto;
import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.model.Feedback;
import com.alexandros.dailycompanion.model.JournalEntry;
import org.springframework.data.domain.Page;

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

    public static Page<FeedbackDto> toFeedbackDto(Page<Feedback> entries) {
        return entries.map(FeedbackDtoMapper::toFeedbackDto);
    }
}
