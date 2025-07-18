package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.SaintDto;
import com.alexandros.dailycompanion.DTO.SaintRequest;
import com.alexandros.dailycompanion.Mapper.SaintDtoMapper;
import com.alexandros.dailycompanion.Model.Saint;
import com.alexandros.dailycompanion.Repository.SaintRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public List<SaintDto> getAllSaints() {
        List<Saint> saints = saintRepository.findAll();
        return SaintDtoMapper.toSaintDto(saints);
    }

    public SaintDto getSaint(UUID saintId) {
        Saint saint = getSaintById(saintId);
        return SaintDtoMapper.toSaintDto(saint);
    }

    public SaintDto createSaint(@Valid SaintRequest saintRequest) {
        MonthDay feastMonthDay = MonthDay.from(saintRequest.feastDay());
        LocalDate feastDay = feastMonthDay.atYear(2000);

        Saint saint = new Saint();
        saint.setName(saintRequest.name());
        saint.setBirthYear(saint.getBirthYear());
        saint.setDeathYear(saintRequest.deathYear());
        saint.setFeastDay(feastDay);
        saint.setBiography(saintRequest.biography());
        saint.setPatronage(saintRequest.patronage());
        saint.setCanonizationYear(saint.getCanonizationYear());
        saint.setImageUrl(saintRequest.imageUrl());
        saintRepository.save(saint);
        return SaintDtoMapper.toSaintDto(saint);
    }

    private Saint getSaintById(UUID id) {
        return saintRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Could not find saint with id: %s", id)));
    }
}
