"use client";

import { useState } from "react";
import LoginForm from "@/components/auth/LoginForm";
import RegisterForm from "@/components/auth/RegisterForm";

type AuthMode = "login" | "register";

export default function AuthPage() {
  const [mode, setMode] = useState<AuthMode>("login");

  return (
    <main className="auth-page">
      <section className="auth-brand-panel">
        <div className="brand-content">
          <span className="brand-label">TEAM 6 LMS</span>
          <h1>Learn. Grow. Achieve.</h1>
          <p>
            Your learning journey starts here. Access your courses,
            track your progress, and build new skills.
          </p>
        </div>
      </section>

      <section className="auth-form-panel">
        <div className="auth-card">
          <div className="auth-tabs" role="tablist" aria-label="Authentication">
            <button
              type="button"
              role="tab"
              aria-selected={mode === "login"}
              className={mode === "login" ? "active" : ""}
              onClick={() => setMode("login")}
            >
              Sign in
            </button>

            <button
              type="button"
              role="tab"
              aria-selected={mode === "register"}
              className={mode === "register" ? "active" : ""}
              onClick={() => setMode("register")}
            >
              Register
            </button>
          </div>

          {mode === "login" ? (
            <LoginForm onRegisterClick={() => setMode("register")} />
          ) : (
            <RegisterForm onLoginClick={() => setMode("login")} />
          )}
        </div>
      </section>
    </main>
  );
}
