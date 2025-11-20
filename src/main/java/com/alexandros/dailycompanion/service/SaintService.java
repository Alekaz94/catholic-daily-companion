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

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        saint.setImageSource(saintRequest.imageSource());
        saint.setImageAuthor(saintRequest.imageAuthor());
        saint.setImageLicence(saintRequest.imageLicence());
        saintRepository.save(saint);
        return SaintDtoMapper.toSaintDto(saint);
    }

    public SaintDto updateSaint(UUID saintId, SaintUpdateRequest saintUpdateRequest) {
        Saint currentSaint = serviceHelper.getSaintById(saintId);
        boolean updated = false;

        if(saintUpdateRequest.name() != null && !saintUpdateRequest.name().isEmpty()) {
            currentSaint.setName(saintUpdateRequest.name());
            updated = true;
        }
        if(saintUpdateRequest.birthYear() != null) {
            currentSaint.setBirthYear(saintUpdateRequest.birthYear());
            updated = true;
        }
        if(saintUpdateRequest.deathYear() != null) {
            currentSaint.setDeathYear(saintUpdateRequest.deathYear());
            updated = true;
        }
        if(saintUpdateRequest.feastDay() != null) {
            currentSaint.setFeastDay(saintUpdateRequest.feastDay());
            updated = true;
        }
        if(saintUpdateRequest.biography() != null && !saintUpdateRequest.biography().isEmpty()) {
            currentSaint.setBiography(saintUpdateRequest.biography());
            updated = true;
        }
        if(saintUpdateRequest.canonizationYear() != null) {
            currentSaint.setCanonizationYear(saintUpdateRequest.canonizationYear());
            updated = true;
        }
        if(saintUpdateRequest.patronage() != null && !saintUpdateRequest.patronage().isEmpty()) {
            currentSaint.setPatronage(saintUpdateRequest.patronage());
            updated = true;
        }
        if(saintUpdateRequest.imageUrl() != null && !saintUpdateRequest.imageUrl().isEmpty()) {
            currentSaint.setImageUrl(saintUpdateRequest.imageUrl());
            updated = true;
        }
        if(saintUpdateRequest.imageSource() != null && !saintUpdateRequest.imageSource().isEmpty()) {
            currentSaint.setImageSource(saintUpdateRequest.imageSource());
            updated = true;
        }
        if(saintUpdateRequest.imageAuthor() != null && !saintUpdateRequest.imageAuthor().isEmpty()) {
            currentSaint.setImageAuthor(saintUpdateRequest.imageAuthor());
            updated = true;
        }
        if(saintUpdateRequest.imageLicence() != null && !saintUpdateRequest.imageLicence().isEmpty()) {
            currentSaint.setImageLicence(saintUpdateRequest.imageLicence());
            updated = true;
        }

        if(updated) {
            saintRepository.save(currentSaint);
        }
        return SaintDtoMapper.toSaintDto(currentSaint);
    }

    public void deleteSaint(UUID saintId) {
        Saint saint = serviceHelper.getSaintById(saintId);
        saintRepository.deleteById(saint.getId());
    }

    public Map<String, List<String>> getSaintsByMonth(int year, int month) {
        List<Saint> saints = saintRepository.findAll();

        return saints.stream()
                .filter(saint -> saint.getFeastDay() != null &&
                        saint.getFeastDay().getMonthValue() == month)
                .collect(Collectors.groupingBy(
                        saint -> saint.getFeastDay().toString(),
                        Collectors.mapping(Saint::getName, Collectors.toList())
                ));
    }

    public List<SaintDto> getAllSaintsByFeastCode(String feastCode) {
        MonthDay feastDay = MonthDay.parse("--" + feastCode);
        List<Saint> saints = saintRepository.findAllByFeastDay(feastDay);

        if (saints.isEmpty()) {
            return Collections.emptyList();
        }

        return saints.stream()
                .map(SaintDtoMapper::toSaintDto)
                .toList();
    }

    public List<SaintDto> getAllSaintsByFeastDay() {
        MonthDay today = MonthDay.now();
        List<Saint> saints = saintRepository.findAllByFeastDay(today);

        if(saints.isEmpty()) {
            return Collections.emptyList();
        }

        return saints.stream()
                .map(SaintDtoMapper::toSaintDto)
                .toList();
    }

    public Map<String, List<String>> getAllFeastDaysMapped() {
        List<Saint> saints = saintRepository.findAll();
        Map<String, List<String>> feastMap = new HashMap<>();

        for(Saint saint : saints) {
            if(saint.getFeastDay() == null) {
                continue;
            }
            String feastCode = String.format("%02d-%02d", saint.getFeastDay().getMonthValue(), saint.getFeastDay().getDayOfMonth());
            feastMap.computeIfAbsent(feastCode, k -> new ArrayList<>()).add(saint.getName());
        }
        return feastMap;
    }
}
