"use client";

import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";

export default function AdminDashboard() {
    const { user, logout } = useAuth();
    const router = useRouter();

    function handleLogout() {
        logout();
        router.push("/auth");
    }

    return (
        <main>
            <h1>Admin Dashboard</h1>
            <h2>Hello, {user?.firstName}!</h2>
            <p>Welcome to the Team 6 LMS Admin Dashboard.</p>
            <p>Role: {user?.role}</p>

            <button onClick={handleLogout}>
                Logout
            </button>
        </main>
    );
}
