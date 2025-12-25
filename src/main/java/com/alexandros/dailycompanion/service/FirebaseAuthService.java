/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.LoginResponse;
import com.alexandros.dailycompanion.dto.UserCreationResult;
import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.mapper.UserDtoMapper;
import com.alexandros.dailycompanion.model.RefreshToken;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FirebaseAuthService {
    private final static Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;

    @Value("${admin.email}")
    private String adminEmail;

    public FirebaseAuthService(UserRepository userRepository, JwtUtil jwtUtil, RefreshTokenService refreshTokenService, AuditLogService auditLogService) {
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserCreationResult createOrGetUser(String email, String firstName, String ipAddress) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return new UserCreationResult(existingUser.get(), false);
        }

        UUID id = UUID.randomUUID();
        LocalDate now = LocalDate.now();
        String role = email.equalsIgnoreCase(adminEmail) ? Roles.ADMIN.name() : Roles.USER.name();

        userRepository.insertIfNotExists(
                id,
                email,
                firstName != null ? firstName : "User",
                role,
                now,
                now
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User should exist after insert"));
        boolean isNew = user.getId().equals(id);

        if(isNew) {
            auditLogService.logAction(
                    id,
                    "SIGNUP SUCCESS",
                    "User",
                    id,
                    "Firebase signup",
                    ipAddress
            );
        }

        return new UserCreationResult(user, isNew);
    }

    public LoginResponse verifyFirebaseTokenAndLogin(String idToken, String ipAddress) throws FirebaseAuthException {
        FirebaseToken decodedFirebaseToken = firebaseAuth.verifyIdToken(idToken);
        String email = decodedFirebaseToken.getEmail().toLowerCase();
        String name = decodedFirebaseToken.getName() != null ? decodedFirebaseToken.getName() : "User";

        logger.info("Firebase token verified | ip={}", ipAddress);
        UserCreationResult result = createOrGetUser(email, name, ipAddress);
        User user = result.user();
        boolean isNewUser = result.isNew();

        auditLogService.logAction(
                user.getId(),
                "LOGIN_SUCCESS",
                "User",
                user.getId(),
                "Firebase login",
                ipAddress
        );

        logger.info("Firebase login user | userId={} | ip={}", user.getId(), ipAddress);

        UserDto userDto = UserDtoMapper.toUserDto(user);
        String token = jwtUtil.generateToken(userDto);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        logger.info("Refresh token received: {}", refreshToken.getToken());

        return new LoginResponse(userDto, token, refreshToken.getToken());
    }
}
