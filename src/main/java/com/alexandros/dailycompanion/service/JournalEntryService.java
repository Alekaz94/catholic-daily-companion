package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.enums.AuditAction;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.JournalEntryDtoMapper;
import com.alexandros.dailycompanion.model.JournalEntry;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.JournalEntryRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class JournalEntryService {
    private final static Logger logger = LoggerFactory.getLogger(JournalEntryService.class);
    private final JournalEntryRepository journalEntryRepository;
    private final ServiceHelper serviceHelper;
    private final AuditLogService auditLogService;

    @Autowired
    public JournalEntryService(JournalEntryRepository journalEntryRepository, ServiceHelper serviceHelper, AuditLogService auditLogService) {
        this.journalEntryRepository = journalEntryRepository;
        this.serviceHelper = serviceHelper;
        this.auditLogService = auditLogService;
    }

    public Page<JournalEntryDto> getAllJournalEntriesForUser(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sort).orElse(Sort.Direction.DESC);

        Sort sortBy = Sort.by(direction, "updatedAt").and(Sort.by(direction, "createdAt"));
        Pageable pageable = PageRequest.of(page, size, sortBy);

        Page<JournalEntry> entries;
        User user = serviceHelper.getAuthenticatedUser();

        if(user.getRole().equals(Roles.ADMIN)) {
            entries = journalEntryRepository.findAll(pageable);
        } else  {
            entries = journalEntryRepository.findAllByUserId(user.getId(), pageable);
        }
        logger.debug("Fetched {} journal entries for user {}", entries.getTotalElements(), user.getId());
        return JournalEntryDtoMapper.toJournalEntryDto(entries);
    }

    public List<JournalEntryDto> getAllJournalEntriesForUserNotPaged(UUID userId) {
        User user = serviceHelper.getUserByIdOrThrow(userId);

        List<JournalEntry> entries = journalEntryRepository.findAllByUserId(user.getId());

        return JournalEntryDtoMapper.toJournalEntryDto(entries);
    }

    public JournalEntryDto getEntryById(UUID entryId) throws AccessDeniedException {
        JournalEntry entry = serviceHelper.getJournalEntryForCurrentUser(entryId);
        logger.debug("Fetched journal entry {} for user {}", entryId, entry.getUser().getId());
        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    @Transactional
    public JournalEntryDto createJournalEntry(@Valid JournalEntryRequest entryRequest, String ipAddress) {
        User user = serviceHelper.getAuthenticatedUser();

        JournalEntry entry = new JournalEntry();
        entry.setCreatedAt(LocalDate.now());
        entry.setUpdatedAt(LocalDate.now());
        entry.setTitle(entryRequest.title());
        entry.setContent(entryRequest.content());
        entry.setUser(user);
        journalEntryRepository.save(entry);

        auditLogService.logAction(user.getId(), AuditAction.CREATE_JOURNAL_ENTRY.name(), "JournalEntry", entry.getId(),
                String.format("{\"title\": \"%s\"}", entry.getTitle()), ipAddress);
        logger.info("Created journal entry '{}' (ID: {}) for user {}", entry.getTitle(), entry.getId(), user.getId());
        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    @Transactional
    public JournalEntryDto updateJournalEntry(UUID entryId,
                                              JournalEntryUpdateRequest entryUpdateRequest,
                                              String ipAddress) throws AccessDeniedException {
        JournalEntry entry = serviceHelper.getJournalEntryForCurrentUser(entryId);
        boolean updated = false;

        if(entryUpdateRequest.title() != null && !entryUpdateRequest.title().isEmpty()) {
            entry.setTitle(entryUpdateRequest.title());
            updated = true;
        }
        if(entryUpdateRequest.content() != null && !entryUpdateRequest.content().isEmpty()) {
            entry.setContent(entryUpdateRequest.content());
            updated = true;
        }

        if(!updated) {
            throw new IllegalArgumentException("No changes provided for update.");
        }

        entry.setUpdatedAt(LocalDate.now());
        journalEntryRepository.save(entry);

        auditLogService.logAction(entry.getUser().getId(), AuditAction.UPDATE_JOURNAL_ENTRY.name(), "JournalEntry", entryId,
                String.format("{\"newTitle\": \"%s\"}", entry.getTitle()), ipAddress);
        logger.info("Updated journal entry {} (ID: {}) for user {}", entryId, entry.getTitle(), entry.getUser().getId());
        return JournalEntryDtoMapper.toJournalEntryDto(entry);
    }

    @Transactional
    public void deleteJournalEntry(UUID entryId, String ipAddress) throws AccessDeniedException {
        JournalEntry entry = serviceHelper.getJournalEntryForCurrentUser(entryId);
        journalEntryRepository.delete(entry);

        auditLogService.logAction(entry.getUser().getId(), AuditAction.DELETE_JOURNAL_ENTRY.name(), "JournalEntry", entryId,
                String.format("{\"deletedTitle\": \"%s\"}", entry.getTitle()), ipAddress);
        logger.info("Deleted journal entry {} (ID: {}) for user {}", entryId, entry.getTitle(), entry.getUser().getId());
    }

    public List<LocalDate> getEntryDates() {
        User currentUser = serviceHelper.getAuthenticatedUser();

        List<JournalEntry> entries = journalEntryRepository.findAllByUserId(currentUser.getId());

        if(entries.isEmpty()) {
            logger.debug("No journal entries found for user {}", currentUser.getId());
            return Collections.emptyList();
        }

        return entries
                .stream()
                .map(JournalEntry::getCreatedAt)
                .toList();
    }

    public List<JournalEntryDto> getEntriesByDate(LocalDate date) {
        User currentUser = serviceHelper.getAuthenticatedUser();

        List<JournalEntry> entries = journalEntryRepository.findEntriesByUserEmailAndCreatedAt(currentUser.getEmail(), date);

        if(entries.isEmpty()) {
            logger.debug("No journal entries found for user {} on date {}", currentUser.getId(), date);
            return Collections.emptyList();
        }

        return entries.stream()
                .map(JournalEntryDtoMapper::toJournalEntryDto)
                .toList();
    }
}
