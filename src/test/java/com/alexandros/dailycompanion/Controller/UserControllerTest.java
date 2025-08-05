package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.UserDto;
import com.alexandros.dailycompanion.DTO.UserRequest;
import com.alexandros.dailycompanion.DTO.UserUpdateRequest;
import com.alexandros.dailycompanion.Enum.Roles;
import com.alexandros.dailycompanion.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@Import(UserControllerTest.MockConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws AccessDeniedException {
        userDto = new UserDto(userId, "User", "Test", "user@example.com", Roles.USER);
        when(userService.getUser(userId)).thenReturn(userDto);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    void getAllUsersShouldReturnUserList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].email").value("user@example.com"));
    }

    @Test
    void getUserByIdShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/v1/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest("John", "Doe", "user@example.com", "password");
        when(userService.createUser(any(UserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void updateUserPasswordShouldReturnUpdateUser() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest("password", "newPassword");
        when(userService.updateUserPassword(any(), any())).thenReturn(userDto);

        mockMvc.perform(put("/api/v1/user/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/user/" + userId))
                .andExpect(status().isNoContent());
    }
}
