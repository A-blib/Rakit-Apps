"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { adminApi } from "@/lib/api/admin";
import { API_URL, CLIENT_HEADER } from "@/lib/api/config";
import { ApiError, apiFetch } from "@/lib/api/http";
import type { AuthResponse, DocAudience, DocCategoryValue, DocRequest, FeedbackStatus } from "@/lib/api/types";
import { ACCESS_COOKIE, REFRESH_COOKIE, REFRESH_MAX_AGE_SECONDS, cookieOptions } from "@/lib/auth/cookies";
import { adminToken } from "@/lib/auth/session";

/** Hasil Server Action untuk form (ditampilkan lewat useActionState). */
export interface FormState {
  error?: string;
  fieldErrors?: Record<string, string>;
}

// ---------- sesi ----------

/** Masuk dengan email + password akun OrgSpace yang ber-flag admin. */
export async function login(_: FormState, form: FormData): Promise<FormState> {
  const email = String(form.get("email") ?? "").trim();
  const password = String(form.get("password") ?? "");
  let auth: AuthResponse;
  try {
    auth = await apiFetch<AuthResponse>("/auth/login", { method: "POST", body: { email, password } });
  } catch (error) {
    return toFormState(error);
  }
  try {
    await adminApi.me(auth.accessToken);
  } catch (error) {
    // Bukan admin: sesi yang baru dibuat langsung dicabut agar tidak ada refresh token menggantung.
    await revoke(auth.refreshToken);
    return toFormState(error);
  }
  const store = await cookies();
  store.set(ACCESS_COOKIE, auth.accessToken, cookieOptions(auth.expiresIn));
  store.set(REFRESH_COOKIE, auth.refreshToken, cookieOptions(REFRESH_MAX_AGE_SECONDS));
  redirect("/dashboard");
}

export async function logout() {
  const store = await cookies();
  const refresh = store.get(REFRESH_COOKIE)?.value;
  if (refresh) {
    await revoke(refresh);
  }
  store.delete(ACCESS_COOKIE);
  store.delete(REFRESH_COOKIE);
  redirect("/dashboard/login");
}

async function revoke(refreshToken: string) {
  try {
    await fetch(`${API_URL}/auth/logout`, {
      method: "POST",
      headers: { ...CLIENT_HEADER, "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });
  } catch {
    // Backend mati: cookie tetap dihapus; refresh token kedaluwarsa sendiri.
  }
}

// ---------- kritik & saran ----------

export async function replyFeedback(id: string, _: FormState, form: FormData): Promise<FormState> {
  const body = String(form.get("body") ?? "").trim();
  if (!body) {
    return { fieldErrors: { body: "Balasan wajib diisi" } };
  }
  try {
    await adminApi.replyFeedback(await adminToken(), id, body);
  } catch (error) {
    return toFormState(error);
  }
  revalidatePath(`/dashboard/feedback/${id}`);
  return {};
}

export async function setFeedbackStatus(id: string, form: FormData) {
  await adminApi.setFeedbackStatus(await adminToken(), id, String(form.get("status")) as FeedbackStatus);
  revalidatePath(`/dashboard/feedback/${id}`);
}

// ---------- dokumentasi ----------

export async function saveDoc(id: string | null, _: FormState, form: FormData): Promise<FormState> {
  const request: DocRequest = {
    slug: String(form.get("slug") ?? "").trim(),
    category: String(form.get("category")) as DocCategoryValue,
    audience: String(form.get("audience") ?? "all") as DocAudience,
    title: String(form.get("title") ?? "").trim(),
    summary: String(form.get("summary") ?? "").trim() || null,
    body: String(form.get("body") ?? ""),
    sortOrder: Number(form.get("sortOrder") ?? 0),
    status: form.get("published") === "on" ? "published" : "draft",
  };
  const token = await adminToken();
  try {
    if (id) {
      await adminApi.updateDoc(token, id, request);
    } else {
      await adminApi.createDoc(token, request);
    }
  } catch (error) {
    return toFormState(error);
  }
  revalidatePath("/dashboard/docs");
  revalidatePath("/docs", "layout");
  redirect("/dashboard/docs");
}

export async function deleteDoc(id: string) {
  await adminApi.deleteDoc(await adminToken(), id);
  revalidatePath("/docs", "layout");
  redirect("/dashboard/docs");
}

function toFormState(error: unknown): FormState {
  if (error instanceof ApiError) {
    const messages: Record<string, string> = {
      INVALID_CREDENTIALS: "Email atau password salah.",
      USE_SOCIAL_LOGIN: "Akun ini memakai login Google/GitHub. Dashboard saat ini hanya mendukung email + password.",
      ADMIN_ONLY: "Akun ini bukan tim internal OrgSpace.",
    };
    return { error: messages[error.code] ?? error.message, fieldErrors: error.fieldErrors };
  }
  return { error: "Terjadi kesalahan. Coba lagi." };
}
