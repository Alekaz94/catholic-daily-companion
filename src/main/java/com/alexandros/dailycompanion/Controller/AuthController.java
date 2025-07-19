package com.alexandros.dailycompanion.Controller;

import com.alexandros.dailycompanion.DTO.*;
import com.alexandros.dailycompanion.Security.JwtUtil;
import com.alexandros.dailycompanion.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignupResponse> signUpUser(@Valid @RequestBody UserRequest userRequest) {
        UserDto user = userService.signUp(userRequest);
        String token = jwtUtil.generateToken(user.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(user, token));
    }
}
