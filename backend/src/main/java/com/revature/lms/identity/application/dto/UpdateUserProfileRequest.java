package com.revature.lms.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest (

    @NotBlank(message = "First name is required.")
    @Size(max = 40, message = "First name cannot exceed 40 characters.")
    String firstName,

    @NotBlank(message = "Last name is required.")
    @Size(max = 40, message = "Last name cannot exceed 40 characters.")
    String lastName,

    @Size(max = 15, message = "Phone cannot exceed 15 characters.")
    String phone
) {
}
