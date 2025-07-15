package com.alexandros.dailycompanion.DTO;

public record LoginResponse(UserDto user,
                            String token) {
}
