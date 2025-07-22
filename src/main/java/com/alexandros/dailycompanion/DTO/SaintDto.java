package com.alexandros.dailycompanion.DTO;

import java.time.MonthDay;
import java.util.UUID;

public record SaintDto(UUID id,
                       String name,
                       Integer birthYear,
                       Integer deathYear,
                       MonthDay feastDay,
                       String biography,
                       String patronage,
                       Integer canonizationYear,
                       String imageUrl) {
}
