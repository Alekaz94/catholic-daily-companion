package com.alexandros.dailycompanion.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.MonthDay;

public record SaintRequest(@NotNull @Size(min = 1) String name,
                           @NotNull Integer birthYear,
                           @NotNull Integer deathYear,
                           @NotNull MonthDay feastDay,
                           @NotNull @Size(min = 10) String biography,
                           @NotNull String patronage,
                           @NotNull Integer canonizationYear,
                           String imageUrl) {
}
