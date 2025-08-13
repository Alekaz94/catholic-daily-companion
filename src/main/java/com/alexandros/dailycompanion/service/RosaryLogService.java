package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.mapper.RosaryLogDtoMapper;
import com.alexandros.dailycompanion.model.RosaryLog;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.RosaryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RosaryLogService {

    private final RosaryLogRepository rosaryLogRepository;
    private final ServiceHelper serviceHelper;

    @Autowired
    public RosaryLogService(RosaryLogRepository rosaryLogRepository, ServiceHelper serviceHelper) {
        this.rosaryLogRepository = rosaryLogRepository;
        this.serviceHelper = serviceHelper;
    }

    public RosaryLogDto markCompleted(UUID userId) {
        LocalDate date = LocalDate.now();
        User user = serviceHelper.getUserByIdOrThrow(userId);

        RosaryLog rosaryLog = rosaryLogRepository
                .findByUserIdAndDate(userId, date)
                .orElse(new RosaryLog(user, date, true));

        rosaryLog.setCompleted(true);
        rosaryLogRepository.save(rosaryLog);
        return RosaryLogDtoMapper.toRosaryDto(rosaryLog);
    }

    public boolean isCompletedToday(UUID userId) {
        LocalDate date = LocalDate.now();
        return rosaryLogRepository
                .findByUserIdAndDate(userId, date)
                .map(RosaryLog::isCompleted)
                .orElse(false);
    }

    public List<RosaryLogDto> getHistory(UUID userId) {
        List<RosaryLog> logs = rosaryLogRepository.findAllByUserIdOrderByDateDesc(userId);
        return RosaryLogDtoMapper.toRosaryLogDto(logs);
    }

    public int getStreak(UUID userId) {
        List<RosaryLog> logs = rosaryLogRepository.findAllByUserIdAndCompletedTrueOrderByDateDesc(userId);
        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for(RosaryLog log : logs) {
            if(log.getDate().isEqual(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }
}
