package com.alexandros.dailycompanion.Mapper;

import com.alexandros.dailycompanion.DTO.UserDto;
import com.alexandros.dailycompanion.Model.User;

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
