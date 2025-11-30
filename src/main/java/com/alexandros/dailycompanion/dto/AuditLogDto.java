/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import java.time.LocalDateTime;

public record AuditLogDto(String action,
                          String entityType,
                          LocalDateTime createdAt,
                          String metadata) {
}
