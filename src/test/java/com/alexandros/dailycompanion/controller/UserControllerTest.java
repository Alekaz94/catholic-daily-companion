package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.exception.GlobalExceptionHandler;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.service.ServiceHelper;
import com.alexandros.dailycompanion.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private ServiceHelper serviceHelper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        userDto = new UserDto(userId, "Test", "User", "test@example.com", null, null, null);
        when(serviceHelper.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
    }

    @Test
    void getAllUsers_success() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(userDto), PageRequest.of(0, 5), 1);
        when(userService.getAllUsers("", 0, 5, "email", "asc")).thenReturn(page);

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(userId.toString()));
    }

    @Test
    void getUser_success() throws Exception {
        when(userService.getUser(userId)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void getUser_notFound_shouldReturn404() throws Exception {
        UUID someUserId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setRole(Roles.ADMIN);
        when(serviceHelper.getAuthenticatedUser()).thenReturn(currentUser);

        when(userService.getUser(someUserId))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/user/{userId}", someUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void createUser_success() throws Exception {
        UserRequest request = new UserRequest("Test", "User", "test@example.com", "password");
        when(userService.createUser(any(UserRequest.class), any())).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void createUser_invalidInput_shouldReturn400() throws Exception {
        String invalidJson = """
        {
            "email": "test@example.com"
        }
        """; // Missing firstName, lastName, password

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserPassword_success() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest("currentPassword", "newPassword");
        when(userService.updateUserPassword(any(UUID.class), any(UserUpdateRequest.class), any())).thenReturn(userDto);

        mockMvc.perform(put("/api/v1/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"currentPassword\", \"newPassword\":\"newPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void updateUserName_success() throws Exception {
        UserNameUpdateRequest nameRequest = new UserNameUpdateRequest("NewFirstname", "NewLastname");
        when(userService.updateUserName(any(UUID.class), any(UserNameUpdateRequest.class), any())).thenReturn(userDto);

        mockMvc.perform(put("/api/v1/user/update-name/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"NewFirstname\", \"lastName\":\"NewLastname\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/api/v1/user/{userId}", userId))
                .andExpect(status().isNoContent());
    }
}
