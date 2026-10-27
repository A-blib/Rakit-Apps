"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { adminApi } from "@/lib/api/admin";
import { API_URL, CLIENT_HEADER } from "@/lib/api/config";
import { ApiError, apiFetch } from "@/lib/api/http";
import type {
  AuthResponse,
  DocAudience,
  DocCategoryValue,
  DocRequest,
  FeedbackStatus,
  LandingHeroRequest,
  PortalUpdateCategory,
  PortalUpdateRequest,
} from "@/lib/api/types";
import { ACCESS_COOKIE, REFRESH_COOKIE, REFRESH_MAX_AGE_SECONDS, cookieOptions } from "@/lib/auth/cookies";
import { adminToken } from "@/lib/auth/session";

/** Hasil Server Action untuk form (ditampilkan lewat useActionState). */
export interface FormState {
  error?: string;
  fieldErrors?: Record<string, string>;
  /** Pesan berhasil untuk form yang tetap di halaman yang sama (tanpa redirect). */
  success?: string;
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

// ---------- hero landing page ----------

/** Sama dengan batas backend; dicek di sini agar pesannya jelas sebelum file dikirim. */
const HERO_IMAGE_MAX_BYTES = 2 * 1024 * 1024;

export async function saveLandingHero(_: FormState, form: FormData): Promise<FormState> {
  const text = (name: string) => String(form.get(name) ?? "").trim();
  const request: LandingHeroRequest = {
    headline: text("headline"),
    headlineHighlight: text("headlineHighlight"),
    subheadline: text("subheadline"),
    primaryCtaLabel: text("primaryCtaLabel"),
    secondaryCtaLabel: text("secondaryCtaLabel"),
    secondaryCtaUrl: text("secondaryCtaUrl"),
    // Satu poin per baris; baris kosong diabaikan.
    highlights: text("highlights").split("\n").map((line) => line.trim()).filter(Boolean),
    imageAlt: text("imageAlt"),
  };
  const image = form.get("image");
  const file = image instanceof File && image.size > 0 ? image : null;
  if (file && file.size > HERO_IMAGE_MAX_BYTES) {
    return { fieldErrors: { image: "Gambar maksimal 2 MB. Pilih gambar yang lebih kecil." } };
  }
  const token = await adminToken();
  try {
    await adminApi.updateLandingHero(token, request);
    if (file) {
      await adminApi.uploadHeroImage(token, file);
    } else if (form.get("resetImage") === "on") {
      await adminApi.deleteHeroImage(token);
    }
  } catch (error) {
    return toFormState(error);
  }
  revalidatePath("/");
  revalidatePath("/dashboard/landing");
  return { success: "Hero tersimpan. Landing page sudah memakai isi terbaru." };
}

// ---------- info terbaru ----------

/** Membuat (id = null) atau mengubah info, lalu mengunggah/menghapus gambarnya. Gambar sama dengan batas hero. */
export async function saveUpdate(id: string | null, _: FormState, form: FormData): Promise<FormState> {
  const text = (name: string) => String(form.get(name) ?? "").trim();
  const request: PortalUpdateRequest = {
    category: text("category") as PortalUpdateCategory,
    title: text("title"),
    summary: text("summary"),
    content: text("content"),
    imageAlt: text("imageAlt") || null,
    published: form.get("published") === "on",
    // Kosong = tanpa batas; angka di luar 1–3650 ditolak backend dengan pesan field.
    displayDays: text("displayDays") ? Number(text("displayDays")) : null,
  };
  const image = form.get("image");
  const file = image instanceof File && image.size > 0 ? image : null;
  if (file && file.size > HERO_IMAGE_MAX_BYTES) {
    return { fieldErrors: { image: "Gambar maksimal 2 MB. Pilih gambar yang lebih kecil." } };
  }
  const token = await adminToken();
  let savedId: string;
  try {
    savedId = id ? (await adminApi.saveUpdate(token, id, request)).id : (await adminApi.createUpdate(token, request)).id;
    if (file) {
      await adminApi.uploadUpdateImage(token, savedId, file);
    } else if (form.get("resetImage") === "on") {
      await adminApi.deleteUpdateImage(token, savedId);
    }
  } catch (error) {
    return toFormState(error);
  }
  revalidatePath("/");
  revalidatePath(`/info/${savedId}`);
  revalidatePath("/dashboard/info");
  redirect("/dashboard/info");
}

/** Tombol Tampilkan/Sembunyikan di daftar: hanya status yang berubah, isi tetap. */
export async function setUpdatePublished(id: string, published: boolean) {
  await adminApi.setUpdatePublished(await adminToken(), id, published);
  revalidatePath("/");
  revalidatePath(`/info/${id}`);
  revalidatePath("/dashboard/info");
}

export async function deleteUpdate(id: string) {
  await adminApi.deleteUpdate(await adminToken(), id);
  revalidatePath("/");
  revalidatePath(`/info/${id}`);
  redirect("/dashboard/info");
}

// ---------- template unggulan landing page ----------

/** Menyimpan daftar template unggulan (maks 12, berurutan). Dipanggil langsung dari komponen, bukan dari <form>. */
export async function saveLandingTemplates(templateIds: string[]): Promise<FormState> {
  try {
    await adminApi.updateLandingTemplates(await adminToken(), templateIds);
  } catch (error) {
    return toFormState(error);
  }
  revalidatePath("/");
  return { success: "Template unggulan tersimpan." };
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
