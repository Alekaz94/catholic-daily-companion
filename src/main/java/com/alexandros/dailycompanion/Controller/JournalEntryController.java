package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.JournalEntryDto;
import com.alexandros.dailycompanion.DTO.JournalEntryRequest;
import com.alexandros.dailycompanion.DTO.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.Service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<JournalEntryDto>> getAllJournalEntries() {
        List<JournalEntryDto> entries = journalEntryService.getAllJournalEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> getEntryById(@PathVariable UUID entryId) {
        JournalEntryDto entry = journalEntryService.getEntryById(entryId);
        return ResponseEntity.ok(entry);
    }

    @PostMapping
    public ResponseEntity<JournalEntryDto> createJournalEntry(@Valid @RequestBody JournalEntryRequest entryRequest) {
        JournalEntryDto entry = journalEntryService.createJournalEntry(entryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> updateJournalEntry(@PathVariable UUID entryId,
                                                          @Valid @RequestBody JournalEntryUpdateRequest entryUpdateRequest) {
        JournalEntryDto entry = journalEntryService.updateJournalEntry(entryId, entryUpdateRequest);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable UUID entryId) {
        journalEntryService.deleteJournalEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}
