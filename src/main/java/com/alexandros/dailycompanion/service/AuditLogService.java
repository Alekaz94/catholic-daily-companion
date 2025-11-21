/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.model.AuditLog;
import com.alexandros.dailycompanion.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Service
public class AuditLogService {

    private final static Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
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
}
