package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.DailyReadingDto;
import com.alexandros.dailycompanion.DTO.DailyReadingRequest;
import com.alexandros.dailycompanion.Model.DailyReading;
import com.alexandros.dailycompanion.Repository.DailyReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DailyReadingServiceTest {

    @Mock
    DailyReadingRepository dailyReadingRepository;

    @InjectMocks
    DailyReadingService dailyReadingService;

    private DailyReading dailyReading;
    private DailyReadingRequest dailyReadingRequest;

    @BeforeEach
    void setUp() {
        dailyReading = new DailyReading();
        dailyReading.setId(UUID.randomUUID());
        dailyReading.setCreatedAt(LocalDate.of(2025, 10, 5));
        dailyReading.setFirstReading("Exodus");
        dailyReading.setSecondReading("Hebrews");
        dailyReading.setPsalm("Psalm 123");
        dailyReading.setGospel("John");

        dailyReadingRequest = new DailyReadingRequest(
                "Exodus",
                "Hebrews",
                "Psalm 123",
                "John"
        );
    }

    @Test
    void createDailyReadingShouldSaveNewDailyReadingAndReturnDto() {
        when(dailyReadingRepository.save(any(DailyReading.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DailyReadingDto result = dailyReadingService.createReading(dailyReadingRequest);

        assertEquals(dailyReadingRequest.firstReading(), result.firstReading());
        assertEquals(dailyReadingRequest.secondReading(), result.secondReading());
        assertEquals(dailyReadingRequest.psalm(), result.psalm());
        assertEquals(dailyReadingRequest.gospel(), result.gospel());
    }

    @Test
    void getDailyReadingShouldReturnDailyReadingDtoIfFound() {
        when(dailyReadingRepository.findById(dailyReading.getId())).thenReturn(Optional.of(dailyReading));
        DailyReadingDto result = dailyReadingService.getDailyReading(dailyReading.getId());

        assertEquals(dailyReading.getFirstReading(), result.firstReading());
        assertEquals(dailyReading.getSecondReading(), result.secondReading());
        assertEquals(dailyReading.getPsalm(), result.psalm());
        assertEquals(dailyReading.getGospel(), result.gospel());
        assertEquals(dailyReading.getCreatedAt(), result.createdAt());
    }

    @Test
    void getDailyReadingShouldThrowIfNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(dailyReadingRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dailyReadingService.getDailyReading(nonExistentId));

        assertTrue(exception.getMessage().contains("Could not find daily reading"));
    }

    @Test
    void getAllDailyReadingsShouldReturnDailyReadingDtos() {
        when(dailyReadingRepository.findAll()).thenReturn(List.of(dailyReading));

        List<DailyReadingDto> result = dailyReadingService.getAllReadings();

        assertEquals(1, result.size());
        assertEquals("Exodus", result.getFirst().firstReading());
        assertEquals("Hebrews", result.getFirst().secondReading());
        assertEquals("Psalm 123", result.getFirst().psalm());
        assertEquals("John", result.getFirst().gospel());
        assertEquals(LocalDate.of(2025, 10, 5), result.getFirst().createdAt());
    }

    @Test
    void getDailyReadingByCreatedAtShouldReturnDailyReadingDtoIfFound() {
        LocalDate date = LocalDate.now();
        dailyReading.setCreatedAt(date);

        when(dailyReadingRepository.findByCreatedAt(date)).thenReturn(Optional.of(dailyReading));
        DailyReadingDto result = dailyReadingService.getTodaysReading();

        assertNotNull(result);
        assertEquals("Exodus", result.firstReading());
        assertEquals("Hebrews", result.secondReading());
        assertEquals("Psalm 123", result.psalm());
        assertEquals("John", result.gospel());
        assertEquals(date, result.createdAt());
    }

    @Test
    void getDailyReadingByCreatedAtShouldThrowIfNotFound() {
        LocalDate date = LocalDate.now();

        when(dailyReadingRepository.findByCreatedAt(date)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dailyReadingService.getTodaysReading());

        assertTrue(exception.getMessage().contains("Could not find today's reading!"));
    }
}
