package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.LoginResponse;
import com.alexandros.dailycompanion.service.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuthException;
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

    private final FirebaseAuthService firebaseAuthService;

    public FirebaseAuthController(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<LoginResponse> firebaseLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");

        if(idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            LoginResponse response = firebaseAuthService.verifyFirebaseTokenAndLogin(idToken);
            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
