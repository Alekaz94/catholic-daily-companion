package com.alexandros.dailycompanion.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_reading")
@Entity
public class DailyReading {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    private LocalDate date;
    @Column(columnDefinition = "TEXT")
    private String firstReading;
    @Column(columnDefinition = "TEXT")
    private String secondReading;
    @Column(columnDefinition = "TEXT")
    private String psalm;
    @Column(columnDefinition = "TEXT")
    private String gospel;
}
