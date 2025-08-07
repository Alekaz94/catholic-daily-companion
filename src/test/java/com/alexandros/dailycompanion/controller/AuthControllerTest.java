package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.LoginRequest;
import com.alexandros.dailycompanion.dto.LoginResponse;
import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.dto.UserRequest;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@Import(AuthControllerTest.MockConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userDto = new UserDto(
                userId,
                "Test",
                "User",
                "user@example.com",
                Roles.USER
        );
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    void loginShouldReturnLoginResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
        LoginResponse loginResponse = new LoginResponse(userDto, "jwt-token");

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("wrong@example.com", "badpassword");

        when(userService.login(any(LoginRequest.class))).thenThrow(new AuthenticationException("Bad credentials") {});

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials!"));
    }

    @Test
    void loginWithMissingEmailShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
              "password": "somePassword"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithBlankPasswordShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
              "email": "user@example.com",
              "password": ""
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signUpShouldReturnUserWithJwtToken() throws Exception {
        UserRequest request = new UserRequest(
                "Test",
                "User",
                "user@example.com",
                "password"
        );

        String token = "jwt-token";

        when(userService.signUp(any(UserRequest.class))).thenReturn(userDto);
        when(jwtUtil.generateToken(eq(userDto.email()))).thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void signUpWithExistingEmailShouldReturnConflict() throws Exception {
        UserRequest request = new UserRequest("Test", "User", "user@example.com", "password");

        when(userService.signUp(any(UserRequest.class)))
                .thenThrow(new IllegalStateException("Email already exists!"));

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists!"));
    }

    @Test
    void signUpWithInvalidEmailFormatShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
              "firstName": "John",
              "lastName": "Doe",
              "email": "not-an-email",
              "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
