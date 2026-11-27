
"use client";

import {
    createContext,
    useCallback,
    useContext,
    useMemo,
    useState,
} from "react";

import { setApiAccessToken } from "@/services/api";
import type {
    AuthenticationResponse,
    UserProfileResponse,
} from "@/types/auth";

interface AuthContextValue {
    user: UserProfileResponse | null;
    accessToken: string | null;
    isAuthenticated: boolean;
    login: (response: AuthenticationResponse) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    const [user, setUser] = useState<UserProfileResponse | null>(null);
    const [accessToken, setAccessToken] = useState<string | null>(null);

    const login = useCallback((response: AuthenticationResponse) => {
        setAccessToken(response.accessToken);
        setApiAccessToken(response.accessToken);

        setUser({
        userId: response.userId,
        email: response.email,
        firstName: "",
        lastName: "",
        phone: null,
        active: true,
        role: response.role,
        createdAt: "",
        updatedAt: "",
        });
    }, []);

    const logout = useCallback(() => {
        setUser(null);
        setAccessToken(null);
        setApiAccessToken(null);
    }, []);

    const value = useMemo(
    () => ({
            user,
            accessToken,
            isAuthenticated: Boolean(accessToken),
            login,
            logout,
        }),
            [user, accessToken, login, logout],
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider.");
    }

    return context;
}