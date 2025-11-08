package com.alexandros.dailycompanion.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record UserNameUpdateRequest(@Nullable @Size(min = 3, message = "Firstname must be a minimum of 3 characters") String firstName,
                                    @Nullable @Size(min = 3, message = "Lastname must be a minimum of 3 characters") String lastName) {
}
