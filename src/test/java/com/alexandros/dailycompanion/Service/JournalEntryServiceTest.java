package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.JournalEntryDto;
import com.alexandros.dailycompanion.DTO.JournalEntryRequest;
import com.alexandros.dailycompanion.DTO.JournalEntryUpdateRequest;
import com.alexandros.dailycompanion.DTO.UserRequest;
import com.alexandros.dailycompanion.Enum.Roles;
import com.alexandros.dailycompanion.Model.JournalEntry;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Repository.JournalEntryRepository;
import com.alexandros.dailycompanion.Repository.UserRepository;
import com.alexandros.dailycompanion.Security.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        mockAuthenticatedUser(user);
        journalEntry.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(journalEntryRepository.findAllByUserId(user.getId())).thenReturn(List.of(journalEntry));
        List<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUser();

        assertEquals(1, result.size());
        assertEquals("Hello there", result.getFirst().title());
        assertEquals("Hello to you too.", result.getFirst().content());
    }

    @Test
    void getAllJournalEntriesShouldReturnJournalEntryDtoListForAdmin() {
        user.setRole(Roles.ADMIN);
        mockAuthenticatedUser(user);
        journalEntry.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(journalEntryRepository.findAll()).thenReturn(List.of(journalEntry));
        List<JournalEntryDto> result = journalEntryService.getAllJournalEntriesForUser();

        assertEquals(1, result.size());
        assertEquals("Hello there", result.getFirst().title());
        assertEquals("Hello to you too.", result.getFirst().content());
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
