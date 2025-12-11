/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import java.time.MonthDay;
import java.util.UUID;

public record SaintListDto(UUID id,
                           String name,
                           MonthDay feastDay,
                           String imageUrl) {
}
