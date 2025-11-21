/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import com.alexandros.dailycompanion.enums.Roles;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(UUID id,
                      String firstName,
                      String lastName,
                      String email,
                      Roles role,
                      LocalDate createdAt,
                      LocalDate updatedAt) {
}
