/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.service.RosaryLogService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RosaryLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RosaryLogService rosaryLogService;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RosaryLogController rosaryLogController;

    private UUID userId;
    private RosaryLogDto logDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(rosaryLogController).build();

        userId = UUID.randomUUID();
        logDto = new RosaryLogDto(UUID.randomUUID(), LocalDate.now(), true);

        when(serviceHelper.getClientIp(request)).thenReturn("127.0.0.1");
        when(serviceHelper.getAuthenticatedUser()).thenReturn(new User());
    }

    @Test
    void completeToday_success() throws Exception {
        when(rosaryLogService.markCompleted(any(UUID.class), any())).thenReturn(logDto);

        mockMvc.perform(post("/api/v1/rosary/{userId}/complete", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(logDto.id().toString()));
    }

    @Test
    void isCompletedToday_success() throws Exception {
        when(rosaryLogService.isCompletedToday(userId)).thenReturn(true);

        mockMvc.perform(post("/api/v1/rosary/{userId}/completed-today", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isCompletedToday_invalidUser_shouldReturnFalse() throws Exception {
        UUID invalidUserId = UUID.randomUUID();
        when(rosaryLogService.isCompletedToday(invalidUserId)).thenReturn(false);

        mockMvc.perform(post("/api/v1/rosary/{userId}/completed-today", invalidUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    /*@Test
    void getHistory_success() throws Exception {
        when(rosaryLogService.getHistory(userId)).thenReturn(List.of(logDto));

        mockMvc.perform(get("/api/v1/rosary/{userId}/history", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(logDto.id().toString()));
    }*/

    @Test
    void getStreak_success() throws Exception {
        when(rosaryLogService.getStreak(userId)).thenReturn(3);

        mockMvc.perform(get("/api/v1/rosary/{userId}/streak", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    /*@Test
    void getRosaryDates_success() throws Exception {
        when(rosaryLogService.getCompletedDates(userId)).thenReturn(List.of(LocalDate.now()));

        mockMvc.perform(get("/api/v1/rosary/{userId}/rosary-dates", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void getRosaryDates_noLogs_shouldReturnEmptyList() throws Exception {
        UUID userId = UUID.randomUUID();
        when(rosaryLogService.getCompletedDates(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/rosary/{userId}/rosary-dates", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
*/
    @Test
    void isCompletedOn_success() throws Exception {
        LocalDate date = LocalDate.now();
        when(rosaryLogService.isCompletedOn(userId, date)).thenReturn(true);

        mockMvc.perform(get("/api/v1/rosary/{userId}/completed-on/{date}", userId, date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
