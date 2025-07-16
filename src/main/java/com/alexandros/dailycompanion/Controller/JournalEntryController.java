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
    public ResponseEntity<List<JournalEntryDto>> getAllJournalEntries(Principal principal) {
        List<JournalEntryDto> entries = journalEntryService.getAllJournalEntries(principal.getName());
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<JournalEntryDto> getEntryById(@PathVariable UUID entryId, Principal principal) throws AccessDeniedException {
        JournalEntryDto entry = journalEntryService.getEntryById(entryId, principal.getName());
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
                                                          @Valid @RequestBody JournalEntryUpdateRequest entryUpdateRequest,
                                                              Principal principal) throws AccessDeniedException {
        JournalEntryDto entry = journalEntryService.updateJournalEntry(entryId, entryUpdateRequest, principal.getName());
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable UUID entryId, Principal principal) throws AccessDeniedException {
        journalEntryService.deleteJournalEntry(entryId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
