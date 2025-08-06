package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.DailyReadingDto;
import com.alexandros.dailycompanion.dto.DailyReadingRequest;
import com.alexandros.dailycompanion.mapper.DailyReadingDtoMapper;
import com.alexandros.dailycompanion.model.DailyReading;
import com.alexandros.dailycompanion.repository.DailyReadingRepository;
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

    public DailyReadingDto getTodaysReading() {
        LocalDate today = LocalDate.now();
        System.out.println("All readings:");
        dailyReadingRepository.findAll().forEach(r -> System.out.println(r.getCreatedAt()));

        DailyReading todaysReading = dailyReadingRepository.findByCreatedAt(today)
                .orElseThrow(() -> new IllegalArgumentException("Could not find today's reading!"));
        return DailyReadingDtoMapper.toDailyReadingDto(todaysReading);
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

