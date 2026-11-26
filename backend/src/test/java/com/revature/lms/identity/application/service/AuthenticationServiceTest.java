package com.revature.lms.identity.application.service;

import com.revature.lms.identity.application.dto.AuthenticationResponse;
import com.revature.lms.identity.application.dto.LoginRequest;
import com.revature.lms.identity.config.JwtConfiguration;
import com.revature.lms.identity.config.PasswordConfiguration;
import com.revature.lms.identity.domain.entity.AppRole;
import com.revature.lms.identity.domain.entity.User;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.exception.AuthenticationFailedException;
import com.revature.lms.identity.repository.AppRoleRepository;
import com.revature.lms.identity.repository.UserRepository;
import com.revature.lms.identity.repository.UserRoleRepository;
import com.revature.lms.identity.security.JwtService;
import com.revature.lms.identity.domain.entity.UserRole;
import com.revature.lms.identity.application.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lms-auth-service-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monolith",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=monolith",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never",
        "app.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.security.jwt.issuer=https://lms.revature.com",
        "app.security.jwt.expiration=PT15M",
        "app.security.login.max-failed-attempts=5",
        "app.security.login.lockout-duration=PT15M"
})
@Import({
        AuthenticationService.class,
        PasswordService.class,
        PasswordConfiguration.class,
        UserRoleService.class,
        JwtService.class,
        JwtConfiguration.class
})
class AuthenticationServiceTest {

    private static final String RAW_PASSWORD = "StrongPassword123!";

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void setUp() {
        appRoleRepository.saveAndFlush(
                new AppRole(RoleName.LEARNER)
        );
    }

    private User createUser(String email) {
        return createUser(email, true);
    }

    private User createUser(String email, boolean active) {
        User user = new User(
                email,
                passwordService.hashPassword(RAW_PASSWORD),
                "John",
                "Doe",
                null
        );

        user.setActive(active);

        User savedUser = userRepository.saveAndFlush(user);

        userRoleService.assignRole(
                savedUser.getUserId(),
                RoleName.LEARNER
        );

        return savedUser;
    }

    private LoginRequest validLogin(String email) {
        return new LoginRequest(email, RAW_PASSWORD);
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        User user = createUser("john@example.com");

        AuthenticationResponse response =
                authenticationService.authenticate(
                        validLogin("john@example.com")
                );

        assertEquals(user.getUserId(), response.userId());
        assertEquals("john@example.com", response.email());
        assertEquals(RoleName.LEARNER, response.role());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
        assertNotNull(response.accessToken());
        assertTrue(jwtService.isTokenValid(response.accessToken()));
    }

    @Test
    void shouldNormalizeEmailDuringLogin() {
        createUser("john@example.com");

        AuthenticationResponse response =
                authenticationService.authenticate(
                        validLogin("  JOHN@EXAMPLE.COM  ")
                );

        assertEquals("john@example.com", response.email());
    }

    @Test
    void shouldUpdateLastLoginTimestamp() {
        User user = createUser("login-time@example.com");

        authenticationService.authenticate(
                validLogin("login-time@example.com")
        );

        User updatedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertNotNull(updatedUser.getLastLoginAt());
        assertFalse(
                updatedUser.getLastLoginAt().isAfter(Instant.now())
        );
    }

    @Test
    void shouldResetFailedAttemptsAfterSuccessfulLogin() {
        User user = createUser("reset@example.com");

        user.setFailedLoginAttempts(3);
        userRepository.saveAndFlush(user);

        authenticationService.authenticate(
                validLogin("reset@example.com")
        );

        User updatedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertEquals(0, updatedUser.getFailedLoginAttempts());
        assertNull(updatedUser.getLockedUntil());
    }

    @Test
    void shouldRejectIncorrectPasswordAndIncrementAttempts() {
        User user = createUser("wrong-password@example.com");

        assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationService.authenticate(
                        new LoginRequest(
                                "wrong-password@example.com",
                                "IncorrectPassword123!"
                        )
                )
        );

        User updatedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertEquals(1, updatedUser.getFailedLoginAttempts());
        assertNull(updatedUser.getLockedUntil());
    }

    @Test
    void shouldLockAccountAfterFiveFailedAttempts() {
        User user = createUser("lock@example.com");

        for (int i = 0; i < 5; i++) {
            assertThrows(
                    AuthenticationFailedException.class,
                    () -> authenticationService.authenticate(
                            new LoginRequest(
                                    "lock@example.com",
                                    "IncorrectPassword123!"
                            )
                    )
            );
        }

        User updatedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertEquals(5, updatedUser.getFailedLoginAttempts());
        assertNotNull(updatedUser.getLockedUntil());
        assertTrue(updatedUser.getLockedUntil().isAfter(Instant.now()));
    }

    @Test
    void shouldRejectLoginWhileAccountIsLocked() {
        User user = createUser("locked@example.com");

        user.setFailedLoginAttempts(5);
        user.setLockedUntil(Instant.now().plusSeconds(900));
        userRepository.saveAndFlush(user);

        assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationService.authenticate(
                        validLogin("locked@example.com")
                )
        );
    }

    @Test
    void shouldAllowLoginAfterLockoutExpires() {
        User user = createUser("expired-lock@example.com");

        user.setFailedLoginAttempts(5);
        user.setLockedUntil(Instant.now().minusSeconds(1));
        userRepository.saveAndFlush(user);

        AuthenticationResponse response =
                authenticationService.authenticate(
                        validLogin("expired-lock@example.com")
                );

        User updatedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertNotNull(response.accessToken());
        assertEquals(0, updatedUser.getFailedLoginAttempts());
        assertNull(updatedUser.getLockedUntil());
    }

    @Test
    void shouldRejectLoginForInactiveAccount() {
        User user = createUser("inactive@example.com", false);

        assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationService.authenticate(
                        validLogin("inactive@example.com")
                )
        );

        User unchangedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertNull(unchangedUser.getLastLoginAt());
    }

    @Test
    void shouldRejectUnknownEmail() {
        assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationService.authenticate(
                        validLogin("unknown@example.com")
                )
        );
    }

    @Test
    void shouldNotIncreaseAttemptsForUnknownEmail() {
        assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationService.authenticate(
                        validLogin("unknown@example.com")
                )
        );

        assertFalse(
                userRepository.existsByNormalizedEmail("unknown@example.com")
        );
    }

    @Test
    void shouldNotIncreaseAttemptsWhenAccountIsAlreadyLocked() {
        User user = createUser("already-locked@example.com");

        user.setFailedLoginAttempts(5);
        user.setLockedUntil(Instant.now().plusSeconds(900));
        userRepository.saveAndFlush(user);

        assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationService.authenticate(
                        validLogin("already-locked@example.com")
                )
        );

        User updatedUser = userRepository.findById(user.getUserId())
                .orElseThrow();

        assertEquals(5, updatedUser.getFailedLoginAttempts());
    }
}
