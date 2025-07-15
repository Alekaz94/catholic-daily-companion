package com.alexandros.dailycompanion.Mapper;

import com.alexandros.dailycompanion.DTO.DailyReadingDto;
import com.alexandros.dailycompanion.Model.DailyReading;

import java.util.List;

public class DailyReadingDtoMapper {
    public static DailyReadingDto toDailyReadingDto(DailyReading dailyReading) {
        if(dailyReading == null) {
            return null;
        }

        return new DailyReadingDto(
                dailyReading.getId(),
                dailyReading.getCreatedAt(),
                dailyReading.getUpdatedAt(),
                dailyReading.getFirstReading(),
                dailyReading.getSecondReading(),
                dailyReading.getPsalm(),
                dailyReading.getGospel()
        );
    }

    public static List<DailyReadingDto> toDailyReadingDto(List<DailyReading> dailyReadings) {
        return dailyReadings.stream().map(DailyReadingDtoMapper::toDailyReadingDto).toList();
    }
}
