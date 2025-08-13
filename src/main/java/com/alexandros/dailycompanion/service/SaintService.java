package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.dto.SaintRequest;
import com.alexandros.dailycompanion.dto.SaintUpdateRequest;
import com.alexandros.dailycompanion.mapper.SaintDtoMapper;
import com.alexandros.dailycompanion.model.Saint;
import com.alexandros.dailycompanion.repository.SaintRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.MonthDay;
import java.util.UUID;

@Service
public class SaintService {
    private final SaintRepository saintRepository;
    private final ServiceHelper serviceHelper;

    @Autowired
    public SaintService(SaintRepository saintRepository, ServiceHelper serviceHelper) {
        this.saintRepository = saintRepository;
        this.serviceHelper = serviceHelper;
    }

    public Page<SaintDto> getAllSaints(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Saint> saints;
        if (query == null || query.trim().isEmpty()) {
            saints = saintRepository.findAll(pageable);
        } else {
            saints = saintRepository.findByNameContainingIgnoreCase(query, pageable);
        }
        return SaintDtoMapper.toSaintDto(saints);
    }

    public SaintDto getSaint(UUID saintId) {
        Saint saint = serviceHelper.getSaintById(saintId);
        return SaintDtoMapper.toSaintDto(saint);
    }

    public SaintDto getSaintByFeastDay() {
        MonthDay today = MonthDay.now();
        return saintRepository.findByFeastDay(today)
                .map(SaintDtoMapper::toSaintDto)
                .orElse(null);
    }

    public SaintDto createSaint(@Valid SaintRequest saintRequest) {
        Saint saint = new Saint();
        saint.setName(saintRequest.name());
        saint.setBirthYear(saintRequest.birthYear());
        saint.setDeathYear(saintRequest.deathYear());
        saint.setFeastDay(saintRequest.feastDay());
        saint.setBiography(saintRequest.biography());
        saint.setPatronage(saintRequest.patronage());
        saint.setCanonizationYear(saintRequest.canonizationYear());
        saint.setImageUrl(saintRequest.imageUrl());
        saintRepository.save(saint);
        return SaintDtoMapper.toSaintDto(saint);
    }

    public SaintDto updateSaint(UUID saintId, SaintUpdateRequest saintUpdateRequest) {
        Saint currentSaint = serviceHelper.getSaintById(saintId);
        if(saintUpdateRequest.name() != null) {
            currentSaint.setName(saintUpdateRequest.name());
        }
        if(saintUpdateRequest.birthYear() != null) {
            currentSaint.setBirthYear(saintUpdateRequest.birthYear());
        }
        if(saintUpdateRequest.deathYear() != null) {
            currentSaint.setDeathYear(saintUpdateRequest.deathYear());
        }
        if(saintUpdateRequest.feastDay() != null) {
            currentSaint.setFeastDay(saintUpdateRequest.feastDay());
        }
        if(saintUpdateRequest.biography() != null) {
            currentSaint.setBiography(saintUpdateRequest.biography());
        }
        if(saintUpdateRequest.canonizationYear() != null) {
            currentSaint.setCanonizationYear(saintUpdateRequest.canonizationYear());
        }
        if(saintUpdateRequest.patronage() != null) {
            currentSaint.setPatronage(saintUpdateRequest.patronage());
        }
        if(saintUpdateRequest.imageUrl() != null) {
            currentSaint.setImageUrl(saintUpdateRequest.imageUrl());
        }
        saintRepository.save(currentSaint);
        return SaintDtoMapper.toSaintDto(currentSaint);
    }

    public void deleteSaint(UUID saintId) {
        Saint saint = serviceHelper.getSaintById(saintId);
        saintRepository.deleteById(saint.getId());
    }
}
