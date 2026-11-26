package com.revature.lms.identity.application.service;

import com.revature.lms.identity.application.dto.AuthenticationResponse;
import com.revature.lms.identity.application.dto.LoginRequest;
import com.revature.lms.identity.domain.enums.RoleName;
import com.revature.lms.identity.domain.entity.User;
import com.revature.lms.identity.exception.AuthenticationFailedException;
import com.revature.lms.identity.repository.UserRepository;
import com.revature.lms.identity.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class AuthenticationService {
    
    private static final String FAILURE_MESSAGE =
            "Invalid email or password, or account temporarily unavailable.";

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserRoleService userRoleService;
    private final JwtService jwtService;

    private final int maxFailedAttempts;
    private final Duration lockoutDuration;
    private final String dummyPasswordHash;

    public AuthenticationService(
        UserRepository userRepository,
        PasswordService passwordService,
        UserRoleService userRoleService,
            JwtService jwtService,
            @Value("${app.security.login.max-failed-attempts:5}")
            int maxFailedAttempts,
            @Value("${app.security.login.lockout-duration:PT15M}")
            Duration lockoutDuration
    ) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.userRoleService = userRoleService;
        this.jwtService = jwtService;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutDuration = lockoutDuration;

        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException(
                    "Maximum failed attempts must be at least 1."
            );
        }

        if (lockoutDuration.isZero() || lockoutDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "Lockout duration must be greater than zero."
            );
        }

        // A real BCrypt hash used to perform dummy verification
        // when an email address does not identify an existing user.
        this.dummyPasswordHash =
                passwordService.hashPassword("internal-dummy-password");
    }

    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public AuthenticationResponse authenticate(LoginRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);
        
        User user = userRepository
                .findByNormalizedEmail(normalizedEmail)
                .orElse(null);

        if (user == null) {
            performDummyVerification(request.password());
            throw authenticationFailed();
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            performDummyVerification(request.password());
            throw authenticationFailed();
        }

        Instant now = Instant.now();

        if (user.getLockedUntil() != null) {
            
            if (user.getLockedUntil().isAfter(now)) {
                performDummyVerification(request.password());
                throw authenticationFailed();
            }

            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        if (!passwordService.verifyPassword(
                request.password(),
                user.getPasswordHash()
        )) {
            recordFailedAttempt(user, now);
            throw authenticationFailed();
        }

        // Successful login resets the security counters.
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);

        userRepository.save(user);

        RoleName role = userRoleService.getRoleForUser(
                user.getUserId()
        );

        String token = jwtService.generateToken(
                user.getUserId(),
                user.getEmail(),
                role
        );

        return new AuthenticationResponse(
                user.getUserId(),
                user.getEmail(),
                role,
                token,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }

    private void recordFailedAttempt(User user, Instant now) {

        int attempts = user.getFailedLoginAttempts() == null
                ? 0
                : user.getFailedLoginAttempts();

        attempts++;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(now.plus(lockoutDuration));
        }

        userRepository.save(user);
    }

    private void performDummyVerification(String rawPassword) {
        passwordService.verifyPassword(
                rawPassword,
                dummyPasswordHash
        );
    }

    private AuthenticationFailedException authenticationFailed() {
        return new AuthenticationFailedException(FAILURE_MESSAGE);
    }


}
