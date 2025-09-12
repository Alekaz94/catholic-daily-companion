package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.dto.PageResponse;
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
import java.time.LocalDate;
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
    public ResponseEntity<PageResponse<JournalEntryDto>> getAllJournalEntries(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "5") int size,
                                                                              @RequestParam(defaultValue = "desc") String sort) {
        Page<JournalEntryDto> entries = journalEntryService.getAllJournalEntriesForUser(page, size, sort);

        PageResponse<JournalEntryDto> response = new PageResponse<>(
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
    public ResponseEntity<List<JournalEntryDto>> getEntryByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<JournalEntryDto> entry = journalEntryService.getEntriesByDate(localDate);
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
