package com.revature.lms.identity.security;

import com.revature.lms.identity.domain.enums.RoleName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final Duration expiration;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.expiration}") Duration expiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.issuer = issuer;
        this.expiration = expiration;

        if (expiration.isZero() || expiration.isNegative()) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero."
            );
        }
    }

    public String generateToken(
            Long userId,
            String email,
            RoleName role
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .build();
        
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();
        
        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
    
    public Jwt parseToken(String token) {
        return jwtDecoder.decode(token);
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);

            return jwt.getSubject() != null
                    && jwt.getClaimAsString("email") != null
                    && jwt.getClaimAsString("role") != null
                    && issuer.equals(jwt.getIssuer().toString());
        } catch (Exception exception) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
