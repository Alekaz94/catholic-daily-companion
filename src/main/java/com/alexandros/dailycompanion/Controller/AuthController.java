package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.LoginRequest;
import com.alexandros.dailycompanion.DTO.UserDto;
import com.alexandros.dailycompanion.DTO.UserRequest;
import com.alexandros.dailycompanion.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUpUser(@Valid @RequestBody UserRequest userRequest) {
        UserDto user = userService.signUp(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
