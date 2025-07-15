package com.alexandros.dailycompanion.DTO;

import java.time.LocalDate;
import java.util.UUID;

public record SaintDto(UUID id,
                       String name,
                       Integer birthYear,
                       Integer deathYear,
                       LocalDate feastDay,
                       String biography) {
}
