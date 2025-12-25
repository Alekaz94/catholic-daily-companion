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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * REST controller responsible for authentication and authorization operations.
 * <p>
 * Provides endpoints for:
 * <ul>
 *     <li>User login</li>
 *     <li>User registration</li>
 *     <li>JWT access token refresh</li>
 * </ul>
 * <p>
 * This controller issues short-lived JWT access tokens and manages
 * long-lived refresh tokens to ensure secure session handling.
 */
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

    /**
     * Authenticates a user using provided credentials.
     * <p>
     * On successful authentication, returns a JWT access token
     * along with relevant user information.
     *
     * @param loginRequest authentication request containing user credentials
     * @param request      HTTP servlet request used to extract client metadata
     * @return login response with access token or {@code 401 Unauthorized} on failure
     */
    /*@PostMapping("/login")
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
    }*/

    /**
     * Registers a new user and creates an authenticated session.
     * <p>
     * Upon successful registration, an access token and refresh token
     * are issued immediately.
     *
     * @param userRequest user registration details
     * @param request     HTTP servlet request used to extract client metadata
     * @return newly created user details with authentication tokens
     */
    /*@PostMapping("/sign-up")
    public ResponseEntity<SignupResponse> signUpUser(@Valid @RequestBody UserRequest userRequest, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        User user = userService.signUp(userRequest, ipAddress);
        logger.info("User signed up | userId={} | ip={}", user.getId(), ipAddress);
        UserDto userDto = UserDtoMapper.toUserDto(user);

        String token = jwtUtil.generateToken(userDto);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(userDto, token, refreshToken.getToken()));
    }*/

    /**
     * Refreshes an expired JWT access token using a valid refresh token.
     *
     * @param request             refresh token request
     * @param httpServletRequest  HTTP servlet request used to extract client metadata
     * @return new access token and refresh token pair, or {@code 401 Unauthorized}
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
        String refreshToken = request.refreshToken();
        String ipAdress = serviceHelper.getClientIp(httpServletRequest);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Refresh token required"
            );
        }

        RefreshToken existing = refreshTokenService.findValidToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Refresh token expired"
                ));
        User user = serviceHelper.getUserByEmail(existing.getEmail());

        String newAccessToken = jwtUtil.generateToken(UserDtoMapper.toUserDto(user));
        RefreshToken newRefreshToken =
                refreshTokenService.rotateRefreshToken(existing);

        logger.info("New access token issued | userId: {} | ip: {}", user.getId(), ipAdress);

        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
    }

    /**
     * Logs out the user by deleting the provided refresh token.
     *
     * @param request             refresh token request
     * @param httpServletRequest  HTTP servlet request used to extract client metadata
     * @return a response indicating that logout was successful
     */
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
        String ipAdress = serviceHelper.getClientIp(httpServletRequest);
        refreshTokenService.deleteByToken(request.refreshToken());

        logger.info("Logged out | ip: {}", ipAdress);
        return ResponseEntity.ok().body("Logout successful");
    }
}
