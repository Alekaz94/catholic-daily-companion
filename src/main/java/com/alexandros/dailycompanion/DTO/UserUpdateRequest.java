package com.alexandros.dailycompanion.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(@NotNull String currentPassword,
                                @NotNull @Size(min = 8, message = "Password must be atleast 8 characters long") String newPassword) {
}
