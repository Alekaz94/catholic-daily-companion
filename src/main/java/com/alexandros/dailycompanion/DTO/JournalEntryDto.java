package com.alexandros.dailycompanion.DTO;

import java.time.LocalDate;
import java.util.UUID;

public record JournalEntryDto(UUID id,
                              LocalDate date,
                              LocalDate updatedAt,
                              String title,
                              String content) {
}
