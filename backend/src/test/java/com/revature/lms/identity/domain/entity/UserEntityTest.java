package com.revature.lms.identity.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {
    
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "test@example.com",
                "hashed-password",
                "John",
                "Doe",
                "1234567890"
        );
    }

    @Test
    void constructorShouldInitializeUserFields() {
        assertNull(user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    void constructorShouldInitializeSecurityDefaults() {
        assertTrue(user.getActive());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertNull(user.getLastLoginAt());
    }

    @Test
    void shouldUpdateUserProfileFields() {
        user.setEmail("updated@example.com");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setPhone("9876543210");

        assertEquals("updated@example.com", user.getEmail());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("9876543210", user.getPhone());
    }

    @Test
    void shouldUpdatePasswordHash() {
        user.setPasswordHash("new-password-hash");

        assertEquals("new-password-hash", user.getPasswordHash());
    }

    @Test
    void shouldUpdateAccountStatus() {
        user.setActive(false);

        assertFalse(user.getActive());
    }

    @Test
    void shouldUpdateFailedLoginAttempts() {
        user.setFailedLoginAttempts(3);

        assertEquals(3, user.getFailedLoginAttempts());
    }

    @Test
    void shouldUpdatedAccountLockTimestamp() {
        Instant lockedUntil = Instant.parse("2026-11-25T12:00:00Z");

        user.setLockedUntil(lockedUntil);

        assertEquals(lockedUntil, user.getLockedUntil());
    }

    @Test
    void shouldUpdateLastLoginTimestamp() {
        Instant lastLogin = Instant.parse("2026-11-25T10:00:00Z");

        user.setLastLoginAt(lastLogin);

        assertEquals(lastLogin, user.getLastLoginAt());
    }
}
