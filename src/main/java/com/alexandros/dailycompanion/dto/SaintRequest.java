package com.alexandros.dailycompanion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.MonthDay;

public record SaintRequest(@NotNull @Size(min = 2) String name,
                           Integer birthYear,
                           Integer deathYear,
                           @NotNull MonthDay feastDay,
                           @NotNull @Size(min = 10) String biography,
                           @NotNull String patronage,
                           Integer canonizationYear,
                           String imageUrl,
                           String imageSource,
                           String imageAuthor,
                           String imageLicence) {
}
