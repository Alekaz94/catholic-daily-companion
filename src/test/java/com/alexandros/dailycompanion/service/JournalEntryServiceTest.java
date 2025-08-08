package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.JournalEntryRequest;
import com.alexandros.dailycompanion.dto.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.dto.UserRequest;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.JournalEntry;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.JournalEntryRepository;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private JournalEntry journalEntry;
    private JournalEntryRequest journalEntryRequest;
    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPassword(PasswordUtil.hashPassword("password"));
        user.setRole(Roles.USER);
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());

        journalEntry = new JournalEntry();
        journalEntry.setId(UUID.randomUUID());
        journalEntry.setCreatedAt(LocalDate.of(2025, 10, 5));
        journalEntry.setUpdatedAt(LocalDate.of(2025, 10, 5));
        journalEntry.setTitle("Hello there");
        journalEntry.setContent("Hello to you too.");

        userRequest = new UserRequest("Test", "User", "test@example.com", "password");
        journalEntryRequest = new JournalEntryRequest("Hello there", "Hello to you too.");
    }

    private void mockAuthenticatedUser(User user) {
        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRole().name())
                        .build();

        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createJournalEntryShouldSaveNewJournalEntryAndReturnDto() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.of(user));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntryDto result = journalEntryService.createJournalEntry(journalEntryRequest, user.getEmail());

        assertEquals(journalEntryRequest.content(), result.content());
        assertEquals(journalEntryRequest.title(), result.title());
    }

    @Test
    void createJournalEntryShouldThrowExceptionIfUserDoesNotExist() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> journalEntryService.createJournalEntry(journalEntryRequest, userRequest.email()));
    }

    @Test
    void getAllJournalEntriesShouldReturnJournalEntryDtoListForUser() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<JournalEntry> page = new PageImpl<>(List.of(journalEntry));
        mockAuthenticatedUser(user);
        journalEntry.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(journalEntryRepository.findAllByUserId(user.getId(), pageable)).thenReturn(page);
        Page<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUser(0, 5, "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("Hello there", result.getContent().getFirst().title());
        assertEquals("Hello to you too.", result.getContent().getFirst().content());
    }

    @Test
    void getAllJournalEntriesShouldReturnJournalEntryDtoListForAdmin() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<JournalEntry> page = new PageImpl<>(List.of(journalEntry));

        user.setRole(Roles.ADMIN);
        mockAuthenticatedUser(user);
        journalEntry.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(journalEntryRepository.findAll(pageable)).thenReturn(page);
        Page<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUser(0, 5, "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("Hello there", result.getContent().getFirst().title());
        assertEquals("Hello to you too.", result.getContent().getFirst().content());
    }

    @Test
    void getEntryByIdShouldReturnDtoIfOwnerOrAdmin() throws AccessDeniedException {
        mockAuthenticatedUser(user);
        journalEntry.setUser(user);

        when(journalEntryRepository.findById(journalEntry.getId())).thenReturn(Optional.of(journalEntry));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        JournalEntryDto result = journalEntryService.getEntryById(journalEntry.getId());

        assertEquals(journalEntry.getTitle(), result.title());
    }

    @Test
    void getEntryByIdShouldThrowAccessDeniedIfUserNotOwner() {
        User ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setEmail("owner@example.com");
        ownerUser.setRole(Roles.USER);
        ownerUser.setPassword("password");
        journalEntry.setUser(ownerUser);

        User userTwo = new User();
        userTwo.setId(UUID.randomUUID());
        userTwo.setEmail("other@example.com");
        userTwo.setRole(Roles.USER);
        userTwo.setPassword("passwordTwo");

        mockAuthenticatedUser(userTwo);

        when(journalEntryRepository.findById(journalEntry.getId())).thenReturn(Optional.of(journalEntry));
        when(userRepository.findByEmail(userTwo.getEmail())).thenReturn(Optional.of(userTwo));

        assertThrows(AccessDeniedException.class, () -> journalEntryService.getEntryById(journalEntry.getId()));
    }

    @Test
    void updateJournalEntryShouldUpdateFields() throws Exception {
        JournalEntryUpdateRequest updateRequest = new JournalEntryUpdateRequest("Updated title", "Updated content");
        mockAuthenticatedUser(user);
        journalEntry.setUser(user);

        when(journalEntryRepository.findById(journalEntry.getId())).thenReturn(Optional.of(journalEntry));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JournalEntryDto result = journalEntryService.updateJournalEntry(journalEntry.getId(), updateRequest);

        assertEquals("Updated title", result.title());
        assertEquals("Updated content", result.content());
    }

    @Test
    void deleteJournalEntryShouldSucceedIfOwner() throws Exception {
        journalEntry.setUser(user);
        mockAuthenticatedUser(user);

        when(journalEntryRepository.findById(journalEntry.getId())).thenReturn(Optional.of(journalEntry));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        journalEntryService.deleteJournalEntry(journalEntry.getId());
    }
}
