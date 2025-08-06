package com.alexandros.dailycompanion.dto;

import com.alexandros.dailycompanion.enums.Roles;

import java.util.UUID;

public record UserDto(UUID id,
                      String firstName,
                      String lastName,
                      String email,
                      Roles role) {
}
