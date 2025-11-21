/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.dto.SaintRequest;
import com.alexandros.dailycompanion.dto.SaintUpdateRequest;
import com.alexandros.dailycompanion.model.Saint;
import com.alexandros.dailycompanion.repository.SaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.MonthDay;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaintServiceTest {

    @Mock
    SaintRepository saintRepository;

    @Mock
    ServiceHelper serviceHelper;

    @InjectMocks
    SaintService saintService;

    private Saint saint;
    private SaintRequest saintRequest;

    @BeforeEach
    void setUp() {
        saint = new Saint();
        saint.setId(UUID.randomUUID());
        saint.setName("St Francis");
        saint.setBirthYear(200);
        saint.setDeathYear(250);
        saint.setFeastDay(MonthDay.of(10, 4));
        saint.setCanonizationYear(450);
        saint.setBiography("Saint of the Catholic church");
        saint.setPatronage("Animals and environment");
        saint.setImageUrl("http://picture.com");
        saint.setImageSource("source");
        saint.setImageAuthor("author");
        saint.setImageLicence("licence");

        saintRequest = new SaintRequest(
                "St Francis",
                200,
                250,
                MonthDay.of(10, 4),
                "Saint of the Catholic church",
                "Animals and environment",
                450,
                "http://picture.com",
                "source",
                "author",
                "licence"
        );
    }

    @Test
    void createSaintShouldSaveNewSaintAndReturnDto() {
        when(saintRepository.save(any(Saint.class))).thenAnswer(i -> i.getArgument(0));
        SaintDto result = saintService.createSaint(saintRequest);

        assertEquals("St Francis", result.name());
        verify(saintRepository).save(any(Saint.class));
    }

    @Test
    void getSaintShouldUseServiceHelper() {
        when(serviceHelper.getSaintById(saint.getId())).thenReturn(saint);

        SaintDto result = saintService.getSaint(saint.getId());

        assertEquals("St Francis", result.name());
    }

    @Test
    void updateSaintShouldModifyFieldsAndSave() {
        SaintUpdateRequest updateRequest = new SaintUpdateRequest(
                "New Name",
                500,
                600,
                MonthDay.of(12, 25),
                "New bio",
                "New patronage",
                999,
                "new.url",
                "new.source",
                "new.author",
                "new.licence"
        );

        when(serviceHelper.getSaintById(saint.getId())).thenReturn(saint);
        when(saintRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SaintDto result = saintService.updateSaint(saint.getId(), updateRequest);

        assertEquals("New Name", result.name());
        assertEquals(500, result.birthYear());
        assertEquals(600, result.deathYear());
        assertEquals(MonthDay.of(12, 25), result.feastDay());
        verify(saintRepository).save(any());
    }

    @Test
    void updateSaintShouldNotSaveIfNoFieldsUpdated() {
        SaintUpdateRequest updateRequest = new SaintUpdateRequest(
                null, null, null, null,
                null, null, null,
                null, null, null, null
        );

        when(serviceHelper.getSaintById(saint.getId())).thenReturn(saint);

        SaintDto result = saintService.updateSaint(saint.getId(), updateRequest);

        verify(saintRepository, never()).save(any());
        assertEquals("St Francis", result.name());
    }

    @Test
    void deleteSaintShouldCallRepositoryDelete() {
        when(serviceHelper.getSaintById(saint.getId())).thenReturn(saint);
        saintService.deleteSaint(saint.getId());
        verify(saintRepository).deleteById(saint.getId());
    }

    @Test
    void getAllSaintsWithoutQueryReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Saint> page = new PageImpl<>(List.of(saint));

        when(saintRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<SaintDto> result = saintService.getAllSaints(null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("St Francis", result.getContent().getFirst().name());
    }

    @Test
    void getAllSaintsWithQueryFiltersByName() {
        Page<Saint> page = new PageImpl<>(List.of(saint));

        when(saintRepository.findByNameContainingIgnoreCase(eq("francis"), any(Pageable.class)))
                .thenReturn(page);

        Page<SaintDto> result = saintService.getAllSaints("francis", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getSaintByFeastDayReturnsDto() {
        MonthDay today = MonthDay.now();
        saint.setFeastDay(today);

        when(saintRepository.findByFeastDay(today)).thenReturn(Optional.of(saint));

        SaintDto result = saintService.getSaintByFeastDay();
        assertNotNull(result);
    }

    @Test
    void getSaintByFeastDayReturnsNullIfNotFound() {
        when(saintRepository.findByFeastDay(MonthDay.now())).thenReturn(Optional.empty());
        assertNull(saintService.getSaintByFeastDay());
    }

    @Test
    void getSaintsByMonthGroupsCorrectly() {
        Saint s1 = new Saint();
        s1.setName("A");
        s1.setFeastDay(MonthDay.of(10, 4));

        Saint s2 = new Saint();
        s2.setName("B");
        s2.setFeastDay(MonthDay.of(10, 4));

        when(saintRepository.findAll()).thenReturn(List.of(s1, s2));
        Map<String, List<String>> result = saintService.getSaintsByMonth(2025, 10);

        assertEquals(1, result.size());
        assertEquals(List.of("A", "B"), result.get("--10-04"));
    }

    @Test
    void getAllSaintsByFeastCodeReturnsList() {
        when(saintRepository.findAllByFeastDay(MonthDay.of(10, 4))).thenReturn(List.of(saint));

        List<SaintDto> result = saintService.getAllSaintsByFeastCode("10-04");
        assertEquals(1, result.size());
    }

    @Test
    void getAllSaintsByFeastCodeReturnsEmpty() {
        when(saintRepository.findAllByFeastDay(any())).thenReturn(Collections.emptyList());
        assertTrue(saintService.getAllSaintsByFeastCode("10-04").isEmpty());
    }

    @Test
    void getAllSaintsByFeastDayReturnsList() {
        MonthDay today = MonthDay.now();
        when(saintRepository.findAllByFeastDay(today)).thenReturn(List.of(saint));

        assertEquals(1, saintService.getAllSaintsByFeastDay().size());
    }

    @Test
    void getAllSaintsByFeastDayReturnsEmpty() {
        when(saintRepository.findAllByFeastDay(any())).thenReturn(Collections.emptyList());
        assertTrue(saintService.getAllSaintsByFeastDay().isEmpty());
    }

    @Test
    void getAllFeastDaysMappedShouldGroupByFeastCode() {
        Saint s1 = new Saint();
        s1.setName("A");
        s1.setFeastDay(MonthDay.of(10, 4));

        Saint s2 = new Saint();
        s2.setName("B");
        s2.setFeastDay(MonthDay.of(10, 4));

        when(saintRepository.findAll()).thenReturn(List.of(s1, s2));

        Map<String, List<String>> result = saintService.getAllFeastDaysMapped();
        assertEquals(List.of("A", "B"), result.get("10-04"));
    }
}