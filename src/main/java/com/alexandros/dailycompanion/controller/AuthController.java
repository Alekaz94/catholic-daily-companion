/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.*;
import com.alexandros.dailycompanion.mapper.UserDtoMapper;
import com.alexandros.dailycompanion.model.RefreshToken;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.service.RefreshTokenService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import com.alexandros.dailycompanion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final ServiceHelper serviceHelper;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, RefreshTokenService refreshTokenService, ServiceHelper serviceHelper) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.serviceHelper = serviceHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        try {
            LoginResponse loginResponse = userService.login(loginRequest);
            logger.info("User logged in | userId={} | ip={}", loginResponse.user().id(), ipAddress);
            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt | ip={}", ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<SignupResponse> signUpUser(@Valid @RequestBody UserRequest userRequest, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        User user = userService.signUp(userRequest, ipAddress);
        logger.info("User signed up | userId={} | ip={}", user.getId(), ipAddress);
        UserDto userDto = UserDtoMapper.toUserDto(user);

        String token = jwtUtil.generateToken(userDto);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(userDto, token, refreshToken.getToken()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
        String ipAddress = serviceHelper.getClientIp(httpServletRequest);
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(oldToken -> {
                    try {
                        refreshTokenService.verifyExpiration(oldToken);
                        logger.info("Refresh token valid | userId={} | ip={}", oldToken.getUser().getId(), ipAddress);

                        User user = oldToken.getUser();
                        UserDto userDto = UserDtoMapper.toUserDto(user);

                        refreshTokenService.deleteRefreshToken(oldToken);

                        String newAccessToken = jwtUtil.generateToken(userDto);
                        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

                        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                    } catch (RuntimeException e) {
                        logger.warn("Expired refresh token used | userId={} | ip={}", oldToken.getUser().getId(), ipAddress);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired refresh token. Please login again.");
                    }
                })
                .orElseGet(() -> {
                    logger.warn("Invalid refresh token attempt | ip={}", ipAddress);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token.");
                });
    }
}
