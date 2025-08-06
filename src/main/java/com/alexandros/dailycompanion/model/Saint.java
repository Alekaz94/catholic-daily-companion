package com.alexandros.dailycompanion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.MonthDay;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saints")
@Entity
public class Saint {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String name;
    private Integer birthYear;
    private Integer deathYear;
    private MonthDay feastDay;
    @Column(columnDefinition = "TEXT")
    private String biography;

    private String patronage;
    private Integer canonizationYear;
    private String imageUrl;
}
