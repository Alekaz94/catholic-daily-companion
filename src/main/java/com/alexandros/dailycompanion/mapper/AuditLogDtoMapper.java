/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.AuditLogDto;
import com.alexandros.dailycompanion.model.AuditLog;

import java.util.List;


public class AuditLogDtoMapper {

    public static AuditLogDto toAuditLogDto(AuditLog auditLog) {
        if(auditLog == null) {
            return null;
        }

        return new AuditLogDto(
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getCreatedAt(),
                auditLog.getMetaData()
        );
    }

    public static List<AuditLogDto> toAuditLogDto(List<AuditLog> auditLogs) {
        return auditLogs.stream().map(AuditLogDtoMapper::toAuditLogDto).toList();
    }
}
