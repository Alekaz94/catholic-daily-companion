/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.model;

import com.alexandros.dailycompanion.converter.MonthDayConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.MonthDay;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saints")
public class Saint {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String name;
    private Integer birthYear;
    private Integer deathYear;
    @Convert(converter = MonthDayConverter.class)
    private MonthDay feastDay;
    @Column(columnDefinition = "TEXT")
    private String biography;
    @Column(columnDefinition = "TEXT")
    private String patronage;
    private Integer canonizationYear;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    @Column(columnDefinition = "TEXT")
    private String imageAuthor;
    @Column(columnDefinition = "TEXT")
    private String imageSource;
    @Column(columnDefinition = "TEXT")
    private String imageLicence;
}
