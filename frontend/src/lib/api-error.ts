import axios from "axios";
import type { ApiErrorResponse } from "@/types/api";

export function getApiErrorMessage(
    error: unknown,
    fallback = "Something went wrong. Please try again.",
): string {
    if (!axios.isAxiosError<ApiErrorResponse>(error)) {
        return fallback;
    }

    const data = error.response?.data;

    if (!data) {
        if (error.code === "ECONNABORTED") {
            return "The request timed out. Please try again.";
        }

    return error.message === "Network Error"
        ? "Unable to connect to the server. Please try again later."
        : fallback;
    }

    if (data.message) return data.message;
    if (data.detail) return data.detail;
    if (data.error) return data.error;

    if (Array.isArray(data.errors)) {
        return data.errors.join(" ");
    }

    if (data.errors && typeof data.errors === "object") {
        return Object.values(data.errors).join(" ");
    }

    return fallback;
}
