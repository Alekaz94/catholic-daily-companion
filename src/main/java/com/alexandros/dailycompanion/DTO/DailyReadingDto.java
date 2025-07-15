package com.alexandros.dailycompanion.DTO;

import java.time.LocalDate;
import java.util.UUID;

public record DailyReadingDto(UUID id,
                              LocalDate createdAt,
                              LocalDate updatedAt,
                              String firstReading,
                              String secondReading,
                              String psalm,
                              String gospel) {
}
