package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.DailyReadingDto;
import com.alexandros.dailycompanion.model.DailyReading;
import org.springframework.data.domain.Page;

import java.util.List;

public class DailyReadingDtoMapper {
    public static DailyReadingDto toDailyReadingDto(DailyReading dailyReading) {
        if(dailyReading == null) {
            return null;
        }

        return new DailyReadingDto(
                dailyReading.getId(),
                dailyReading.getCreatedAt(),
                dailyReading.getFirstReading(),
                dailyReading.getSecondReading(),
                dailyReading.getPsalm(),
                dailyReading.getGospel()
        );
    }

    public static List<DailyReadingDto> toDailyReadingDto(List<DailyReading> dailyReadings) {
        return dailyReadings.stream().map(DailyReadingDtoMapper::toDailyReadingDto).toList();
    }

    public static Page<DailyReadingDto> toDailyReadingDto(Page<DailyReading> dailyReadings) {
        return dailyReadings.map(DailyReadingDtoMapper::toDailyReadingDto);
    }
}
