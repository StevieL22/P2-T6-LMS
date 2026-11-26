
package com.revature.lms.identity.security;

import com.revature.lms.identity.domain.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LmsJwtAuthenticationConverterTest {

    private LmsJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LmsJwtAuthenticationConverter();
    }

    @Test
    void shouldConvertValidLearnerJwt() {
        Jwt jwt = createJwt("42", "learner@example.com", "LEARNER");

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        LmsUserPrincipal principal =
                assertInstanceOf(
                        LmsUserPrincipal.class,
                        authentication.getPrincipal()
                );

        assertEquals(42L, principal.userId());
        assertEquals("learner@example.com", principal.email());
        assertEquals(RoleName.LEARNER, principal.role());
        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_LEARNER")
        ));
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void shouldConvertAdminRole() {
        Jwt jwt = createJwt("1", "admin@example.com", "ADMIN");

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        LmsUserPrincipal principal =
                (LmsUserPrincipal) authentication.getPrincipal();

        assertEquals(RoleName.ADMIN, principal.role());
        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ));
    }

    @Test
    void shouldConvertTrainerRole() {
        Jwt jwt = createJwt("2", "trainer@example.com", "TRAINER");

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        LmsUserPrincipal principal =
                (LmsUserPrincipal) authentication.getPrincipal();

        assertEquals(RoleName.TRAINER, principal.role());
        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_TRAINER")
        ));
    }

    @Test
    void shouldRejectInvalidRole() {
        Jwt jwt = createJwt("42", "user@example.com", "SUPERUSER");

        assertThrows(JwtException.class, () -> converter.convert(jwt));
    }

    @Test
    void shouldRejectInvalidUserId() {
        Jwt jwt = createJwt("invalid-id", "user@example.com", "LEARNER");

        assertThrows(JwtException.class, () -> converter.convert(jwt));
    }

    @Test
    void shouldRejectMissingEmail() {
        Jwt jwt = createJwt("42", null, "LEARNER");

        assertThrows(JwtException.class, () -> converter.convert(jwt));
    }

    private Jwt createJwt(String subject, String email, String role) {
        Instant now = Instant.now();

        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("role", role)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900));

        if (email != null) {
            builder.claim("email", email);
        }

        return builder.build();
    }
}
