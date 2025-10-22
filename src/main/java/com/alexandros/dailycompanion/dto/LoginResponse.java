package com.alexandros.dailycompanion.dto;

public record LoginResponse(UserDto user,
                            String token,
                            String refreshToken) {
}
