/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import com.alexandros.dailycompanion.enums.Roles;

import java.util.UUID;

public record AdminUserListDto(UUID id,
                               String email,
                               Roles role) {
}
