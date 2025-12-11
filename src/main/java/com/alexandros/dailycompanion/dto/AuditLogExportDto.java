/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogExportDto(
        UUID id,
        UUID userId,
        String action,
        String entityType,
        UUID entityId,
        String metadata,
        String ipAddress,
        LocalDateTime createdAt
) {}
