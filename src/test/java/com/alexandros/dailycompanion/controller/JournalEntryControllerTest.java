/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.exception.GlobalExceptionHandler;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.service.JournalEntryService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JournalEntryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JournalEntryService journalEntryService;

    @Mock
    private ServiceHelper serviceHelper;

    @InjectMocks
    private JournalEntryController journalEntryController;

    private User user;
    private JournalEntryDto journalEntryDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(journalEntryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        user = new User();
        user.setId(UUID.randomUUID());

        journalEntryDto = new JournalEntryDto(
                UUID.randomUUID(),
                LocalDate.now(),
                LocalDate.now(),
                "My Title",
                "My Content"
        );

        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(serviceHelper.getClientIp(any())).thenReturn("127.0.0.1");
    }

    @Test
    void getAllJournalEntries_success() throws Exception {
        PageResponse<JournalEntryDto> pageResponse = new PageResponse<>(
                List.of(journalEntryDto), 0, 5, 1, 1, true
        );

        when(journalEntryService.getAllJournalEntriesForUser(0, 5, "desc")).thenReturn(
                new org.springframework.data.domain.PageImpl<>(List.of(journalEntryDto))
        );

        mockMvc.perform(get("/api/v1/journal-entry")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("My Title"));
    }

    @Test
    void getEntryById_success() throws Exception {
        when(journalEntryService.getEntryById(journalEntryDto.id())).thenReturn(journalEntryDto);

        mockMvc.perform(get("/api/v1/journal-entry/{entryId}", journalEntryDto.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Title"));
    }

    @Test
    void getEntryById_notFound_shouldReturn404() throws Exception {
        UUID entryId = UUID.randomUUID();
        when(journalEntryService.getEntryById(entryId))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Journal entry not found"));

        mockMvc.perform(get("/api/v1/journal-entry/{entryId}", entryId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Journal entry not found"));
    }

    @Test
    void createJournalEntry_success() throws Exception {
        JournalEntryRequest request = new JournalEntryRequest("My Title", "My Content");

        when(journalEntryService.createJournalEntry(any(), any())).thenReturn(journalEntryDto);

        mockMvc.perform(post("/api/v1/journal-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"My Title\",\"content\":\"My Content\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("My Title"));
    }

    @Test
    void createJournalEntry_invalidInput_shouldReturn400() throws Exception {
        String invalidJson = """
    {
        "content": "Only content, missing title"
    }
    """;

        mockMvc.perform(post("/api/v1/journal-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateJournalEntry_success() throws Exception {
        JournalEntryUpdateRequest updateRequest = new JournalEntryUpdateRequest("Updated Title", "Updated Content");

        when(journalEntryService.updateJournalEntry(any(), any(), any())).thenReturn(
                new JournalEntryDto(journalEntryDto.id(), LocalDate.now(), LocalDate.now(), "Updated Title", "Updated Content")
        );

        mockMvc.perform(put("/api/v1/journal-entry/{entryId}", journalEntryDto.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\",\"content\":\"Updated Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateJournalEntry_notFound_shouldReturn404() throws Exception {
        UUID entryId = UUID.randomUUID();

        when(journalEntryService.updateJournalEntry(eq(entryId), any(), any()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Journal entry not found"));

        mockMvc.perform(put("/api/v1/journal-entry/{entryId}", entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"content\":\"Updated Content\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Journal entry not found"));
    }

    @Test
    void deleteJournalEntry_success() throws Exception {
        mockMvc.perform(delete("/api/v1/journal-entry/{entryId}", journalEntryDto.id()))
                .andExpect(status().isNoContent());
    }
}
