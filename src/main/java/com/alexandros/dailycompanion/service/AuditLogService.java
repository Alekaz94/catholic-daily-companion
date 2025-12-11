/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.AuditLogDto;
import com.alexandros.dailycompanion.dto.AuditLogExportDto;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.AuditLogDtoMapper;
import com.alexandros.dailycompanion.mapper.AuditLogExportDtoMapper;
import com.alexandros.dailycompanion.model.AuditLog;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final static Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;
    private final ServiceHelper serviceHelper;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository, ServiceHelper serviceHelper) {
        this.auditLogRepository = auditLogRepository;
        this.serviceHelper = serviceHelper;
    }

    public void logAction(UUID userId, String action, String entityType, UUID entityId, String metadata, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetaData(metadata);
        log.setIpAddress(ipAddress);

        auditLogRepository.save(log);
        logger.info("AUDIT_LOG | userId={} | action={} | entityType={} | entityId={} | ip={} | meta={}",
                userId, action, entityType, entityId, ipAddress, metadata);
    }

    public List<AuditLogDto> getAllAuditLogsForUser(UUID userId) throws AccessDeniedException {
        User user = serviceHelper.getAuthenticatedUser();

        if(!user.getRole().equals(Roles.ADMIN) && !user.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's data.");
        }

        return AuditLogDtoMapper.toAuditLogDto(auditLogRepository.findAllByUserId(userId));
    }

    public List<AuditLogExportDto> getAllAuditLogsForExportForUser(UUID userId) throws AccessDeniedException {
        User user = serviceHelper.getAuthenticatedUser();

        if(!user.getRole().equals(Roles.ADMIN) && !user.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's data.");
        }

        return AuditLogExportDtoMapper.toAuditLogExportDto(auditLogRepository.findAllByUserId(userId));
    }
}
