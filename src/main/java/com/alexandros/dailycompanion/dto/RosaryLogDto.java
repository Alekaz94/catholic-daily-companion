/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RosaryLogDto(UUID id,
                           LocalDate date,
                           boolean completed) {
}
