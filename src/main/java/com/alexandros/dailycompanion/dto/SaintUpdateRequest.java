/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

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
