import { ApiError } from "@/lib/api/http";

/** Pesan error data yang gagal dimuat (mis. backend mati). */
export function ErrorBox({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? error.message : "Terjadi kesalahan saat memuat data.";
  return (
    <div role="alert" className="rounded-card border border-error/40 bg-error/10 p-4 text-sm text-error">
      {message}
    </div>
  );
}
