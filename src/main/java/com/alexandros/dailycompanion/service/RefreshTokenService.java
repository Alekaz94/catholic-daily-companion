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
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDuration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceHelper serviceHelper;

    public RefreshToken createRefreshToken(String email) {
        User user = serviceHelper.getUserByEmail(email);
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusSeconds(refreshTokenDuration));

        logger.info("Created refresh token for userId={}", user.getId());
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findValidToken(String token) {
        Optional<RefreshToken> found = refreshTokenRepository.findByToken(token);

        if (found.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token already used"
            );
        }

        return found.filter(t -> t.getExpiryDate().isAfter(Instant.now()));
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken existing) {
        refreshTokenRepository.deleteByToken(existing.getToken());

        RefreshToken newToken = new RefreshToken();
        newToken.setUserId(existing.getUserId());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDuration));

        return refreshTokenRepository.save(newToken);
    }

    public String getUserIdFromToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getUserId().toString())
                .orElse(null);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }

    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
