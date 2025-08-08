package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal-entry")
@Validated
public class JournalEntryController {
    private final JournalEntryService journalEntryService;

    @Autowired
    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping
    public ResponseEntity<Page<JournalEntryDto>> getAllJournalEntries(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "5") int size,
                                                                      @RequestParam(defaultValue = "desc") String sort) {
        Page<JournalEntryDto> entries = journalEntryService.getAllJournalEntriesForUser(page, size, sort);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> getEntryById(@PathVariable UUID entryId) throws AccessDeniedException {
        JournalEntryDto entry = journalEntryService.getEntryById(entryId);
        return ResponseEntity.ok(entry);
    }

    @PostMapping
    public ResponseEntity<JournalEntryDto> createJournalEntry(@Valid @RequestBody JournalEntryRequest entryRequest,
                                                              Principal principal) {
        JournalEntryDto entry = journalEntryService.createJournalEntry(entryRequest, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> updateJournalEntry(@PathVariable UUID entryId,
                                                          @Valid @RequestBody JournalEntryUpdateRequest entryUpdateRequest) throws AccessDeniedException {
        JournalEntryDto entry = journalEntryService.updateJournalEntry(entryId, entryUpdateRequest);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable UUID entryId) throws AccessDeniedException {
        journalEntryService.deleteJournalEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}
