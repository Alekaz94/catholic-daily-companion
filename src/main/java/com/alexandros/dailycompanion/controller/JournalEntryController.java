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

/**
 * REST controller responsible for managing user journal entries.
 * <p>
 * Provides endpoints for:
 * <ul>
 *     <li>Creating journal entries by users</li>
 *     <li>Retrieving journal entries with pagination</li>
 *     <li>Viewing individual journal entry details</li>
 *     <li>Updating journal entries</li>
 * </ul>
 */
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

    /**
     * Retrieves a paginated list of journal entries for the authenticated user.
     *
     * @param page zero-based page index (default: 0)
     * @param size number of entries per page (default: 5)
     * @param sort sort direction, either {@code asc} or {@code desc} (default: desc)
     * @return paginated response containing journal entry summaries
     */
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

    /**
     * Retrieves a specific journal entry by its unique identifier.
     *
     * @param entryId unique identifier of the journal entry
     * @return full journal entry details
     * @throws AccessDeniedException if the entry does not belong to the user
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> getEntryById(@PathVariable UUID entryId) throws AccessDeniedException {
        JournalEntryDto entry = journalEntryService.getEntryById(entryId);
        return ResponseEntity.ok(entry);
    }

    /**
     * Retrieves all dates on which the user has journal entries.
     *
     * @return list of ISO-8601 formatted dates (yyyy-MM-dd)
     */
    @GetMapping("/dates")
    public ResponseEntity<List<String>> getJournalEntryDates() {
        List<String> dates = journalEntryService.getEntryDates()
                .stream()
                .map(LocalDate::toString)
                .toList();
        return ResponseEntity.ok(dates);
    }

    /**
     * Retrieves journal entries for a specific date.
     *
     * @param date date in {@code yyyy-MM-dd} format
     * @return list of journal entries for the given date
     */
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

    /**
     * Creates a new journal entry for the authenticated user.
     *
     * @param entryRequest   journal entry creation request
     * @param servletRequest HTTP servlet request used to extract client IP
     * @return created journal entry
     */
    @PostMapping
    public ResponseEntity<JournalEntryDto> createJournalEntry(@Valid @RequestBody JournalEntryRequest entryRequest,
                                                              HttpServletRequest servletRequest) {
        User currentUser = serviceHelper.getAuthenticatedUser();
        String ipAddress = serviceHelper.getClientIp(servletRequest);

        JournalEntryDto entry = journalEntryService.createJournalEntry(entryRequest, ipAddress);
        logger.info("POST /journal-entry | user={} | title='{}' | ip={}", currentUser.getId(), entry.title(), ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    /**
     * Updates an existing journal entry.
     *
     * @param entryId            unique identifier of the journal entry
     * @param entryUpdateRequest updated journal entry data
     * @param servletRequest     HTTP servlet request used to extract client IP
     * @return updated journal entry
     * @throws AccessDeniedException if the entry does not belong to the user
     */
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

    /**
     * Deletes an existing journal entry.
     *
     * @param entryId            unique identifier of the journal entry
     * @param servletRequest     HTTP servlet request used to extract client IP
     * @return {@code 204 No Content} on successful deletion
     * @throws AccessDeniedException if the entry does not belong to the user
     */
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
