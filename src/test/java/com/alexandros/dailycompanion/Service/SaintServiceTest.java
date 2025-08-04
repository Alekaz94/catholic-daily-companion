package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.SaintDto;
import com.alexandros.dailycompanion.DTO.SaintRequest;
import com.alexandros.dailycompanion.Model.Saint;
import com.alexandros.dailycompanion.Repository.SaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaintServiceTest {

    @Mock
    SaintRepository saintRepository;

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

        saintRequest = new SaintRequest(
                "St Francis",
                200,
                250,
                MonthDay.of(10, 4),
                "Saint of the Catholic church",
                "Animals and environment",
                450,
                "http://picture.com"
        );
    }

    @Test
    void createSaintShouldSaveNewSaintAndReturnDto() {
        when(saintRepository.save(any(Saint.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SaintDto result = saintService.createSaint(saintRequest);

        assertEquals(saintRequest.name(), result.name());
        assertEquals(saintRequest.birthYear(), result.birthYear());
        assertEquals(saintRequest.deathYear(), result.deathYear());
        assertEquals(saintRequest.feastDay(), result.feastDay());
        assertEquals(saintRequest.patronage(), result.patronage());
        assertEquals(saintRequest.canonizationYear(), result.canonizationYear());
        assertEquals(saintRequest.biography(), result.biography());
        assertEquals(saintRequest.imageUrl(), result.imageUrl());
    }

    @Test
    void getSaintShouldReturnSaintDtoIfFound() {
        when(saintRepository.findById(saint.getId())).thenReturn(Optional.of(saint));
        SaintDto result = saintService.getSaint(saint.getId());

        assertEquals(saint.getName(), result.name());
    }

    @Test
    void getSaintShouldThrowIfNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(saintRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> saintService.getSaint(nonExistentId));

        assertTrue(exception.getMessage().contains("Could not find saint"));
    }

    @Test
    void getAllSaintsShouldReturnPagedSaintDtosWithoutQuery() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Saint> page = new PageImpl<>(List.of(saint));

        when(saintRepository.findAll(pageable)).thenReturn(page);
        Page<SaintDto> result = saintService.getAllSaints(null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("St Francis", result.getContent().getFirst().name());
    }

    @Test
    void getAllSaintsShouldReturnPagedSaintDtosWithQuery() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Saint> page = new PageImpl<>(List.of(saint));

        when(saintRepository.findByNameContainingIgnoreCase("francis", pageable)).thenReturn(page);
        Page<SaintDto> result = saintService.getAllSaints("francis", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("St Francis", result.getContent().getFirst().name());
    }

    @Test
    void getSaintByFeastDayShouldReturnSaintDtoIfFound() {
        MonthDay today = MonthDay.now();
        saint.setFeastDay(today);

        when(saintRepository.findByFeastDay(today)).thenReturn(Optional.of(saint));
        SaintDto result = saintService.getSaintByFeastDay();

        assertNotNull(result);
        assertEquals("St Francis", result.name());
    }

    @Test
    void getSaintByFeastDayShouldReturnNullIfNotFound() {
        MonthDay today = MonthDay.now();

        when(saintRepository.findByFeastDay(today)).thenReturn(Optional.empty());
        SaintDto result = saintService.getSaintByFeastDay();

        assertNull(result);
    }
}
