package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.enums.AuditAction;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.security.PasswordUtil;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

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

        userRequest = new UserRequest("Test", "User", "test@example.com", "password");
    }

    @Test
    void createUserShouldSaveNewUserAndReturnDto() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.createUser(userRequest, "127.0.0.1");

        assertEquals(userRequest.firstName(), result.firstName());
        assertEquals(userRequest.lastName(), result.lastName());
        assertEquals(userRequest.email(), result.email());
        verify(auditLogService).logAction(any(), eq(AuditAction.CREATE_USER.name()), any(), any(), any(), any());
    }

    @Test
    void createUserShouldThrowExceptionIfEmailExists() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userRequest, "127.0.0.1"));
    }

    @Test
    void signUpShouldReturnUserWhenSuccess() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.signUp(userRequest, "127.0.0.1");
        assertEquals(userRequest.email(), result.getEmail());
    }

    @Test
    void signUpShouldThrowExceptionWhenEmailExists() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(userRequest, "127.0.0.1"));
    }

    @Test
    void getUserShouldReturnUserIfAuthorized() throws Exception {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);

        var result = userService.getUser(user.getId());
        assertEquals(user.getEmail(), result.email());
    }

    @Test
    void getUserShouldThrowAccessDeniedForUnauthorizedUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("u2@example.com");
        otherUser.setRole(Roles.USER);

        when(serviceHelper.getAuthenticatedUser()).thenReturn(otherUser);

        assertThrows(AccessDeniedException.class,
                () -> userService.getUser(UUID.randomUUID()));
    }

    @Test
    void updateUserPasswordShouldUpdatePasswordIfCorrect() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest("password", "newPass");
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);
        when(userRepository.findPasswordHashById(user.getId())).thenReturn(user.getPassword());
        when(userRepository.save(any())).thenReturn(user);

        var result = userService.updateUserPassword(user.getId(), updateRequest, "127.0.0.1");
        assertEquals(user.getEmail(), result.email());
    }

    @Test
    void updateUserPasswordShouldThrowOnIncorrectCurrentPassword() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("wrongPass", "newPass");
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);
        when(userRepository.findPasswordHashById(user.getId())).thenReturn(user.getPassword());

        assertThrows(IllegalArgumentException.class, () -> userService.updateUserPassword(user.getId(), updateRequest, "127.0.0.1"));
    }

    @Test
    void updateUserNameShouldUpdateFirstAndLastName() throws Exception {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        when(serviceHelper.getUserByIdOrThrow(user.getId())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);

        UserNameUpdateRequest request = new UserNameUpdateRequest("NewFirst", "NewLast");
        var result = userService.updateUserName(user.getId(), request, "127.0.0.1");
        assertEquals("NewFirst", result.firstName());
        assertEquals("NewLast", result.lastName());
    }

    @Test
    void updateUserNameShouldThrowBadRequestIfNoFields() {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);

        UserNameUpdateRequest request = new UserNameUpdateRequest(null, null);
        assertThrows(BadRequestException.class,
                () -> userService.updateUserName(user.getId(), request, "127.0.0.1"));
    }

    @Test
    void deleteUserShouldDeleteUserIfAuthorized() throws Exception {
        when(serviceHelper.getAuthenticatedUser()).thenReturn(user);
        userService.deleteUser(user.getId(), "127.0.0.1");
        verify(userRepository).deleteById(user.getId());
    }

    @Test
    void deleteUserShouldThrowAccessDeniedIfUnauthorized() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setRole(Roles.USER);
        when(serviceHelper.getAuthenticatedUser()).thenReturn(anotherUser);

        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(user.getId(), "127.0.0.1"));
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsIfExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername(user.getEmail());
        assertEquals(user.getEmail(), userDetails.getUsername());
    }

    @Test
    void loadUserByUsernameShouldThrowIfNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.loadUserByUsername("notfound@example.com"));
    }

}
