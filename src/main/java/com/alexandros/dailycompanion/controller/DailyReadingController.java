package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.DailyReadingDto;
import com.alexandros.dailycompanion.dto.DailyReadingRequest;
import com.alexandros.dailycompanion.dto.DailyReadingUpdateRequest;
import com.alexandros.dailycompanion.service.DailyReadingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/daily-reading")
@Validated
public class DailyReadingController {
    private final DailyReadingService dailyReadingService;

    @Autowired
    public DailyReadingController(DailyReadingService dailyReadingService) {
        this.dailyReadingService = dailyReadingService;
    }

    @GetMapping
    public ResponseEntity<Page<DailyReadingDto>> getAllReadings(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size,
                                                                @RequestParam(defaultValue = "desc") String sort) {
        Page<DailyReadingDto> readings = dailyReadingService.getAllReadings(page, size, sort);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/{readingId}")
    public ResponseEntity<DailyReadingDto> getDailyReading(@PathVariable UUID readingId) {
        DailyReadingDto reading = dailyReadingService.getDailyReading(readingId);
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/today")
    public ResponseEntity<DailyReadingDto> getTodaysReading() {
        DailyReadingDto todaysReading = dailyReadingService.getTodaysReading();
        return ResponseEntity.ok(todaysReading);
    }

    @PostMapping
    public ResponseEntity<DailyReadingDto> createDailyReading(@Valid @RequestBody DailyReadingRequest readingRequest) {
        DailyReadingDto reading = dailyReadingService.createReading(readingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(reading);
    }

    @PutMapping("/{readingId}")
    public ResponseEntity<DailyReadingDto> updateDailyReading(@PathVariable UUID readingId,
                                                              @RequestBody DailyReadingUpdateRequest dailyReadingUpdateRequest) {
        DailyReadingDto reading = dailyReadingService.updateReading(readingId, dailyReadingUpdateRequest);
        return ResponseEntity.ok(reading);
    }

    @DeleteMapping("/{readingId}")
    public ResponseEntity<Void> deleteDailyReading(@PathVariable UUID readingId) {
        dailyReadingService.deleteReading(readingId);
        return ResponseEntity.noContent().build();
    }
}
