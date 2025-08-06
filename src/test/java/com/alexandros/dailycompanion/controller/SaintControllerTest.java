package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.dto.SaintRequest;
import com.alexandros.dailycompanion.service.SaintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@Import(SaintControllerTest.MockConfig.class)
public class SaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SaintService saintService;

    @Autowired
    private ObjectMapper objectMapper;

    private SaintDto saintDto;
    private final UUID saintId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        saintDto = new SaintDto(saintId,
                "St Francis",
                200,
                300,
                MonthDay.of(8, 6),
                "St of the Catholic church",
                "Animals",
                450,
                "http://picture.com"
        );
        when(saintService.getSaint(saintId)).thenReturn(saintDto);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public SaintService saintService() {
            return Mockito.mock(SaintService.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        }
    }

    @Test
    void getAllSaintsShouldReturnPagedSaintDtoWithQuery() throws Exception {
        Page<SaintDto> page = new PageImpl<>(List.of(saintDto), PageRequest.of(0, 5), 1);
        when(saintService.getAllSaints(anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/saint")
                        .param("query", "Francis")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(saintId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("St Francis"));
    }

    @Test
    void getAllSaintsShouldReturnPagedSaintDtoWithoutQuery() throws Exception {
        Page<SaintDto> page = new PageImpl<>(List.of(saintDto), PageRequest.of(0, 5), 1);
        when(saintService.getAllSaints(anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/saint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(saintId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("St Francis"));
    }

    @Test
    void getSaintByIdShouldReturnSaintDto() throws Exception {
        mockMvc.perform(get("/api/v1/saint/" + saintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saintId.toString()))
                .andExpect(jsonPath("$.name").value("St Francis"));
    }

    @Test
    void getTodaysSaintShouldReturnSaintDto() throws Exception {
        when(saintService.getSaintByFeastDay()).thenReturn(saintDto);

        mockMvc.perform(get("/api/v1/saint/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("St Francis"));
    }

    @Test
    void createSaintShouldReturnCreatedSaint() throws Exception {
        SaintRequest saintRequest = new SaintRequest(
                "St Francis",
                200,
                300,
                MonthDay.of(10, 5),
                "St of the Catholic church",
                "Animals",
                450,
                "http://picture.com"
        );
        when(saintService.createSaint(saintRequest)).thenReturn(saintDto);

        mockMvc.perform(post("/api/v1/saint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saintRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("St Francis"));
    }
}
