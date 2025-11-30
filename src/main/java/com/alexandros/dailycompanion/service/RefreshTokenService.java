/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.service;

import com.alexandros.dailycompanion.model.RefreshToken;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.RefreshTokenRepository;
import com.alexandros.dailycompanion.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refreshExpiration}")
    private Long refreshExpirationSeconds;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceHelper serviceHelper;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        user = userRepository.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found!"));

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);

        RefreshToken refreshToken = existingToken.orElse(new RefreshToken());
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshExpirationSeconds));

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        logger.info("Created refresh token for userId={}", user.getId());
        return saved;
    }

    public void verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().isBefore(Instant.now())) {
            logger.warn("Expired refresh token detected for userId={}", token.getUser().getId());
            throw new RuntimeException("Refresh token was expired. Please login again.");
        }
    }

    public int deleteAllByUserId(UUID userId) {
        logger.info("Deleted all refresh tokens for userId={}", userId);
        return refreshTokenRepository.deleteByUserId(userId);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        logger.info("Deleted refresh token for userId={}", refreshToken.getUser().getId());
        refreshTokenRepository.delete(refreshToken);
    }
}
