import type {
    LoginRequest,
    RegisterUserRequest,
} from "@/types/auth";

export type ValidationErrors = Record<string, string>;

export function validateLogin(
    values: LoginRequest,
): ValidationErrors {
    const errors: ValidationErrors = {};

    if (!values.email.trim()) {
        errors.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
        errors.email = "Enter a valid email address.";
    } else if (values.email.length > 155) {
        errors.email = "Email cannot exceed 155 characters."
    }

    if (!values.password) {
        errors.password = "Password is required."
    }

    return errors
}

export function validateRegistration(
    values: RegisterUserRequest & {confirmPassword: string},
): ValidationErrors {
    const errors: ValidationErrors = {};

    if (!values.firstName.trim()) {
        errors.firstName = "First name is required.";
    } else if (values.firstName.length > 40) {
        errors.firstName = "First name cannot exceed 40 characters.";
    }

    if (!values.lastName.trim()) {
        errors.lastName = "Last name is required.";
    } else if (values.lastName.length > 40) {
        errors.lastName = "Last name cannot exceed 40 characters.";
    }

    if (!values.email.trim()) {
        errors.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
        errors.email = "Enter a valid email address.";
    } else if (values.email.length > 155) {
        errors.email = "Email cannot exceed 155 characters.";
    }

    if (values.password.length < 12 || values.password.length > 72) {
        errors.password = "Password must contain between 12 and 72 characters.";
    }

    if (values.confirmPassword !== values.password) {
        errors.confirmPassword = "Passwords do not match.";
    }

    if (values.phone && values.phone.length > 15) {
        errors.phone = "Phone cannot exceed 15 characters.";
    }

    return errors
}