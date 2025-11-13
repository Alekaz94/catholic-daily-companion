package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.service.ServiceHelper;
import com.alexandros.dailycompanion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ServiceHelper serviceHelper;

    @Autowired
    public UserController(UserService userService, ServiceHelper serviceHelper) {
        this.userService = userService;
        this.serviceHelper = serviceHelper;
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

        logger.info("GET /user?page={}&size={} | query='{}'", page, size, query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) throws AccessDeniedException {
        UserDto user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        UserDto user = userService.createUser(userRequest, ipAddress);
        logger.info("POST /user | User created | user={} | ip={}", user.id(), ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
                                              @Valid @RequestBody UserUpdateRequest userUpdateRequest,
                                              HttpServletRequest request) throws AccessDeniedException {
        String ipAddress = serviceHelper.getClientIp(request);
        UserDto user = userService.updateUserPassword(userId, userUpdateRequest, ipAddress);
        logger.info("PUT /user/{} | Updating password | user={} | ip={}", userId, user.id(), ipAddress);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-name/{userId}")
    public ResponseEntity<UserDto> updateUserName(@PathVariable UUID userId,
                                                  @RequestBody UserNameUpdateRequest userNameUpdateRequest,
                                                  HttpServletRequest request) throws AccessDeniedException, BadRequestException {
        String ipAddress = serviceHelper.getClientIp(request);
        UserDto user = userService.updateUserName(userId, userNameUpdateRequest, ipAddress);
        logger.info("PUT /user/update-name/{} | Updating name | user={} | ip={}", userId, user.id(), ipAddress);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId, HttpServletRequest request) throws AccessDeniedException {
        String ipAddress = serviceHelper.getClientIp(request);
        userService.deleteUser(userId, ipAddress);
        logger.info("DELETE /user/{} | Deleting user | ip={}", userId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
