package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.SaintDto;
import com.alexandros.dailycompanion.DTO.SaintRequest;
import com.alexandros.dailycompanion.Mapper.SaintDtoMapper;
import com.alexandros.dailycompanion.Model.Saint;
import com.alexandros.dailycompanion.Repository.SaintRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

@Service
public class SaintService {
    private final SaintRepository saintRepository;

    @Autowired
    public SaintService(SaintRepository saintRepository) {
        this.saintRepository = saintRepository;
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
        Saint saint = getSaintById(saintId);
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

    private Saint getSaintById(UUID id) {
        return saintRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find saint with id: %s", id)));
    }
}
