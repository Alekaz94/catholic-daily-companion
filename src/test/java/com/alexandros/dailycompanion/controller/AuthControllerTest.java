/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.exception.GlobalExceptionHandler;
import com.alexandros.dailycompanion.model.RefreshToken;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.service.RefreshTokenService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import com.alexandros.dailycompanion.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ServiceHelper serviceHelper;

    @InjectMocks
    private AuthController authController;

    private User user;
    private UserDto userDto;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        when(serviceHelper.getClientIp(any())).thenReturn("127.0.0.1");

        user = new User();
        user.setId(UUID.randomUUID());

        userDto = new UserDto(user.getId(), "Test", "User", "test@example.com", Roles.USER, LocalDate.now(), LocalDate.now());

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
    }

    @Test
    void login_success() throws Exception {
        LoginResponse loginResponse = new LoginResponse(userDto, "token", refreshToken.getToken());

        when(serviceHelper.getClientIp(any())).thenReturn("127.0.0.1");
        when(userService.login(any())).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void login_invalidInput_shouldReturn400() throws Exception {
        String invalidJson = """
    {
        "email": "test@example.com"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_failure_invalidCredentials() throws Exception {
        when(serviceHelper.getClientIp(any())).thenReturn("127.0.0.1");
        when(userService.login(any())).thenThrow(BadCredentialsException.class);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials!"));
    }

    @Test
    void signUp_success() throws Exception {
        UserRequest userRequest = new UserRequest("Test", "User", "test@example.com", "password");
        when(serviceHelper.getClientIp(any())).thenReturn("127.0.0.1");
        when(userService.signUp(any(), any())).thenReturn(user);
        when(jwtUtil.generateToken(any())).thenReturn("token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Test\", \"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void signUp_existingEmail_shouldReturn409() throws Exception {
        when(userService.signUp(any(), any()))
                .thenThrow(new IllegalStateException("Email already exists"));

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Test\", \"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void refreshToken_success() throws Exception {
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("refresh-token");
        newRefreshToken.setUser(user);

        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.createRefreshToken(any())).thenReturn(newRefreshToken);
        when(jwtUtil.generateToken(any())).thenReturn("new-access-token");

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refreshToken_invalid() throws Exception {
        when(serviceHelper.getClientIp(any())).thenReturn("127.0.0.1");
        when(refreshTokenService.findByToken("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid refresh token."));
    }

    @Test
    void refreshToken_missing_shouldReturn401() throws Exception {
        when(refreshTokenService.findByToken("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid refresh token."));
    }
}
