/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.dto;

import jakarta.validation.constraints.Size;

public record JournalEntryUpdateRequest(String title,
                                        @Size(min = 1, message = "Content can not be empty!") String content) {
}
