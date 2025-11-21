/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.dto.LoginResponse;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class FirebaseAuthService {
    private final static Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public FirebaseAuthService(UserRepository userRepository, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public User createOrGetUser(String email, String firstName) {
        try {
            return userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        LocalDate now = LocalDate.now();
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setFirstName(firstName != null ? firstName : "User");
                        newUser.setLastName("");
                        newUser.setRole(Roles.USER);
                        newUser.setCreatedAt(now);
                        newUser.setUpdatedAt(now);
                        return userRepository.save(newUser);
                    });
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User should exist after insert."));
        }
    }

    public LoginResponse verifyFirebaseTokenAndLogin(String idToken, String ipAddress) throws FirebaseAuthException {
        FirebaseToken decodedFirebaseToken = firebaseAuth.verifyIdToken(idToken);
        String email = decodedFirebaseToken.getEmail().toLowerCase();
        String name = decodedFirebaseToken.getName() != null ? decodedFirebaseToken.getName() : "User";

        logger.info("Firebase token verified | ip={}", ipAddress);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = createOrGetUser(email, name);

       /* if(optionalUser.isPresent()) {
            user = optionalUser.get();
            logger.info("Firebase login existing user | userId={} | ip={}", user.getId(), ipAddress);
        } else {
            logger.info("Creating new Firebase user | ip={}", ipAddress);
            user = new User();
            user.setEmail(email);
            user.setFirstName(decodedFirebaseToken.getName() != null ? decodedFirebaseToken.getName() : "User");
            user.setRole(Roles.USER);
            user.setCreatedAt(LocalDate.now());
            user.setUpdatedAt(LocalDate.now());
            try {
                user = userRepository.save(user);
                logger.info("Firebase user created | userId={} | ip={}", user.getId(), ipAddress);
            } catch (DataIntegrityViolationException e) {
                logger.warn("Race condition: user already created | userId={} | ip={}", user.getId(), ipAddress);
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User was just created but cant be found."));
            }
        }*/
        logger.info("Firebase login user | userId={} | ip={}", user.getId(), ipAddress);

        UserDto userDto = UserDtoMapper.toUserDto(user);
        String token = jwtUtil.generateToken(userDto);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new LoginResponse(userDto, token, refreshToken.getToken());
    }
}
