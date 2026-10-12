import { API_URL, CLIENT_HEADER } from "./config";
import type { ErrorResponse } from "./types";

/** Error dari backend (atau jaringan) dengan kode yang bisa dipakai untuk memilih pesan. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
    readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiError";
  }

  /** Backend mati / tidak terjangkau. */
  get isNetwork(): boolean {
    return this.status === 0;
  }
}

export interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  /** Access token admin/user. Kosong untuk endpoint publik. */
  token?: string;
  /** Detik cache Next.js untuk GET publik (mis. 300). Kosong = selalu ambil baru. */
  revalidate?: number;
  query?: Record<string, string | number | boolean | null | undefined>;
}

/**
 * Satu-satunya pintu ke backend. Semua fungsi di lib/api memakai ini, jadi header, format error, dan cache diatur
 * di satu tempat.
 */
export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const url = new URL(API_URL + path);
  for (const [key, value] of Object.entries(options.query ?? {})) {
    if (value !== null && value !== undefined && value !== "") {
      url.searchParams.set(key, String(value));
    }
  }
  const headers: Record<string, string> = { ...CLIENT_HEADER, Accept: "application/json" };
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`;
  }
  let response: Response;
  try {
    response = await fetch(url, {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      ...(options.revalidate !== undefined && !options.token
        ? { next: { revalidate: options.revalidate } }
        : { cache: "no-store" as const }),
    });
  } catch {
    throw new ApiError(0, "NETWORK_ERROR", "Server OrgSpace tidak bisa dihubungi. Pastikan backend berjalan.");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const data = text ? JSON.parse(text) : undefined;
  if (!response.ok) {
    const error = (data ?? {}) as Partial<ErrorResponse>;
    throw new ApiError(
      response.status,
      error.code ?? "INTERNAL_ERROR",
      error.message ?? `Permintaan gagal (${response.status}).`,
      error.fieldErrors ?? {},
    );
  }
  return data as T;
}
