"use client";

import { useState } from "react";
import type { SyntheticEvent } from "react";
import { useRouter } from "next/navigation";

import { login as loginRequest } from "@/services/auth-service";
import { getApiErrorMessage } from "@/lib/api-error";
import { validateLogin } from "@/lib/auth-validation";
import { useAuth } from "@/contexts/AuthContext";
import type { LoginRequest, RoleName } from "@/types/auth";

interface LoginFormProps {
    onRegisterClick: () => void;
}

const DEMO_PASSWORD = "DemoPassword123!";

const DEMO_ACCOUNTS: Record<RoleName, string> = {
    ADMIN: "demo.admin@team6lms.local",
    TRAINER: "demo.trainer@team6lms.local",
    LEARNER: "demo.learner@team6lms.local",
};

function getDashboardPath(role: RoleName): string {
    switch (role) {
        case "ADMIN":
            return "/admin";
        case "TRAINER":
            return "/trainer";
        case "LEARNER":
            return "/learner";
    }
}

export default function LoginForm({
    onRegisterClick,
}: LoginFormProps) {
    const { login, demoLogin, isLoading } = useAuth();
    const router = useRouter();

    const [values, setValues] = useState<LoginRequest>({
        email: "",
        password: "",
    });

    const [errors, setErrors] = useState<Record<string, string>>({});
    const [serverError, setServerError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    function selectDemoAccount(role: RoleName) {
        setValues({
            email: DEMO_ACCOUNTS[role],
            password: DEMO_PASSWORD,
        });

        setErrors({});
        setServerError("");
    }

    async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();

        const validationErrors = validateLogin(values);
        setErrors(validationErrors);
        setServerError("");

        if (Object.keys(validationErrors).length > 0) return;

        setIsSubmitting(true);

        try {
            const demoRole = (
                Object.keys(DEMO_ACCOUNTS) as RoleName[]
            ).find(
                (role) =>
                    values.email.trim().toLowerCase() ===
                        DEMO_ACCOUNTS[role] &&
                    values.password === DEMO_PASSWORD,
            );

            if (demoRole) {
                demoLogin(demoRole);
                router.push(getDashboardPath(demoRole));
                return;
            }

            const response = await loginRequest(values);
            await login(response);
            router.push(getDashboardPath(response.role));
        } catch (error) {
            setServerError(
                getApiErrorMessage(error, "Unable to sign in."),
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-heading">
                <h2>Welcome back</h2>
                <p>Sign in to continue learning.</p>
            </div>

            {serverError && (
                <div className="form-error" role="alert">
                    {serverError}
                </div>
            )}

            <label htmlFor="login-email">Email address</label>
            <input
                id="login-email"
                type="email"
                autoComplete="email"
                placeholder="you@example.com"
                value={values.email}
                onChange={(event) =>
                    setValues({ ...values, email: event.target.value })
                }
                aria-invalid={Boolean(errors.email)}
            />
            {errors.email && (
                <p className="field-error">{errors.email}</p>
            )}

            <label htmlFor="login-password">Password</label>
            <input
                id="login-password"
                type="password"
                autoComplete="current-password"
                placeholder="Enter your password"
                value={values.password}
                onChange={(event) =>
                    setValues({ ...values, password: event.target.value })
                }
                aria-invalid={Boolean(errors.password)}
            />
            {errors.password && (
                <p className="field-error">{errors.password}</p>
            )}

            <button type="submit" disabled={isSubmitting || isLoading}>
                {isSubmitting || isLoading ? "Signing in..." : "Sign in"}
            </button>

            <div className="demo-access">
                <div className="demo-heading">
                    <h3>Demo access</h3>
                    <p>Select a role to autofill the login fields.</p>
                </div>

                <div className="demo-buttons">
                    <button
                        type="button"
                        className="demo-button demo-admin"
                        disabled={isSubmitting || isLoading}
                        onClick={() => selectDemoAccount("ADMIN")}
                    >
                        Demo Admin
                    </button>

                    <button
                        type="button"
                        className="demo-button demo-trainer"
                        disabled={isSubmitting || isLoading}
                        onClick={() => selectDemoAccount("TRAINER")}
                    >
                        Demo Trainer
                    </button>

                    <button
                        type="button"
                        className="demo-button demo-learner"
                        disabled={isSubmitting || isLoading}
                        onClick={() => selectDemoAccount("LEARNER")}
                    >
                        Demo Learner
                    </button>
                </div>
            </div>

            <p className="auth-switch">
                Don&apos;t have an account?{" "}
                <button type="button" onClick={onRegisterClick}>
                    Create one
                </button>
            </p>
        </form>
    );
}