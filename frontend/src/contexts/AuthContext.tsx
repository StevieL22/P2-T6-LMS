"use client";

import {
    createContext,
    useCallback,
    useContext,
    useMemo,
    useState,
} from "react";

import type React from "react";

import { setApiAccessToken } from "@/services/api";
import type {
    AuthenticationResponse,
    RoleName,
    UserProfileResponse,
} from "@/types/auth";
import { getCurrentUser } from "@/services/auth-service";

interface AuthContextValue {
    user: UserProfileResponse | null;
    accessToken: string | null;
    isAuthenticated: boolean;
    isDemo: boolean;
    isLoading: boolean;
    login: (response: AuthenticationResponse) => Promise<void>;
    demoLogin: (role: RoleName) => void;
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
    const [isDemo, setIsDemo] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const login = useCallback(async (response: AuthenticationResponse) => {
        setIsLoading(true);
        setIsDemo(false);

        setAccessToken(response.accessToken);
        setApiAccessToken(response.accessToken);

        try {
            const profile = await getCurrentUser();
            setUser(profile);
        } catch (error) {
            setUser(null);
            setAccessToken(null);
            setApiAccessToken(null);
            throw error;
        } finally {
            setIsLoading(false);
        }
    }, []);

    const demoLogin = useCallback((role: RoleName) => {
        const now = new Date().toISOString();

        const demoUser: UserProfileResponse = {
            userId: -1,
            email: `demo.${role.toLowerCase()}@team6lms.local`,
            firstName: "Demo",
            lastName:
                role.charAt(0) + role.slice(1).toLowerCase(),
            phone: null,
            active: true,
            role,
            createdAt: now,
            updatedAt: now,
        };

        setApiAccessToken(null);
        setAccessToken(null);
        setUser(demoUser);
        setIsDemo(true);
        setIsLoading(false);
    }, []);

    const logout = useCallback(() => {
        setUser(null);
        setAccessToken(null);
        setApiAccessToken(null);
        setIsDemo(false);
        setIsLoading(false);
    }, []);

    const value = useMemo(
        () => ({
            user,
            accessToken,
            isAuthenticated: Boolean(user && (accessToken || isDemo)),
            isDemo,
            isLoading,
            login,
            demoLogin,
            logout,
        }),
        [
            user,
            accessToken,
            isDemo,
            isLoading,
            login,
            demoLogin,
            logout,
        ],
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