/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

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
import java.util.UUID;

/**
 * REST controller responsible for managing user-related operations.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *     <li>Retrieving user accounts with pagination and filtering</li>
 *     <li>Accessing the authenticated user's dashboard data</li>
 *     <li>Creating, updating, and deleting user accounts</li>
 *     <li>Updating user credentials and profile information</li>
 * </ul>
 * All endpoints enforce authorization rules to ensure users can only
 * access or modify data they are permitted to manage.
 */
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

    /**
     * Retrieves a paginated list of users with optional search and sorting.
     *
     * @param query   optional search query (e.g. email or name)
     * @param page    zero-based page index
     * @param size    number of users per page
     * @param sortBy  field to sort by
     * @param sortDir sort direction ("asc" or "desc")
     * @return paginated list of users
     */
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

    /**
     * Retrieves dashboard data for the currently authenticated user.
     * <p>
     * This typically includes aggregated statistics and recent activity
     * relevant to the user's account.
     *
     * @return dashboard data for the current user
     */
    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardDto> getDashboard() {
        UserDashboardDto dashboard = userService.getDashboardForCurrentUser();

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Retrieves details for a specific user.
     *
     * @param userId identifier of the user to retrieve
     * @return user details
     * @throws AccessDeniedException if access to the requested user is not permitted
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) throws AccessDeniedException {
        UserDto user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Creates a new user account.
     *
     * @param userRequest request containing user registration data
     * @param request     HTTP request used to extract client IP address
     * @return created user
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        UserDto user = userService.createUser(userRequest, ipAddress);
        logger.info("POST /user | User created | user={} | ip={}", user.id(), ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Updates a user's password.
     *
     * @param userId            identifier of the user to update
     * @param userUpdateRequest request containing new password data
     * @param request           HTTP request used to extract client IP address
     * @return updated user
     * @throws AccessDeniedException if the operation is not permitted
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
                                              @Valid @RequestBody UserUpdateRequest userUpdateRequest,
                                              HttpServletRequest request) throws AccessDeniedException {
        String ipAddress = serviceHelper.getClientIp(request);
        UserDto user = userService.updateUserPassword(userId, userUpdateRequest, ipAddress);
        logger.info("PUT /user/{} | Updating password | user={} | ip={}", userId, user.id(), ipAddress);
        return ResponseEntity.ok(user);
    }

    /**
     * Updates a user's display name.
     *
     * @param userId                 identifier of the user to update
     * @param userNameUpdateRequest  request containing updated name information
     * @param request                HTTP request used to extract client IP address
     * @return updated user
     * @throws AccessDeniedException if the operation is not permitted
     * @throws BadRequestException   if the provided data is invalid
     */
    @PutMapping("/update-name/{userId}")
    public ResponseEntity<UserDto> updateUserName(@PathVariable UUID userId,
                                                  @RequestBody UserNameUpdateRequest userNameUpdateRequest,
                                                  HttpServletRequest request) throws AccessDeniedException, BadRequestException {
        String ipAddress = serviceHelper.getClientIp(request);
        UserDto user = userService.updateUserName(userId, userNameUpdateRequest, ipAddress);
        logger.info("PUT /user/update-name/{} | Updating name | user={} | ip={}", userId, user.id(), ipAddress);
        return ResponseEntity.ok(user);
    }

    /**
     * Deletes a user account.
     * <p>
     * This operation permanently removes the user and associated data.
     *
     * @param userId  identifier of the user to delete
     * @param request HTTP request used to extract client IP address
     * @return {@code 204 No Content} on successful deletion
     * @throws AccessDeniedException if the operation is not permitted
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId, HttpServletRequest request) throws AccessDeniedException {
        String ipAddress = serviceHelper.getClientIp(request);
        userService.deleteUser(userId, ipAddress);
        logger.info("DELETE /user/{} | Deleting user | ip={}", userId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
