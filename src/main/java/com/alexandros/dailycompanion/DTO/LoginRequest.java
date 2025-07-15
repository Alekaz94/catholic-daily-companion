package com.alexandros.dailycompanion.DTO;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull String email,
                           @NotNull String password) {
}
