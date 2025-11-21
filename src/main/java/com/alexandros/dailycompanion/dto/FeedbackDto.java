/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackDto(UUID id,
                          String category,
                          String message,
                          String email,
                          LocalDateTime submittedAt,
                          boolean isFixed) {
}
