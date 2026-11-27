"use client";

import { useState } from "react";
import type { SyntheticEvent } from "react";
import { login as loginRequest } from "@/services/auth-service";
import { getApiErrorMessage } from "@/lib/api-error";
import { validateLogin } from "@/lib/auth-validation";
import { useAuth } from "@/contexts/AuthContext";
import type { LoginRequest } from "@/types/auth";
import { useRouter } from "next/navigation";

interface LoginFormProps {
    onRegisterClick: () => void;
}

export default function LoginForm({
    onRegisterClick,
}: LoginFormProps) {
    const { login } = useAuth();

    const router = useRouter();

    const [values, setValues] = useState<LoginRequest>({
        email: "",
        password: "",
    });

    const [errors, setErrors] = useState<Record<string, string>>({});
    const [serverError, setServerError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();

        const validationErrors = validateLogin(values);
        setErrors(validationErrors);
        setServerError("");

        if (Object.keys(validationErrors).length > 0) return;

        setIsSubmitting(true);

        try {
            const response = await loginRequest(values);
            login(response);
            router.push("/dashboard");
        } catch (error) {
            setServerError(getApiErrorMessage(error, "Unable to sign in."));
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
        {errors.email && <p className="field-error">{errors.email}</p>}

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
        {errors.password && <p className="field-error">{errors.password}</p>}

        <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Sign in"}
        </button>

        <p className="auth-switch">
            Don&apos;t have an account?{" "}
            <button type="button" onClick={onRegisterClick}>
            Create one
            </button>
        </p>
        </form>
    );
}