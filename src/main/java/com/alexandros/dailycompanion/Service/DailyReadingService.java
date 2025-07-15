package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.DailyReadingDto;
import com.alexandros.dailycompanion.DTO.DailyReadingRequest;
import com.alexandros.dailycompanion.Mapper.DailyReadingDtoMapper;
import com.alexandros.dailycompanion.Model.DailyReading;
import com.alexandros.dailycompanion.Repository.DailyReadingRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DailyReadingService {
    private final DailyReadingRepository dailyReadingRepository;

    @Autowired
    public DailyReadingService(DailyReadingRepository dailyReadingRepository) {
        this.dailyReadingRepository = dailyReadingRepository;
    }

    public DailyReadingDto getDailyReading(UUID id) {
        DailyReading reading = getDailyReadingById(id);
        return DailyReadingDtoMapper.toDailyReadingDto(reading);
    }

    public List<DailyReadingDto> getAllReadings() {
        List<DailyReading> readings = dailyReadingRepository.findAll();
        return DailyReadingDtoMapper.toDailyReadingDto(readings);
    }

    public DailyReadingDto createReading(@Valid DailyReadingRequest readingRequest) {
        DailyReading reading = new DailyReading();

        reading.setCreatedAt(LocalDate.now());
        reading.setFirstReading(readingRequest.firstReading());
        reading.setSecondReading(readingRequest.secondReading());
        reading.setPsalm(readingRequest.psalm());
        reading.setGospel(readingRequest.gospel());

        dailyReadingRepository.save(reading);

        return DailyReadingDtoMapper.toDailyReadingDto(reading);
    }

    private DailyReading getDailyReadingById(UUID id) {
        return dailyReadingRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find daily reading with id: %s", id)));
    }
}

