/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.model.AuditLog;
import com.alexandros.dailycompanion.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void logActionShouldSaveAuditLog() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        auditLogService.logAction(
                userId,
                "CREATE",
                "User",
                entityId,
                "{\"meta\":\"test\"}",
                "127.0.0.1"
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(entityId, saved.getEntityId());
        assertEquals("CREATE", saved.getAction());
        assertEquals("{\"meta\":\"test\"}", saved.getMetaData());
        assertEquals("127.0.0.1", saved.getIpAddress());
    }
}
