import axios from "axios";

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
    headers: {
        "Content-Type": "application/json",
    },
    timeout: 15000,
});

let accessToken: string | null = null;

export function setApiAccessToken(token: string | null) {
    accessToken = token;
}

api.interceptors.request.use((config) => {
    if (accessToken) {
        config.headers.set("Authorization", `Bearer ${accessToken}`);
    }

    return config;
});

export default api;