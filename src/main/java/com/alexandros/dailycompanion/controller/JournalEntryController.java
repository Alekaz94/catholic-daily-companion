/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.service.JournalEntryService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal-entry")
@Validated
public class JournalEntryController {
    private final static Logger logger = LoggerFactory.getLogger(JournalEntryController.class);
    private final JournalEntryService journalEntryService;
    private final ServiceHelper serviceHelper;

    @Autowired
    public JournalEntryController(JournalEntryService journalEntryService, ServiceHelper serviceHelper) {
        this.journalEntryService = journalEntryService;
        this.serviceHelper = serviceHelper;
    }

    @GetMapping
    public ResponseEntity<PageResponse<JournalEntryLiteDto>> getAllJournalEntries(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "5") int size,
                                                                              @RequestParam(defaultValue = "desc") String sort) {
        Page<JournalEntryLiteDto> entries = journalEntryService.getAllJournalEntriesForUser(page, size, sort);

        PageResponse<JournalEntryLiteDto> response = new PageResponse<>(
                entries.getContent(),
                entries.getNumber(),
                entries.getSize(),
                entries.getTotalElements(),
                entries.getTotalPages(),
                entries.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> getEntryById(@PathVariable UUID entryId) throws AccessDeniedException {
        JournalEntryDto entry = journalEntryService.getEntryById(entryId);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/dates")
    public ResponseEntity<List<String>> getJournalEntryDates() {
        List<String> dates = journalEntryService.getEntryDates()
                .stream()
                .map(LocalDate::toString)
                .toList();
        return ResponseEntity.ok(dates);
    }

    @GetMapping("/dates/{date}")
    public ResponseEntity<?> getEntryByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<JournalEntryDto> entry = journalEntryService.getEntriesByDate(localDate);
            return ResponseEntity.ok(entry);
        } catch (DateTimeParseException dateTimeParseException) {
            return ResponseEntity.badRequest().body("Invalid date format. Use 'yyyy-MM-dd'.");
        }
    }

    @PostMapping
    public ResponseEntity<JournalEntryDto> createJournalEntry(@Valid @RequestBody JournalEntryRequest entryRequest,
                                                              HttpServletRequest servletRequest) {
        User currentUser = serviceHelper.getAuthenticatedUser();
        String ipAddress = serviceHelper.getClientIp(servletRequest);

        JournalEntryDto entry = journalEntryService.createJournalEntry(entryRequest, ipAddress);
        logger.info("POST /journal-entry | user={} | title='{}' | ip={}", currentUser.getId(), entry.title(), ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> updateJournalEntry(@PathVariable UUID entryId,
                                                          @Valid @RequestBody JournalEntryUpdateRequest entryUpdateRequest,
                                                              HttpServletRequest servletRequest) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();
        String ipAddress = serviceHelper.getClientIp(servletRequest);

        JournalEntryDto updatedEntry = journalEntryService.updateJournalEntry(entryId, entryUpdateRequest, ipAddress);
        logger.info("PUT /journal-entry/{} | user={} | title='{}' | ip={}", entryId, currentUser.getId(), updatedEntry.title(), ipAddress);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable UUID entryId,
                                                   HttpServletRequest servletRequest) throws AccessDeniedException {
        User currentUser = serviceHelper.getAuthenticatedUser();
        String ipAddress = serviceHelper.getClientIp(servletRequest);

        journalEntryService.deleteJournalEntry(entryId, ipAddress);
        logger.info("DELETE /journal-entry/{} | user={} | ip={}", entryId, currentUser.getId(), ipAddress);
        return ResponseEntity.noContent().build();
    }
}
