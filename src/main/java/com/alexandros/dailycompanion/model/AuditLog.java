package com.alexandros.dailycompanion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    private UUID userId;
    private String action;
    private String entityType;
    private UUID entityId;

    @Column(columnDefinition = "TEXT")
    private String metaData;

    private String ipAddress;
    private LocalDateTime createdAt = LocalDateTime.now();
}
