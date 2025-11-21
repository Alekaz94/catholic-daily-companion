package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.service.SaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.MonthDay;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SaintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SaintService saintService;

    @InjectMocks
    private SaintController saintController;

    private UUID saintId;
    private SaintDto saintDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(saintController).build();

        saintId = UUID.randomUUID();
        saintDto = new SaintDto(saintId, "St. Peter", 300, 350, MonthDay.of(10, 2), "Feast Day Info", "patronage", 600, "image", "source", "author", "licence");
    }

    @Test
    void getAllSaints_success() throws Exception {
        Page<SaintDto> page = new PageImpl<>(List.of(saintDto), PageRequest.of(0, 5), 1);
        when(saintService.getAllSaints("", 0, 5)).thenReturn(page);

        mockMvc.perform(get("/api/v1/saint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(saintId.toString()));
    }

    @Test
    void getSaint_success() throws Exception {
        when(saintService.getSaint(saintId)).thenReturn(saintDto);

        mockMvc.perform(get("/api/v1/saint/{saintId}", saintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saintId.toString()));
    }

    @Test
    void getSaint_notFound_shouldReturn404() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(saintService.getSaint(invalidId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Saint not found"));

        mockMvc.perform(get("/api/v1/saint/{saintId}", invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSaint_success() throws Exception {
        // Updated SaintRequest with all fields
        SaintRequest request = new SaintRequest(
                "St. Paul",
                300,
                450,
                MonthDay.of(12, 3),
                "Feast Info For Saint",
                "Patronage",
                1500,
                "imageUrl",
                "imageSource",
                "imageAuthor",
                "imageLicence"
        );

        when(saintService.createSaint(any(SaintRequest.class))).thenReturn(saintDto);

        // JSON payload must match all fields
        String json = """
            {
                "name": "St. Paul",
                "birthYear": 300,
                "deathYear": 450,
                "feastDay": "--12-03",
                "biography": "Feast Info For Saint",
                "patronage": "Patronage",
                "canonizationYear": 1500,
                "imageUrl": "imageUrl",
                "imageSource": "imageSource",
                "imageAuthor": "imageAuthor",
                "imageLicence": "imageLicence"
            }
            """;

        mockMvc.perform(post("/api/v1/saint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saintId.toString()));
    }

    @Test
    void createSaint_invalidInput_shouldReturnBadRequest() throws Exception {
        String invalidJson = """
        {
            "birthYear": 300,
            "deathYear": 450,
            "feastDay": "--12-03",
            "patronage": "Patronage"
        }
        """;

        mockMvc.perform(post("/api/v1/saint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSaint_success() throws Exception {
        SaintUpdateRequest request = new SaintUpdateRequest("New Name", 400, 460, MonthDay.of(12, 4), "New Desc", "Patron", 1560, "image.url", "image.source", "image.author", "image.licence");
        when(saintService.updateSaint(any(UUID.class), any(SaintUpdateRequest.class))).thenReturn(saintDto);

        mockMvc.perform(put("/api/v1/saint/{saintId}", saintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\",\"description\":\"New Desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saintId.toString()));
    }

    @Test
    void updateSaint_notFound_shouldReturn404() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(saintService.updateSaint(any(UUID.class), any(SaintUpdateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Saint not found"));

        String json = """
        {
            "name": "New Name",
            "description": "New Desc"
        }
        """;

        mockMvc.perform(put("/api/v1/saint/{saintId}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSaint_success() throws Exception {
        mockMvc.perform(delete("/api/v1/saint/{saintId}", saintId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSaint_notFound_shouldReturn404() throws Exception {
        UUID invalidId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Saint not found"))
                .when(saintService).deleteSaint(invalidId);

        mockMvc.perform(delete("/api/v1/saint/{saintId}", invalidId))
                .andExpect(status().isNotFound());
    }
}
