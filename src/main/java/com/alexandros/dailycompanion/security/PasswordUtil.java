package com.alexandros.dailycompanion.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String password) {
        if(password == null || password.isEmpty()) {
            throw new IllegalArgumentException("No valid password given!");
        }
        return encoder.encode(password);
    }

    public static boolean validateHashedPassword(String password, String hashedPassword) {
        return encoder.matches(password, hashedPassword);
    }
}
