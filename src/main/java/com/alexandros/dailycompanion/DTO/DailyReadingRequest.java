package com.alexandros.dailycompanion.DTO;

import jakarta.validation.constraints.NotNull;

public record DailyReadingRequest(@NotNull String firstReading,
                                  String secondReading,
                                  @NotNull String psalm,
                                  @NotNull String gospel) {
}
