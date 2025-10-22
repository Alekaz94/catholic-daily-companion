package com.alexandros.dailycompanion.dto;

public record TokenRefreshResponse(String token,
                                   String refreshToken) {
}
