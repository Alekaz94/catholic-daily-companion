package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(@RequestParam(required = false, defaultValue = "") String query,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "5") int size,
                                                             @RequestParam(defaultValue = "email") String sortBy,
                                                             @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UserDto> users = userService.getAllUsers(query, page, size, sortBy, sortDir);

        PageResponse<UserDto> response = new PageResponse<>(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) throws AccessDeniedException {
        UserDto user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDto user = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
                                              @Valid @RequestBody UserUpdateRequest userUpdateRequest) throws AccessDeniedException {
        UserDto user = userService.updateUserPassword(userId, userUpdateRequest);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-name/{userId}")
    public ResponseEntity<UserDto> updateUserName(@PathVariable UUID userId,
                                                  @RequestBody UserNameUpdateRequest userNameUpdateRequest) throws AccessDeniedException, BadRequestException {
        UserDto user = userService.updateUserName(userId, userNameUpdateRequest);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) throws AccessDeniedException {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
