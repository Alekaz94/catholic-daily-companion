package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.SaintDto;
import com.alexandros.dailycompanion.DTO.SaintRequest;
import com.alexandros.dailycompanion.Service.SaintService;
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
@RequestMapping("/api/v1/saint")
@Validated
public class SaintController {
    private final SaintService saintService;

    @Autowired
    public SaintController(SaintService saintService) {
        this.saintService = saintService;
    }

    @GetMapping
    public ResponseEntity<Page<SaintDto>> getAllSaints(@RequestParam(required = false, defaultValue = "") String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "5") int size) {
        Page<SaintDto> saints = saintService.getAllSaints(query, page, size);
        return ResponseEntity.ok(saints);
    }

    @GetMapping("/{saintId}")
    public ResponseEntity<SaintDto> getSaint(@PathVariable UUID saintId) {
        SaintDto saint = saintService.getSaint(saintId);
        return ResponseEntity.ok(saint);
    }

    @GetMapping("/today")
    public ResponseEntity<SaintDto> getTodaysSaint() {
        SaintDto saint = saintService.getSaintByFeastDay();
        return ResponseEntity.ok(saint);
    }

    @PostMapping
    public ResponseEntity<SaintDto> createSaint(@Valid @RequestBody SaintRequest saintRequest) {
        SaintDto saint = saintService.createSaint(saintRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(saint);
    }

}
