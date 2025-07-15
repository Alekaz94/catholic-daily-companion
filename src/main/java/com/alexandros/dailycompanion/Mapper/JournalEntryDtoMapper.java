package com.alexandros.dailycompanion.Mapper;

import com.alexandros.dailycompanion.DTO.JournalEntryDto;
import com.alexandros.dailycompanion.Model.JournalEntry;

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
                journalEntry.getContent()
        );
    }

    public static List<JournalEntryDto> toJournalEntryDto(List<JournalEntry> journalEntries) {
        return journalEntries.stream().map(JournalEntryDtoMapper::toJournalEntryDto).toList();
    }
}
