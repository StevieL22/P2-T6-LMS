package com.revature.lms.identity.application.dto;

import com.revature.lms.identity.domain.enums.RoleName;

public record AuthenticationResponse(
    Long userId,
    String email,
    RoleName role,
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
