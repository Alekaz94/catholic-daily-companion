package com.alexandros.dailycompanion.Mapper;

import com.alexandros.dailycompanion.DTO.SaintDto;
import com.alexandros.dailycompanion.Model.Saint;

import java.util.List;

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
                saint.getBiography()
        );
    }

    public static List<SaintDto> toSaintDto(List<Saint> saints) {
        return saints.stream().map(SaintDtoMapper::toSaintDto).toList();
    }
}
