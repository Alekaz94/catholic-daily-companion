package com.alexandros.dailycompanion.dto;

import java.time.MonthDay;

public record SaintUpdateRequest(String name,
                                 Integer birthYear,
                                 Integer deathYear,
                                 MonthDay feastDay,
                                 String biography,
                                 String patronage,
                                 Integer canonizationYear,
                                 String imageUrl,
                                 String imageSource,
                                 String imageAuthor,
                                 String imageLicence) {
}
