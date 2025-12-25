/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class responsible for initializing Firebase services.
 * <p>
 * This configuration reads Firebase service account credentials from
 * environment variables and initializes the Firebase SDK at application
 * startup. It enables Firebase-based authentication (e.g. Google Sign-In)
 * and other Firebase features used by the application.
 */
@Configuration
public class FirebaseConfig {

    private final Environment env;

    public FirebaseConfig(Environment env) {
        this.env = env;
    }

    /**
     * Initializes the Firebase application using service account credentials.
     * <p>
     * The credentials are expected to be provided via the
     * {@code FIREBASE_CREDENTIALS} environment variable in JSON format.
     * Line breaks are restored at runtime to support secure deployment.
     *
     * @throws IOException if credential parsing fails
     * @throws IllegalStateException if credentials are missing or invalid
     */
    @PostConstruct
    public void initFirebase() throws IOException {
        String firebaseCredentials = env.getProperty("FIREBASE_CREDENTIALS");

        if(firebaseCredentials == null || firebaseCredentials.isBlank()) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS environment variable is not set");
        }
        firebaseCredentials = firebaseCredentials.replace("\\n", "\n");

        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));

        FirebaseOptions options = FirebaseOptions
                .builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if(FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
