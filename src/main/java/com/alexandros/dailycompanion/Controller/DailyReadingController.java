package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.DailyReadingDto;
import com.alexandros.dailycompanion.DTO.DailyReadingRequest;
import com.alexandros.dailycompanion.Model.DailyReading;
import com.alexandros.dailycompanion.Service.DailyReadingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<DailyReadingDto>> getAllReadings() {
        List<DailyReadingDto> readings = dailyReadingService.getAllReadings();
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/${readingId}")
    public ResponseEntity<DailyReadingDto> getDailyReading(@PathVariable UUID readingId) {
        DailyReadingDto reading = dailyReadingService.getDailyReading(readingId);
        return ResponseEntity.ok(reading);
    }

    @PostMapping
    public ResponseEntity<DailyReadingDto> createDailyReading(@Valid @RequestBody DailyReadingRequest readingRequest) {
        DailyReadingDto reading = dailyReadingService.createReading(readingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(reading);
    }

    // Updating or deleting daily readings are intentionally not supported at the moment.
}
