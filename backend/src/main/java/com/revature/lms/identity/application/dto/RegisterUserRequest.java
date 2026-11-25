package com.revature.lms.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest (

    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    @Size(max = 155, message = "Email cannot exceed 155 characters.")
    String email,

    @NotBlank(message = "Password is required.")
    @Size(
        min = 12,
        max = 72,
        message = "Password must contain between 12 and 72 characters."
    )
    String password,

    @NotBlank(message = "First name is required.")
    @Size(max = 40, message = "First name cannot exceed 40 characters.")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 40, message = "Last name cannot exceed 40 characters.")
    String lastName,

    @Size(max = 15, message = "Phone cannot exceed 15 characters.")
    String phone
) {
}
