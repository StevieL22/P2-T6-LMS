package com.revature.lms.identity.application.dto;

import com.revature.lms.identity.domain.enums.RoleName;

import java.time.Instant;

public record UserProfileResponse(
    Long userId,
    String email,
    String firstName,
    String lastName,
    String phone,
    Boolean active,
    RoleName role,
    Instant createdAt,
    Instant updatedAt
) {
}
