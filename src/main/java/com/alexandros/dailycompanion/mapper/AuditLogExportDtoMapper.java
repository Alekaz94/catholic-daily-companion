/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.AuditLogExportDto;
import com.alexandros.dailycompanion.model.AuditLog;

import java.util.List;

public class AuditLogExportDtoMapper {

    public static AuditLogExportDto toAuditLogExportDto(AuditLog auditLog) {
        if(auditLog == null) {
            return null;
        }

        return new AuditLogExportDto(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getMetaData(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }

    public static List<AuditLogExportDto> toAuditLogExportDto(List<AuditLog> auditLogs) {
        return auditLogs.stream().map(AuditLogExportDtoMapper::toAuditLogExportDto).toList();
    }
}
