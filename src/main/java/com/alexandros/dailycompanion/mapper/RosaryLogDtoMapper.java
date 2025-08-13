package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.model.RosaryLog;

import java.util.List;

public class RosaryLogDtoMapper {
    public static RosaryLogDto toRosaryDto(RosaryLog rosaryLog) {
        if(rosaryLog == null) {
            return null;
        }

        return new RosaryLogDto(
                rosaryLog.getId(),
                rosaryLog.getDate(),
                rosaryLog.isCompleted()
        );
    }

    public static List<RosaryLogDto> toRosaryLogDto(List<RosaryLog> rosaryLogs) {
        return rosaryLogs.stream().map(RosaryLogDtoMapper::toRosaryDto).toList();
    }
}
