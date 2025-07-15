package com.alexandros.dailycompanion.DTO;

import com.alexandros.dailycompanion.Enum.Roles;

import java.util.UUID;

public record UserDto(UUID id,
                      String firstName,
                      String lastName,
                      String email,
                      Roles role) {
}
