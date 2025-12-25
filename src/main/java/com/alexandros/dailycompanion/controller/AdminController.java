/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.AdminUserListDto;
import com.alexandros.dailycompanion.dto.AdminUserOverviewDto;
import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

/**
 * REST controller responsible for administrative operations.
 * <p>
 * Provides endpoints for managing and inspecting application users,
 * including user listing with pagination and detailed user overviews.
 * All endpoints under this controller are intended for admin-level access only.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Retrieves a paginated list of users for administrative purposes.
     * <p>
     * Supports optional searching, pagination, and sorting.
     *
     * @param query   optional search query (e.g. email)
     * @param page    zero-based page index (default: 0)
     * @param size    number of users per page (default: 10)
     * @param sortBy  field to sort by (default: email)
     * @param sortDir sort direction, either {@code asc} or {@code desc} (default: asc)
     * @return paginated response containing user list data
     */
    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserListDto>> getAllUsers(@RequestParam(required = false, defaultValue = "") String query,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "email") String sortBy,
                                                              @RequestParam(defaultValue = "asc") String sortDir) {
        Page<AdminUserListDto> users = adminService.getAllUsersForAdmin(query, page, size, sortBy, sortDir);

        PageResponse<AdminUserListDto> response = new PageResponse<>(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a detailed overview of a specific user.
     * <p>
     * The overview may include profile details, activity data,
     * and paginated feedback or interactions associated with the user.
     *
     * @param userId        unique identifier of the user
     * @param feedbackPage zero-based page index for user feedback (default: 0)
     * @param feedbackSize number of feedback items per page (default: 10)
     * @param feedbackSort sort direction for feedback items (default: desc)
     * @return detailed overview of the specified user
     * @throws AccessDeniedException if the requesting user is not authorized
     *                               to access this user's data
     */
    @GetMapping("/users/{userId}/overview")
    public ResponseEntity<AdminUserOverviewDto> getUserOverview(@PathVariable UUID userId,
                                                                @RequestParam(defaultValue = "0") int feedbackPage,
                                                                @RequestParam(defaultValue = "10") int feedbackSize,
                                                                @RequestParam(defaultValue = "desc") String feedbackSort) throws AccessDeniedException {
        AdminUserOverviewDto overview = adminService.getUserOverview(userId, feedbackPage, feedbackSize, feedbackSort);
        return ResponseEntity.ok(overview);
    }
}
