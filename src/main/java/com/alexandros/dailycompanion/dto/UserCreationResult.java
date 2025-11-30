package com.alexandros.dailycompanion.dto;

import com.alexandros.dailycompanion.model.User;

public record UserCreationResult(User user,
                                 boolean isNew) {
}
