"use client";

import { useState } from "react";
import type { SyntheticEvent } from "react";
import { register as registerRequest } from "@/services/auth-service";
import { getApiErrorMessage } from "@/lib/api-error";
import { validateRegistration } from "@/lib/auth-validation";
import type { RegisterUserRequest } from "@/types/auth";

interface RegisterFormProps {
  onLoginClick: () => void;
}

export default function RegisterForm({
  onLoginClick,
}: RegisterFormProps) {
  const [values, setValues] = useState<
    RegisterUserRequest & { confirmPassword: string }
  >({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    phone: "",
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState("");
  const [success, setSuccess] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationErrors = validateRegistration(values);
    setErrors(validationErrors);
    setServerError("");
    setSuccess("");

    if (Object.keys(validationErrors).length > 0) return;

    setIsSubmitting(true);

    try {
      await registerRequest({
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        email: values.email.trim(),
        password: values.password,
        phone: values.phone?.trim() || undefined,
      });

      setSuccess("Your account was created successfully. You can now sign in.");
      setValues({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
        phone: "",
      });
    } catch (error) {
      setServerError(
        getApiErrorMessage(error, "Unable to create your account."),
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="auth-form">
      <div className="form-heading">
        <h2>Create your account</h2>
        <p>Join the learning management system.</p>
      </div>

      {serverError && (
        <div className="form-error" role="alert">
          {serverError}
        </div>
      )}

      {success && (
        <div className="form-success" role="status">
          {success}
          {" "}
          <button type="button" onClick={onLoginClick}>
            Sign in
          </button>
        </div>
      )}

      <div className="form-row">
        <div>
          <label htmlFor="register-first-name">First name</label>
          <input
            id="register-first-name"
            autoComplete="given-name"
            value={values.firstName}
            onChange={(event) =>
              setValues({ ...values, firstName: event.target.value })
            }
            aria-invalid={Boolean(errors.firstName)}
          />
          {errors.firstName && (
            <p className="field-error">{errors.firstName}</p>
          )}
        </div>

        <div>
          <label htmlFor="register-last-name">Last name</label>
          <input
            id="register-last-name"
            autoComplete="family-name"
            value={values.lastName}
            onChange={(event) =>
              setValues({ ...values, lastName: event.target.value })
            }
            aria-invalid={Boolean(errors.lastName)}
          />
          {errors.lastName && (
            <p className="field-error">{errors.lastName}</p>
          )}
        </div>
      </div>

      <label htmlFor="register-email">Email address</label>
      <input
        id="register-email"
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

      <label htmlFor="register-phone">Phone number (optional)</label>
      <input
        id="register-phone"
        type="tel"
        autoComplete="tel"
        value={values.phone}
        onChange={(event) =>
          setValues({ ...values, phone: event.target.value })
        }
        aria-invalid={Boolean(errors.phone)}
      />
      {errors.phone && <p className="field-error">{errors.phone}</p>}

      <label htmlFor="register-password">Password</label>
      <input
        id="register-password"
        type="password"
        autoComplete="new-password"
        value={values.password}
        onChange={(event) =>
          setValues({ ...values, password: event.target.value })
        }
        aria-invalid={Boolean(errors.password)}
      />
      <p className="field-hint">Use between 12 and 72 characters.</p>
      {errors.password && <p className="field-error">{errors.password}</p>}

      <label htmlFor="register-confirm-password">Confirm password</label>
      <input
        id="register-confirm-password"
        type="password"
        autoComplete="new-password"
        value={values.confirmPassword}
        onChange={(event) =>
          setValues({ ...values, confirmPassword: event.target.value })
        }
        aria-invalid={Boolean(errors.confirmPassword)}
      />
      {errors.confirmPassword && (
        <p className="field-error">{errors.confirmPassword}</p>
      )}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Creating account..." : "Create account"}
      </button>

      <p className="auth-switch">
        Already have an account?{" "}
        <button type="button" onClick={onLoginClick}>
          Sign in
        </button>
      </p>
    </form>
  );
}
