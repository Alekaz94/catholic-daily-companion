package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.LoginRequest;
import com.alexandros.dailycompanion.dto.LoginResponse;
import com.alexandros.dailycompanion.dto.UserRequest;
import com.alexandros.dailycompanion.dto.UserUpdateRequest;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.security.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserShouldSaveNewUserAndReturnDto() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = userService.createUser(userRequest);

        assertEquals(userRequest.firstName(), result.firstName());
        assertEquals(userRequest.lastName(), result.lastName());
        assertEquals(userRequest.email(), result.email());
    }

    @Test
    void createUserShouldThrowExceptionIfEmailExists() {
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userRequest));
    }

    @Test
    void getAllUsersShouldReturnUserDtoList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        var result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.getFirst().email());
    }

    @Test
    void getUserShouldReturnUserIfAuthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        var result = userService.getUser(user.getId());

        assertEquals(user.getEmail(), result.email());
    }

    @Test
    void getUserShouldThrowAccessDeniedExceptionForUnauthorizedUser() {
        User userTwo = new User();
        userTwo.setId(UUID.randomUUID());
        userTwo.setEmail("userTwo@example.com");
        userTwo.setRole(Roles.USER);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(userTwo.getEmail())
                .password("password")
                .authorities("USER")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );
        when(userRepository.findByEmail("userTwo@example.com")).thenReturn(Optional.of(userTwo));
        assertThrows(AccessDeniedException.class, () -> userService.getUser(user.getId()));
    }

    @Test
    void loginShouldReturnTokenAndUserDto() {
        LoginRequest request = new LoginRequest(user.getEmail(), "password");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("password")
                .authorities("USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("mocked-token");

        LoginResponse response = userService.login(request);

        assertEquals(user.getEmail(), response.user().email());
        assertEquals("mocked-token", response.token());
    }

    @Test
    void loginShouldReturnUnauthorizedOnInvalidCredentials() {
        LoginRequest request = new LoginRequest("wrong@email.com", "wrongPassword");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationException.class, () -> {
            userService.login(request);
        });
    }

    @Test
    void updateUserPasswordShouldUpdatePasswordIfCorrect() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest("password", "newPassword");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of((user)));
        when(userRepository.findPasswordHashById(user.getId())).thenReturn(user.getPassword());
        when(userRepository.save(any())).thenReturn(user);

        var result = userService.updateUserPassword(user.getId(), updateRequest);

        assertEquals(user.getEmail(), result.email());
    }

    @Test
    void deleteUserShouldDeleteUserById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        Mockito.verify(userRepository).deleteById(user.getId());
    }

}
