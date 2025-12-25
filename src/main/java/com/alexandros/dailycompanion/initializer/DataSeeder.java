/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.initializer;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.model.Saint;
import com.alexandros.dailycompanion.repository.SaintRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.MonthDayDeserializer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Application data initializer responsible for seeding static reference data.
 * <p>
 * This component populates the database with predefined saints data
 * from a bundled JSON resource on application startup if the database is empty.
 */
@Component
public class DataSeeder {

    private final SaintRepository saintRepository;

    public DataSeeder(SaintRepository saintRepository) {
        this.saintRepository = saintRepository;
    }

    /**
     * Seeds saint records into the database if no records currently exist.
     * <p>
     * Data is loaded from {@code data/saints.json} and converted into {@link Saint} entities.
     * The operation is transactional to ensure atomic persistence.
     *
     * @throws RuntimeException if data loading or persistence fails
     */
    public void seedSaintsIfEmpty() {
        if(saintRepository.count() > 0) {
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("--MM-dd");
            module.addDeserializer(java.time.MonthDay.class, new MonthDayDeserializer(formatter));
            objectMapper.registerModule(module);

            InputStream inputStream = new ClassPathResource("data/saints.json").getInputStream();

            List<SaintDto> saintDtos = objectMapper.readValue(inputStream, new TypeReference<>() {});

            List<Saint> saints = saintDtos.stream().map(saintDto -> {
                Saint saint = new Saint();
                saint.setName(saintDto.name());
                saint.setBirthYear(saintDto.birthYear());
                saint.setDeathYear(saintDto.deathYear());
                saint.setFeastDay(saintDto.feastDay());
                saint.setBiography(saintDto.biography());
                saint.setPatronage(saintDto.patronage());
                saint.setCanonizationYear(saintDto.canonizationYear());
                saint.setImageUrl(saintDto.imageUrl());
                saint.setImageSource(saintDto.imageSource());
                saint.setImageAuthor(saintDto.imageAuthor());
                saint.setImageLicence(saintDto.imageLicence());
                return saint;
            }).toList();

            saintRepository.saveAll(saints);
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed saints", e);
        }
    }
}
