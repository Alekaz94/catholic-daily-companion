package com.alexandros.dailycompanion.dto;

public record SignupResponse(UserDto user,
                             String token) {
}
