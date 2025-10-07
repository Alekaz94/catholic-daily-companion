package com.alexandros.dailycompanion.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackDto(UUID id,
                          String category,
                          String message,
                          String email,
                          LocalDateTime submittedAt,
                          boolean isFixed) {
}
