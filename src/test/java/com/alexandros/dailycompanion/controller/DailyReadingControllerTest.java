package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.DailyReadingDto;
import com.alexandros.dailycompanion.dto.DailyReadingRequest;
import com.alexandros.dailycompanion.service.DailyReadingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@Import(DailyReadingControllerTest.MockConfig.class)
public class DailyReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DailyReadingService dailyReadingService;

    @Autowired
    private ObjectMapper objectMapper;

    private DailyReadingDto dailyReadingDto;
    private final UUID readingId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        dailyReadingDto = new DailyReadingDto(
                readingId,
                LocalDate.now(),
                "Exodus",
                "Hebrews",
                "Psalm 123",
                "John"
        );
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public DailyReadingService dailyReadingService() {
            return Mockito.mock(DailyReadingService.class);
        }
    }

    @Test
    void getAllDailyReadingsShouldReturnListOfDailyReadingDto() throws Exception {
        when(dailyReadingService.getAllReadings()).thenReturn(List.of(dailyReadingDto));

        mockMvc.perform(get("/api/v1/daily-reading"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(readingId.toString()))
                .andExpect(jsonPath("$[0].createdAt").value(LocalDate.now().toString()));
    }

    @Test
    void getDailyReadingByIdShouldReturnDailyReadingDto() throws Exception {
        when(dailyReadingService.getDailyReading(readingId)).thenReturn(dailyReadingDto);

        mockMvc.perform(get("/api/v1/daily-reading/" + readingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(readingId.toString()));
    }

    @Test
    void getTodaysDailyReadingShouldReturnDailyReadingDto() throws Exception {
        when(dailyReadingService.getTodaysReading()).thenReturn(dailyReadingDto);

        mockMvc.perform(get("/api/v1/daily-reading/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(readingId.toString()));
    }

    @Test
    void createDailyReadingShouldReturnCreatedDailyReading() throws Exception {
        DailyReadingRequest request = new DailyReadingRequest(
                "Leviticus",
                "Revelations",
                "Psalm 22",
                "Matthew"
        );

        DailyReadingDto createdDto = new DailyReadingDto(
                UUID.randomUUID(),
                LocalDate.now(),
                request.firstReading(),
                request.secondReading(),
                request.psalm(),
                request.gospel()
        );
        when(dailyReadingService.createReading(any(DailyReadingRequest.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/v1/daily-reading")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstReading").value("Leviticus"));
    }
}
