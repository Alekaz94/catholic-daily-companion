package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public class UserDtoMapper {
    public static UserDto toUserDto(User user) {
        if(user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static List<UserDto> toUserDto(List<User> users) {
        return users.stream().map(UserDtoMapper::toUserDto).toList();
    }

    public static Page<UserDto> toUserDto(Page<User> users) {
        return users.map(UserDtoMapper::toUserDto);
    }
}
