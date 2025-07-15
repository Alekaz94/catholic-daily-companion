package com.alexandros.dailycompanion.Service;

import com.alexandros.dailycompanion.DTO.UserDto;
import com.alexandros.dailycompanion.DTO.UserRequest;
import com.alexandros.dailycompanion.DTO.UserUpdateRequest;
import com.alexandros.dailycompanion.Mapper.UserDtoMapper;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserDtoMapper.toUserDto(users);
    }

    public UserDto getUser(UUID userId) {
        User user = getUserByIdOrThrow(userId);
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto createUser(@Valid UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByEmail(userRequest.email());
        if(existingUser.isPresent()) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPassword(userRequest.password());
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto updateUserPassword(UUID userId, @Valid UserUpdateRequest userUpdateRequest) {
        User user = getUserByIdOrThrow(userId);
        user.setPassword(userUpdateRequest.password());
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
        return UserDtoMapper.toUserDto(user);
    }

    public void deleteUser(UUID userId) {
        User user = getUserByIdOrThrow(userId);
        userRepository.deleteById(user.getId());
    }

    private User getUserByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Could not find user with id: %s", id)));
    }
}
