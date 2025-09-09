package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.*;
import com.alexandros.dailycompanion.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
public class ServiceHelper {

    private final UserRepository userRepository;
    private final SaintRepository saintRepository;
//    private final DailyReadingRepository dailyReadingRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final RosaryLogRepository rosaryLogRepository;

    @Autowired
    public ServiceHelper(UserRepository userRepository,
                         SaintRepository saintRepository,
    //                     DailyReadingRepository dailyReadingRepository,
                         JournalEntryRepository journalEntryRepository,
                         RosaryLogRepository rosaryLogRepository) {
        this.userRepository = userRepository;
        this.saintRepository = saintRepository;
        //this.dailyReadingRepository = dailyReadingRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.rosaryLogRepository = rosaryLogRepository;
    }

    public User getUserByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() ->
                new UsernameNotFoundException("Could not find user!"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    public Saint getSaintById(UUID id) {
        return saintRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find saint with id: %s", id)));
    }

    public JournalEntry getJournalEntryOrThrow(UUID id) {
        return journalEntryRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Could not find journal entry!"));
    }

    public JournalEntry getJournalEntryForCurrentUser(UUID entryId) throws AccessDeniedException {
        JournalEntry entry = getJournalEntryOrThrow(entryId);
        User currentUser = getAuthenticatedUser();
        if(!isOwnerOrAdmin(entry, currentUser)) {
            throw new AccessDeniedException("You are not authorized to access this journal entry!");
        }
        return entry;
    }

/*    public DailyReading getDailyReadingById(UUID id) {
        return dailyReadingRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find daily reading with id: %s", id)));
    }*/

    private boolean isOwnerOrAdmin(JournalEntry entry, User user) {
        return user.getRole().equals(Roles.ADMIN) || entry.getUser().getId().equals(user.getId());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Authenticated user not found!"));
    }
}
