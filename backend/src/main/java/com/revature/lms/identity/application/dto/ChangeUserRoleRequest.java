package com.revature.lms.identity.application.dto;

import com.revature.lms.identity.domain.enums.RoleName;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
    @NotNull(message = "Role is required.")
    RoleName role
) {
}
