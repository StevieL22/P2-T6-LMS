package com.revature.lms.identity.repository;

import com.revature.lms.identity.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:lms-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monolith",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=monolith",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never"
})

class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {
        return new User(
            email,
            "hashed-password",
            "John",
            "Doe",
            "1234567890"
        );
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        User savedUser = userRepository.saveAndFlush(
            createUser("john@example.com")
        );

        Optional<User> result = userRepository.findById(
            savedUser.getUserId()
        );

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void shouldGenerateUserIdWhenSaved() {
        User savedUser = userRepository.saveAndFlush(
            createUser("generated@example.com")
        );

        assertNotNull(savedUser.getUserId());
        assertTrue(savedUser.getUserId() > 0);
    }

    @Test
    void shouldFindNormalizedEmailRegardlessOfCaseAndWhitespace() {
        userRepository.saveAndFlush(
            createUser("john@example.com")
        );

        Optional<User> result =
                userRepository.findByNormalizedEmail("  JOHN@EXAMPLE.COM  ");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenNormalizedEmailDoesNotExist() {
        Optional<User> result =
            userRepository.findByNormalizedEmail("missing@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenNormalizedEmailExists() {
        userRepository.saveAndFlush(
            createUser("john@example.com")
        );

        assertTrue(
            userRepository.existsByNormalizedEmail(" JOHN@EXAMPLE.COM ")
        );
    }

    @Test
    void shouldReturnFalseWhenNormalizedEmailDoesNotExist() {
        assertFalse(
            userRepository.existsByNormalizedEmail("missing@example.com")
        );
    }

    @Test
    void shouldPersistSecurityDefaults() {
        User savedUser = userRepository.saveAndFlush(
            createUser("security@example.com")
        );

        User retrievedUser = userRepository.findById(
            savedUser.getUserId()
        ).orElseThrow();

        assertTrue(retrievedUser.getActive());
        assertEquals(0, retrievedUser.getFailedLoginAttempts());
        assertNull(retrievedUser.getLockedUntil());
        assertNull(retrievedUser.getLastLoginAt());
    }
}
