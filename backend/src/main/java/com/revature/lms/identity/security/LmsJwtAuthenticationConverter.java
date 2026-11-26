package com.revature.lms.identity.security;

import com.revature.lms.identity.domain.enums.RoleName;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LmsJwtAuthenticationConverter implements
        Converter<Jwt, AbstractAuthenticationToken> {
    
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String roleClaim = jwt.getClaimAsString("role");

        if (subject == null || email == null || email.isBlank()
                || roleClaim == null || roleClaim.isBlank()) {
            throw new JwtException("JWT is missing required user claims.");
        }

        Long userId;

        try {
            userId = Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new JwtException("JWT subject must be a valid user ID.");
        }

        RoleName role;

        try {
            role = RoleName.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            throw new JwtException("JWT contains an invalid user role.");
        }

        LmsUserPrincipal principal = new LmsUserPrincipal(
                userId,
                email,
                role
        );

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + role.name());
        
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                jwt,
                List.of(authority)
        );
    }
}
