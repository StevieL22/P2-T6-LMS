import api from "@/services/api";
import type {
    AuthenticationResponse,
    LoginRequest,
    RegisterUserRequest,
    UserProfileResponse,
} from "@/types/auth";

export async function login(
    request: LoginRequest,
): Promise<AuthenticationResponse> {
    const response = await api.post<AuthenticationResponse>(
        "/api/v1/auth/login",
        request,
    );

    return response.data;
}

export async function register(
    request: RegisterUserRequest,
): Promise<UserProfileResponse> {
    const response = await api.post<UserProfileResponse>(
        "/api/v1/auth/register",
        request,
    );

    return response.data;
}

export async function getCurrentUser(): Promise<UserProfileResponse> {
    const response = await api.get<UserProfileResponse>(
        "/api/v1/users/me",
    );

    return response.data;
}
