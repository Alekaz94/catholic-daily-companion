package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.service.JournalEntryService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@Import(JournalEntryControllerTest.MockConfig.class)
public class JournalEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private ObjectMapper objectMapper;

    private JournalEntryDto journalEntryDto;
    private final UUID entryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        journalEntryDto = new JournalEntryDto(
                entryId,
                LocalDate.now(),
                LocalDate.now(),
                "Hello",
                "Content"
        );
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public JournalEntryService journalEntryService() {
            return Mockito.mock(JournalEntryService.class);
        }
    }

    @Test
    void getAllJournalEntriesShouldReturnList() throws Exception {
        Page<JournalEntryDto> page = new PageImpl<>(List.of(journalEntryDto));
        when(journalEntryService.getAllJournalEntriesForUser(anyInt(), anyInt(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/v1/journal-entry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(entryId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Hello"))
                .andExpect(jsonPath("$.content[0].content").value("Content"));
    }

    @Test
    void getJournalEntryByIdShouldReturnJournalEntryDto() throws Exception {
        when(journalEntryService.getEntryById(entryId)).thenReturn(journalEntryDto);

        mockMvc.perform(get("/api/v1/journal-entry/" + entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entryId.toString()))
                .andExpect(jsonPath("$.title").value("Hello"))
                .andExpect(jsonPath("$.content").value("Content"));
    }

    @Test
    void createJournalEntryShouldReturnCreatedJournalEntryDto() throws Exception{
        JournalEntryRequest request = new JournalEntryRequest(
                "New Title",
                "New Content"
        );

        JournalEntryDto createdDto = new JournalEntryDto(
                entryId,
                LocalDate.now(),
                LocalDate.now(),
                request.title(),
                request.content()
        );

        when(journalEntryService.createJournalEntry(any(JournalEntryRequest.class), eq("test")))
                .thenReturn(createdDto);

        mockMvc.perform(post("/api/v1/journal-entry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.content").value("New Content"));
    }

    @Test
    void createJournalEntryWithEmptyTitleShouldReturnBadRequest() throws Exception {
        JournalEntryRequest invalidRequest = new JournalEntryRequest(
                "",
                "Some content"
        );

        mockMvc.perform(post("/api/v1/journal-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateJournalEntryShouldReturnUpdatedJournalEntry() throws Exception {
        JournalEntryUpdateRequest request = new JournalEntryUpdateRequest(
            "Updated Title",
                "Updated Content"
        );

        JournalEntryDto updatedDto = new JournalEntryDto(
                entryId,
                LocalDate.now(),
                LocalDate.now(),
                "Updated Title",
                "Updated Content"
        );

        when(journalEntryService.updateJournalEntry(eq(entryId), any(JournalEntryUpdateRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/journal-entry/" + entryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    void updateJournalEntryWithOnlyContentShouldReturnUpdatedEntry() throws Exception {
        JournalEntryUpdateRequest updateRequest = new JournalEntryUpdateRequest(
                null,
                "Only Content Updated"
        );

        JournalEntryDto updatedDto = new JournalEntryDto(
                entryId,
                LocalDate.now(),
                LocalDate.now(),
                journalEntryDto.title(),
                "Only Content Updated"
        );

        when(journalEntryService.updateJournalEntry(eq(entryId), any(JournalEntryUpdateRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/journal-entry/" + entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(journalEntryDto.title()))
                .andExpect(jsonPath("$.content").value("Only Content Updated"));
    }

    @Test
    void updateJournalEntryWithEmptyContentShouldReturnBadRequest() throws Exception {
        JournalEntryRequest invalidRequest = new JournalEntryRequest(
                "Title",
                ""
        );

        mockMvc.perform(put("/api/v1/journal-entry/" + entryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteJournalEntryShouldReturnNoContnent() throws Exception {
        doNothing().when(journalEntryService).deleteJournalEntry(entryId);

        mockMvc.perform(delete("/api/v1/journal-entry/" + entryId))
                .andExpect(status().isNoContent());
    }
}
