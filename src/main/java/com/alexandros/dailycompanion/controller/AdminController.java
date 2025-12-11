/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.AdminUserListDto;
import com.alexandros.dailycompanion.dto.AdminUserOverviewDto;
import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

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

    @GetMapping("/users/{userId}/overview")
    public ResponseEntity<AdminUserOverviewDto> getUserOverview(@PathVariable UUID userId,
                                                                @RequestParam(defaultValue = "0") int feedbackPage,
                                                                @RequestParam(defaultValue = "10") int feedbackSize,
                                                                @RequestParam(defaultValue = "desc") String feedbackSort) throws AccessDeniedException {
        AdminUserOverviewDto overview = adminService.getUserOverview(userId, feedbackPage, feedbackSize, feedbackSort);
        return ResponseEntity.ok(overview);
    }
}
