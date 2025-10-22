package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.mapper.UserDtoMapper;
import com.alexandros.dailycompanion.model.RefreshToken;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.service.RefreshTokenService;
import com.alexandros.dailycompanion.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.login(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignupResponse> signUpUser(@Valid @RequestBody UserRequest userRequest) {
        User user = userService.signUp(userRequest);
        UserDto userDto = UserDtoMapper.toUserDto(user);

        String token = jwtUtil.generateToken(userDto);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(userDto, token, refreshToken.getToken()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(oldToken -> {
                    try {
                        refreshTokenService.verifyExpiration(oldToken);

                        User user = oldToken.getUser();
                        UserDto userDto = UserDtoMapper.toUserDto(user);

                        refreshTokenService.deleteRefreshToken(oldToken);

                        String newAccessToken = jwtUtil.generateToken(userDto);
                        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

                        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                    } catch (RuntimeException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired refresh token. Please login again.");
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token."));
    }
}
