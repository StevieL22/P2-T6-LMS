package com.revature.lms.identity.security;

import com.revature.lms.identity.domain.enums.RoleName;

public record LmsUserPrincipal(
    Long userId,
    String email,
    RoleName role
) {
}
