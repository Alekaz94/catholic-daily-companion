/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(@NotNull String currentPassword,
                                @NotNull @Size(min = 8, message = "Password must be atleast 8 characters long") String newPassword) {
}
