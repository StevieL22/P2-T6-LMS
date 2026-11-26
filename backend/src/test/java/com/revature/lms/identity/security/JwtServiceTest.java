package com.revature.lms.identity.security;

import com.revature.lms.identity.config.JwtConfiguration;
import com.revature.lms.identity.domain.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes()
        );

        JwtConfiguration configuration = new JwtConfiguration();

        SecretKey secretKey = configuration.jwtSecretKey(secret);
        JwtEncoder encoder = configuration.jwtEncoder(secretKey);
        JwtDecoder decoder = configuration.jwtDecoder(
                secretKey,
                "https://lms.revature.com"
        );

        jwtService = new JwtService(
                encoder,
                decoder,
                "https://lms.revature.com",
                java.time.Duration.ofMinutes(15)
        );
    }

    @Test
    void shouldGenerateValidJwt() {
        String token = jwtService.generateToken(
                1L,
                "john@example.com",
                RoleName.LEARNER
        );

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldIncludeUserClaims() {
        String token = jwtService.generateToken(
                42L,
                "trainer@example.com",
                RoleName.TRAINER
        );

        var decoded = jwtService.parseToken(token);

        assertEquals("42", decoded.getSubject());
        assertEquals("trainer@example.com",
                decoded.getClaimAsString("email"));
        assertEquals("TRAINER",
                decoded.getClaimAsString("role"));
        assertEquals("https://lms.revature.com", decoded.getIssuer().toString());
    }

    @Test
    void shouldRejectNullToken() {
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void shouldRejectBlankToken() {
        assertFalse(jwtService.isTokenValid(" "));
    }

    @Test
    void shouldRejectMalformedToken() {
        assertFalse(jwtService.isTokenValid("not.a.jwt"));
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        String token = jwtService.generateToken(
                1L,
                "john@example.com",
                RoleName.LEARNER
        );

        String tamperedToken = token.substring(0, token.length() - 2)
                + "xx";

        assertFalse(jwtService.isTokenValid(tamperedToken));
    }

    @Test
    void shouldReturnExpirationInSeconds() {
        assertEquals(900, jwtService.getExpirationSeconds());
    }
}
