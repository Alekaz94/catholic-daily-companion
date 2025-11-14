package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.dto.SaintRequest;
import com.alexandros.dailycompanion.dto.SaintUpdateRequest;
import com.alexandros.dailycompanion.dto.PageResponse;
import com.alexandros.dailycompanion.service.SaintService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/saint")
@Validated
public class SaintController {
    private final static Logger logger = LoggerFactory.getLogger(SaintController.class);
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
    public ResponseEntity<List<SaintDto>> getTodaysSaint() {
        List<SaintDto> saint = saintService.getAllSaintsByFeastDay();
        return ResponseEntity.ok(saint);
    }

    @GetMapping("/feast/{feastCode}")
    public ResponseEntity<List<SaintDto>> getSaintByFeastCode(@PathVariable String feastCode) {
        List<SaintDto> saints = saintService.getAllSaintsByFeastCode(feastCode);
        return ResponseEntity.ok(saints);
    }

    @GetMapping("/feast")
    public ResponseEntity<Map<String, List<String>>> getAllFeastDays() {
        Map<String, List<String>> feastMap = saintService.getAllFeastDaysMapped();
        return ResponseEntity.ok(feastMap);
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<Map<String, List<String>>> getSaintsByMonth(@PathVariable int year, @PathVariable int month) {
        Map<String, List<String>> saints = saintService.getSaintsByMonth(year, month);
        return ResponseEntity.ok(saints);
    }

    @PostMapping
    public ResponseEntity<SaintDto> createSaint(@Valid @RequestBody SaintRequest saintRequest) {
        SaintDto saint = saintService.createSaint(saintRequest);
        logger.info("POST /saint | Created saint | saint={} | id={}", saint.name(), saint.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(saint);
    }

    @PutMapping("/{saintId}")
    public ResponseEntity<SaintDto> updateSaint(@PathVariable UUID saintId,
                                                @RequestBody SaintUpdateRequest saintUpdateRequest) {
        SaintDto saint = saintService.updateSaint(saintId, saintUpdateRequest);
        logger.info("PUT /saint/{} | Updated saint | saint={} | id={}", saintId, saint.name(), saint.id());
        return ResponseEntity.ok(saint);
    }

    @DeleteMapping("/{saintId}")
    public ResponseEntity<Void> deleteSaint(@PathVariable UUID saintId) {
        saintService.deleteSaint(saintId);
        logger.info("DELETE /saint/{} | Deleted saint", saintId);
        return ResponseEntity.noContent().build();
    }

}
