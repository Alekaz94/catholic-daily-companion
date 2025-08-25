package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.dto.SaintRequest;
import com.alexandros.dailycompanion.dto.SaintUpdateRequest;
import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.service.SaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PageResponse<SaintDto>> getAllSaints(@RequestParam(required = false, defaultValue = "") String query,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "5") int size) {
        Page<SaintDto> saintPage = saintService.getAllSaints(query, page, size);

        PageResponse<SaintDto> response = new PageResponse<>(
                saintPage.getContent(),
                saintPage.getNumber(),
                saintPage.getSize(),
                saintPage.getTotalElements(),
                saintPage.getTotalPages(),
                saintPage.isLast()
        );

        return ResponseEntity.ok(response);
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

    @PutMapping("/{saintId}")
    public ResponseEntity<SaintDto> updateSaint(@PathVariable UUID saintId,
                                                @RequestBody SaintUpdateRequest saintUpdateRequest) {
        SaintDto saint = saintService.updateSaint(saintId, saintUpdateRequest);
        return ResponseEntity.ok(saint);
    }

    @DeleteMapping("/{saintId}")
    public ResponseEntity<Void> deleteSaint(@PathVariable UUID saintId) {
        saintService.deleteSaint(saintId);
        return ResponseEntity.noContent().build();
    }

}
