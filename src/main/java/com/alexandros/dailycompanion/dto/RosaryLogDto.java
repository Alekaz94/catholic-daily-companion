package com.alexandros.dailycompanion.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RosaryLogDto(UUID id,
                           LocalDate date,
                           boolean completed) {
}
