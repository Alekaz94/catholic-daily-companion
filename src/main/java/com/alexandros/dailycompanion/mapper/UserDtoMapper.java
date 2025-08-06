package com.alexandros.dailycompanion.mapper;

import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.model.User;

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
                user.getRole()
        );
    }

    public static List<UserDto> toUserDto(List<User> users) {
        return users.stream().map(UserDtoMapper::toUserDto).toList();
    }
}
