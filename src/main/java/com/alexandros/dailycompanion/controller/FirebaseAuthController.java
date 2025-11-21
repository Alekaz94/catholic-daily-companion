/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.LoginResponse;
import com.alexandros.dailycompanion.service.FirebaseAuthService;
import com.alexandros.dailycompanion.service.ServiceHelper;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/firebase-auth")
public class FirebaseAuthController {
    private final static Logger logger = LoggerFactory.getLogger(FirebaseAuthController.class);
    private final FirebaseAuthService firebaseAuthService;
    private final ServiceHelper serviceHelper;

    @Autowired
    public FirebaseAuthController(FirebaseAuthService firebaseAuthService, ServiceHelper serviceHelper) {
        this.firebaseAuthService = firebaseAuthService;
        this.serviceHelper = serviceHelper;
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<LoginResponse> firebaseLogin(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String ipAddress = serviceHelper.getClientIp(request);
        String idToken = body.get("idToken");

        if(idToken == null || idToken.isBlank()) {
            logger.warn("Firebase login attempt with missing idToken | ip={}", ipAddress);
            return ResponseEntity.badRequest().build();
        }

        try {
            LoginResponse response = firebaseAuthService.verifyFirebaseTokenAndLogin(idToken, ipAddress);
            logger.info("Firebase login success | userId={} | ip={}", response.user().id(), ipAddress);
            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            logger.warn("Firebase login failed | ip={} | reason={}", ipAddress, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
