/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.JournalEntry;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private User user;
    private User otherUser;
    private JournalEntry entry;
    private JournalEntryRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRole(Roles.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Roles.USER);

        entry = new JournalEntry();
        entry.setId(UUID.randomUUID());
        entry.setUser(user);
        entry.setTitle("Title");
        entry.setContent("Content");
        entry.setCreatedAt(LocalDate.now());
        entry.setUpdatedAt(LocalDate.now());

        request = new JournalEntryRequest("New Title", "New Content");
    }

    @Test
    void getAllJournalEntriesForUserEmptyList() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt")
                .and(Sort.by(Sort.Direction.DESC, "createdAt")));
        when(journalEntryRepository.findAllByUserId(user.getId(), pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUser(0, 5, "desc");
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getAllJournalEntriesForUserReturnsEntries() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt")
                .and(Sort.by(Sort.Direction.DESC, "createdAt")));
        when(journalEntryRepository.findAllByUserId(user.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(entry)));

        Page<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUser(0, 5, "desc");
        assertEquals(1, result.getTotalElements());
        assertEquals(entry.getTitle(), result.getContent().getFirst().title());
    }

    @Test
    void getAllJournalEntriesForUserNotPagedSuccess() throws AccessDeniedException {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(journalEntryRepository.findAllByUserId(user.getId())).thenReturn(List.of(entry));

        List<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUserNotPaged(user.getId());
        assertEquals(1, result.size());
        assertEquals(entry.getTitle(), result.getFirst().title());
    }

    @Test
    void getAllJournalEntriesForUserNotPagedThrowsIfOtherUser() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        assertThrows(AccessDeniedException.class,
                () -> journalEntryService.getAllJournalEntriesForUserNotPaged(otherUser.getId()));
    }

    @Test
    void getEntryByIdSuccess() throws AccessDeniedException {
        when(serviceHelper.getJournalEntryForCurrentUser(entry.getId())).thenReturn(entry);
        JournalEntryDto result = journalEntryService.getEntryById(entry.getId());
        assertEquals(entry.getTitle(), result.title());
    }

    @Test
    void createJournalEntrySuccess() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

        JournalEntryDto result = journalEntryService.createJournalEntry(request, "127.0.0.1");

        assertEquals(request.title(), result.title());
        assertEquals(request.content(), result.content());
    }

    @Test
    void updateJournalEntrySuccess() throws Exception {
        JournalEntryUpdateRequest updateRequest = new JournalEntryUpdateRequest("Updated Title", "Updated Content");
        when(serviceHelper.getJournalEntryForCurrentUser(entry.getId())).thenReturn(entry);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

        JournalEntryDto result = journalEntryService.updateJournalEntry(entry.getId(), updateRequest, "127.0.0.1");

        assertEquals("Updated Title", result.title());
        assertEquals("Updated Content", result.content());
    }

    @Test
    void updateJournalEntryThrowsIfNoChanges() throws AccessDeniedException {
        JournalEntryUpdateRequest updateRequest = new JournalEntryUpdateRequest(null, null);
        when(serviceHelper.getJournalEntryForCurrentUser(entry.getId())).thenReturn(entry);

        assertThrows(IllegalArgumentException.class,
                () -> journalEntryService.updateJournalEntry(entry.getId(), updateRequest, "127.0.0.1"));
    }

    @Test
    void deleteJournalEntrySuccess() throws Exception {
        when(serviceHelper.getJournalEntryForCurrentUser(entry.getId())).thenReturn(entry);
        journalEntryService.deleteJournalEntry(entry.getId(), "127.0.0.1");
    }

    @Test
    void getEntryDatesReturnsList() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(journalEntryRepository.findAllByUserId(user.getId())).thenReturn(List.of(entry));

        List<LocalDate> dates = journalEntryService.getEntryDates();
        assertEquals(1, dates.size());
        assertEquals(entry.getCreatedAt(), dates.getFirst());
    }

    @Test
    void getEntriesByDateReturnsList() {
        LocalDate date = LocalDate.now();
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(journalEntryRepository.findEntriesByUserEmailAndCreatedAt(user.getEmail(), date))
                .thenReturn(List.of(entry));

        List<JournalEntryDto> result = journalEntryService.getEntriesByDate(date);
        assertEquals(1, result.size());
        assertEquals(entry.getTitle(), result.getFirst().title());
    }

    @Test
    void getEntriesByDateReturnsEmptyIfNone() {
        LocalDate date = LocalDate.now();
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(journalEntryRepository.findEntriesByUserEmailAndCreatedAt(user.getEmail(), date))
                .thenReturn(Collections.emptyList());

        List<JournalEntryDto> result = journalEntryService.getEntriesByDate(date);
        assertTrue(result.isEmpty());
    }
}
