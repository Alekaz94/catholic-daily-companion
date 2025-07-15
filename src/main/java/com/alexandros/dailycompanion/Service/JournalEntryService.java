package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.JournalEntryDto;
import com.alexandros.dailycompanion.DTO.JournalEntryRequest;
import com.alexandros.dailycompanion.DTO.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.Mapper.JournalEntryDtoMapper;
import com.alexandros.dailycompanion.Model.JournalEntry;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Repository.JournalEntryRepository;
import com.alexandros.dailycompanion.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class JournalEntryService {
    private final JournalEntryRepository journalEntryRepository;
    private final UserRepository userRepository;

    @Autowired
    public JournalEntryService(JournalEntryRepository journalEntryRepository, UserRepository userRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.userRepository = userRepository;
    }

    public List<JournalEntryDto> getAllJournalEntries() {
        List<JournalEntry> entries = journalEntryRepository.findAll();
        return JournalEntryDtoMapper.toJournalEntryDto(entries);
    }

    public JournalEntryDto getEntryById(UUID entryId) {
        JournalEntry entry = getJournalEntryById(entryId);
        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    public JournalEntryDto createJournalEntry(@Valid JournalEntryRequest entryRequest) {
        User user = userRepository.findByEmail(entryRequest.email()).orElseThrow(() ->
                new EntityNotFoundException(String.format("Could not find user with email: %s", entryRequest.email())));
        JournalEntry entry = new JournalEntry();

        entry.setCreatedAt(LocalDate.now());
        entry.setUpdatedAt(LocalDate.now());
        entry.setContent(entryRequest.content());
        entry.setUser(user);
        journalEntryRepository.save(entry);

        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    public JournalEntryDto updateJournalEntry(UUID entryId, @Valid JournalEntryUpdateRequest entryUpdateRequest) {
        JournalEntry entry = getJournalEntryById(entryId);

        entry.setContent(entryUpdateRequest.content());
        entry.setUpdatedAt(LocalDate.now());
        journalEntryRepository.save(entry);

        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    public void deleteJournalEntry(UUID entryId) {
        JournalEntry entry = getJournalEntryById(entryId);
        journalEntryRepository.deleteById(entry.getId());
    }

    public JournalEntry getJournalEntryById(UUID id) {
        return journalEntryRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Could not find journal entry with id: %s", id)));
    }
}
