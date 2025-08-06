package com.alexandros.dailycompanion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(@NotNull String firstName,
                          @NotNull String lastName,
                          @NotNull String email,
                          @NotNull @Size(min = 8, message = "Password must be atleast 8 characters long") String password) {
}
