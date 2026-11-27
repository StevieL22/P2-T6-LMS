export type RoleName = "ADMIN" | "TRAINER" | "LEARNER";

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterUserRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
}

export interface AuthenticationResponse {
    userId: number;
    email: string;
    role: RoleName;
    accessToken: string;
    tokenType: string;
    expiresIn: number;
}

export interface UserProfileResponse {
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
    phone: string | null;
    active: boolean;
    role: RoleName;
    createdAt: string;
    updatedAt: string;
}

export interface UpdateUserProfileRequest {
    firstName: string;
    lastName: string;
    phone?: string;
}
