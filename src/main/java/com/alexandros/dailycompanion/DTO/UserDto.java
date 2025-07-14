package com.alexandros.dailycompanion.DTO;

import java.util.UUID;

public record UserDto(UUID id,
                      String firstName,
                      String lastName,
                      String email) {
}
