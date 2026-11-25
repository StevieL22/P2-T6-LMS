package com.revature.lms.identity.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(
                new BCryptPasswordEncoder(4)
        );
    }

    @Test
    void shouldHashPassword() {
        String rawPassword = "StrongPassword123!";
        String hash = passwordService.hashPassword(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
    }

    @Test
    void shouldVerifyCorrectPassword() {
        String rawPassword = "StrongPassword123!";
        String hash = passwordService.hashPassword(rawPassword);

        assertTrue(
                passwordService.verifyPassword(rawPassword, hash)
        );
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String hash = passwordService.hashPassword(
                "StrongPassword123!"
        );

        assertFalse(
                passwordService.verifyPassword("WrongPassword123!", hash)
        );
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "StrongPassword123!";

        String firstHash = passwordService.hashPassword(rawPassword);
        String secondHash = passwordService.hashPassword(rawPassword);

        assertNotEquals(firstHash, secondHash);

        assertTrue(passwordService.verifyPassword(rawPassword, firstHash));
        assertTrue(passwordService.verifyPassword(rawPassword, secondHash));
    }

    @Test
    void shouldRejectNullRawPasswordDuringVerification() {
        assertFalse(
                passwordService.verifyPassword(null, "some-password-hash")
        );
    }

    @Test
    void shouldRejectNullHashDuringVerification() {
        assertFalse(
            passwordService.verifyPassword("SomePassword123!", null)
        );
    }

    @Test
    void shouldRejectBlankPasswordWhenHashing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.hashPassword(" ")
        );
    }

    @Test
    void shouldRejectNullPasswordWhenHashing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.hashPassword(null)
        );
    }
}
