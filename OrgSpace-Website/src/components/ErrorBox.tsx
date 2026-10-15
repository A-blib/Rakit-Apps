import { ApiError } from "@/lib/api/http";

/** Pesan error data yang gagal dimuat (mis. backend mati). */
export function ErrorBox({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? error.message : "Terjadi kesalahan saat memuat data.";
  return (
    <div
      role="alert"
      className="flex items-start gap-3 rounded-card border border-red-200 bg-red-50/70 p-4 text-xs text-red-900 dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-300"
    >
      <svg className="size-4.5 shrink-0 text-red-600 dark:text-red-400 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
        <path
          fillRule="evenodd"
          d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
          clipRule="evenodd"
        />
      </svg>
      <div>
        <p className="font-semibold text-red-950 dark:text-red-200">Gagal Memuat Data</p>
        <p className="mt-0.5 text-red-800 dark:text-red-300/90 leading-relaxed">{message}</p>
      </div>
    </div>
  );
}
