package com.alexandros.dailycompanion.Mapper;

import com.alexandros.dailycompanion.DTO.SaintDto;
import com.alexandros.dailycompanion.Model.Saint;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class SaintDtoMapper {
    public static SaintDto toSaintDto(Saint saint) {
        if (saint == null) {
            return null;
        }

        return new SaintDto(
                saint.getId(),
                saint.getName(),
                saint.getBirthYear(),
                saint.getDeathYear(),
                saint.getFeastDay(),
                saint.getBiography(),
                saint.getPatronage(),
                saint.getCanonizationYear(),
                saint.getImageUrl()
        );
    }

    public static List<SaintDto> toSaintDto(List<Saint> saints) {
        return saints.stream().map(SaintDtoMapper::toSaintDto).toList();
    }

    public static Page<SaintDto> toSaintDto(Page<Saint> saints) {
        return saints.map(SaintDtoMapper::toSaintDto);
    }
}
