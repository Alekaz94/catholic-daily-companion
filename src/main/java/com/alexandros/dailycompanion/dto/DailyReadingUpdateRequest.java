package com.alexandros.dailycompanion.dto;

public record DailyReadingUpdateRequest(String firstReading,
                                        String secondReading,
                                        String psalm,
                                        String gospel) {
}
