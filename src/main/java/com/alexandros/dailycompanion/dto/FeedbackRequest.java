package com.alexandros.dailycompanion.dto;

public record FeedbackRequest(String category,
                              String message,
                              String email,
                              boolean isFixed) {
}
