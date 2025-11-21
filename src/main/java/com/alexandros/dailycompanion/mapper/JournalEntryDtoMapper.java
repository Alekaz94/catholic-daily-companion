/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.model.JournalEntry;
import org.springframework.data.domain.Page;

import java.util.List;

public class JournalEntryDtoMapper {

    public static JournalEntryDto toJournalEntryDto(JournalEntry journalEntry) {
        if(journalEntry == null) {
            return null;
        }

        return new JournalEntryDto(
                journalEntry.getId(),
                journalEntry.getCreatedAt(),
                journalEntry.getUpdatedAt(),
                journalEntry.getTitle(),
                journalEntry.getContent()
        );
    }

    public static List<JournalEntryDto> toJournalEntryDto(List<JournalEntry> journalEntries) {
        return journalEntries.stream().map(JournalEntryDtoMapper::toJournalEntryDto).toList();
    }

    public static Page<JournalEntryDto> toJournalEntryDto(Page<JournalEntry> entries) {
        return entries.map(JournalEntryDtoMapper::toJournalEntryDto);
    }
}
