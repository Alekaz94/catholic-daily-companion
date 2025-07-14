package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.UserDto;
import com.alexandros.dailycompanion.DTO.UserRequest;
import com.alexandros.dailycompanion.Mapper.UserDtoMapper;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
        UserDto user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDto user = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
