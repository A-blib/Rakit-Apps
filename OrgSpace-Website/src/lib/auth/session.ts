import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { adminApi } from "@/lib/api/admin";
import { ApiError } from "@/lib/api/http";
import type { AdminMe } from "@/lib/api/types";
import { ACCESS_COOKIE } from "./cookies";

/** Access token sesi saat ini (sudah diperbarui proxy.ts bila hampir kedaluwarsa), atau undefined. */
export async function accessToken(): Promise<string | undefined> {
  return (await cookies()).get(ACCESS_COOKIE)?.value;
}

/**
 * Wajib dipanggil di layout dashboard. Mengembalikan token + profil admin; selain itu dialihkan ke halaman login.
 * Status admin selalu dicek ke backend (bukan dari cookie), jadi admin yang dicabut haknya langsung keluar.
 */
export async function requireAdmin(): Promise<{ token: string; admin: AdminMe }> {
  const token = await accessToken();
  if (!token) {
    redirect("/dashboard/login");
  }
  try {
    return { token, admin: await adminApi.me(token) };
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.code === "ADMIN_ONLY")) {
      redirect(`/dashboard/login?reason=${error.code === "ADMIN_ONLY" ? "not-admin" : "expired"}`);
    }
    throw error;
  }
}

/** Untuk halaman di dalam dashboard (layout sudah memastikan admin). */
export async function adminToken(): Promise<string> {
  const token = await accessToken();
  if (!token) {
    redirect("/dashboard/login");
  }
  return token;
}
