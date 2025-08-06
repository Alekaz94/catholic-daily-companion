package com.alexandros.dailycompanion.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DailyReadingDto(UUID id,
                              LocalDate createdAt,
                              String firstReading,
                              String secondReading,
                              String psalm,
                              String gospel) {
}
