package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.JournalEntryDto;
import com.alexandros.dailycompanion.DTO.JournalEntryRequest;
import com.alexandros.dailycompanion.DTO.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.Enum.Roles;
import com.alexandros.dailycompanion.Mapper.JournalEntryDtoMapper;
import com.alexandros.dailycompanion.Model.JournalEntry;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Repository.JournalEntryRepository;
import com.alexandros.dailycompanion.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
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

    public List<JournalEntryDto> getAllJournalEntriesForUser() {
        List<JournalEntry> entries;
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = getUserByEmail(email);

        if(user.getRole().equals(Roles.ADMIN)) {
            entries = journalEntryRepository.findAll();
        } else  {
            entries = journalEntryRepository.findAllByUserId(user.getId());
        }
        return JournalEntryDtoMapper.toJournalEntryDto(entries);
    }

    public JournalEntryDto getEntryById(UUID entryId) throws AccessDeniedException {
        JournalEntry entry = getJournalEntryForCurrentUser(entryId);
        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    public JournalEntryDto createJournalEntry(@Valid JournalEntryRequest entryRequest, String email) {
        User user = getUserByEmail(email);
        JournalEntry entry = new JournalEntry();

        entry.setCreatedAt(LocalDate.now());
        entry.setUpdatedAt(LocalDate.now());
        entry.setTitle(entryRequest.title());
        entry.setContent(entryRequest.content());
        entry.setUser(user);
        journalEntryRepository.save(entry);

        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    public JournalEntryDto updateJournalEntry(UUID entryId,
                                              @Valid JournalEntryUpdateRequest entryUpdateRequest) throws AccessDeniedException {
        JournalEntry entry = getJournalEntryForCurrentUser(entryId);
        if(entryUpdateRequest.title() != null && !entryUpdateRequest.title().isEmpty() ) {
            entry.setTitle(entryUpdateRequest.title());
        }
        entry.setContent(entryUpdateRequest.content());
        entry.setUpdatedAt(LocalDate.now());
        journalEntryRepository.save(entry);
        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    public void deleteJournalEntry(UUID entryId) throws AccessDeniedException {
        JournalEntry entry = getJournalEntryForCurrentUser(entryId);
        journalEntryRepository.deleteById(entry.getId());
    }

    private JournalEntry getJournalEntryForCurrentUser(UUID entryId) throws AccessDeniedException {
        JournalEntry entry = getJournalEntryOrThrow(entryId);
        User currentUser = getAuthenticatedUser();
        if(!isOwnerOrAdmin(entry, currentUser)) {
            throw new AccessDeniedException("You are not authorized to access this journal entry!");
        }
        return entry;
    }

    private JournalEntry getJournalEntryOrThrow(UUID id) {
        return journalEntryRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Could not find journal entry!"));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Authenticated user not found!"));
    }

    private boolean isOwnerOrAdmin(JournalEntry entry, User user) {
        return user.getRole().equals(Roles.ADMIN) || entry.getUser().getId().equals(user.getId());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }
}
