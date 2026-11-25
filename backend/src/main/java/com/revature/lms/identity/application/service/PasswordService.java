package com.revature.lms.identity.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }

        return passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(
            String rawPassword,
            String passwordHash
    ) {
        if (rawPassword == null || passwordHash == null) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
