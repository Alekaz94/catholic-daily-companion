package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.*;
import com.alexandros.dailycompanion.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceHelper {

    private final static Logger logger = LoggerFactory.getLogger(ServiceHelper.class);
    private final UserRepository userRepository;
    private final SaintRepository saintRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final RosaryLogRepository rosaryLogRepository;

    @Autowired
    public ServiceHelper(UserRepository userRepository,
                         SaintRepository saintRepository,
                         JournalEntryRepository journalEntryRepository,
                         RosaryLogRepository rosaryLogRepository) {
        this.userRepository = userRepository;
        this.saintRepository = saintRepository;
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

    public String trimContent(String content) {
        if(content == null) {
            return "";
        }

        return content.length() > 80 ? content.substring(0, 80) + "..." : content;
    }

    public String getClientIp(HttpServletRequest request) {
        if(request == null) {
            return "UNKNOWN";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");
        String remoteAddress = request.getRemoteAddr();

        String clientIp = Optional.ofNullable(forwardedFor)
                .map(x -> x.split(",")[0].trim())
                .filter(ip -> !ip.isEmpty())
                .or(() -> Optional.ofNullable(realIp))
                .or(() -> Optional.ofNullable(remoteAddress))
                .orElse("UNKNOWN");

        logger.debug("Resolved client IP: {} (X-Forwarded-For='{}'), X-Real-IP='{}', RemoteAddress='{}'",
                clientIp, forwardedFor, realIp, remoteAddress);
        return clientIp;
    }

    private boolean isOwnerOrAdmin(JournalEntry entry, User user) {
        return user.getRole().equals(Roles.ADMIN) || entry.getUser().getId().equals(user.getId());
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UsernameNotFoundException("No authenticated user found in security context!");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Authenticated user not found!"));
    }
}
