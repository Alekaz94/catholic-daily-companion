/*
package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.DailyReadingDto;
import com.alexandros.dailycompanion.dto.DailyReadingRequest;
import com.alexandros.dailycompanion.dto.DailyReadingUpdateRequest;
import com.alexandros.dailycompanion.mapper.DailyReadingDtoMapper;
import com.alexandros.dailycompanion.model.DailyReading;
import com.alexandros.dailycompanion.repository.DailyReadingRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DailyReadingService {
    private final DailyReadingRepository dailyReadingRepository;
    private final ServiceHelper serviceHelper;

    @Autowired
    public DailyReadingService(DailyReadingRepository dailyReadingRepository, ServiceHelper serviceHelper) {
        this.dailyReadingRepository = dailyReadingRepository;
        this.serviceHelper = serviceHelper;
    }

    public DailyReadingDto getDailyReading(UUID id) {
        DailyReading reading = serviceHelper.getDailyReadingById(id);
        return DailyReadingDtoMapper.toDailyReadingDto(reading);
    }

    public Page<DailyReadingDto> getAllReadings(int page, int size, String sort) {
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<DailyReading> dailyReadings = dailyReadingRepository.findAll(pageable);
        return DailyReadingDtoMapper.toDailyReadingDto(dailyReadings);
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

    public DailyReadingDto updateReading(UUID readingId, DailyReadingUpdateRequest dailyReadingUpdateRequest) {
        DailyReading currentReading = serviceHelper.getDailyReadingById(readingId);
        if(dailyReadingUpdateRequest.firstReading() != null) {
            currentReading.setFirstReading(dailyReadingUpdateRequest.firstReading());
        }
        if(dailyReadingUpdateRequest.secondReading() != null) {
            currentReading.setSecondReading(dailyReadingUpdateRequest.secondReading());
        }
        if(dailyReadingUpdateRequest.psalm() != null) {
            currentReading.setPsalm(dailyReadingUpdateRequest.psalm());
        }
        if(dailyReadingUpdateRequest.firstReading() != null) {
            currentReading.setGospel(dailyReadingUpdateRequest.gospel());
        }
        dailyReadingRepository.save(currentReading);
        return DailyReadingDtoMapper.toDailyReadingDto(currentReading);
    }

    public void deleteReading(UUID readingId) {
        DailyReading dailyReading = serviceHelper.getDailyReadingById(readingId);
        dailyReadingRepository.deleteById(dailyReading.getId());
    }

    public List<DailyReadingDto> getReadingsBetween(LocalDate start, LocalDate end) {
        List<DailyReading> readings = dailyReadingRepository.findByCreatedAtBetween(start, end);
        return readings.stream()
                .map(DailyReadingDtoMapper::toDailyReadingDto)
                .toList();
    }
}

*/
