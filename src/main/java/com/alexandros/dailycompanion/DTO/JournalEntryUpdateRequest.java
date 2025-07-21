package com.alexandros.dailycompanion.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JournalEntryUpdateRequest(String title,
                                        @Size(min = 1, message = "Content can not be empty!") String content) {
}
