"use client";

import { useActionState } from "react";
import type { PortalUpdate } from "@/lib/api/types";
import { UPDATE_CATEGORY_LABEL, formatDateTime } from "@/lib/format";
import { saveUpdate, type FormState } from "../../actions";

/**
 * CONTOH AWAL untuk tim web: form buat/ubah info terbaru.
 * Bagian "Kartu landing" = yang tampil di landing; "Halaman detail" = isi lengkap di /info/{id} (Markdown).
 */
export function UpdateForm({ update }: { update?: PortalUpdate }) {
  const [state, action, pending] = useActionState<FormState, FormData>(saveUpdate.bind(null, update?.id ?? null), {});
  const error = (field: string) =>
    state.fieldErrors?.[field] ? <p className="mt-1 text-xs text-rose-600 font-medium">{state.fieldErrors[field]}</p> : null;

  return (
    <form action={action} className="space-y-6">
      <fieldset className="card space-y-4 p-5">
        <legend className="px-1 text-sm font-semibold text-foreground">Kartu landing</legend>
        <div className="grid gap-4 sm:grid-cols-[1fr_12rem]">
          <div>
            <label className="label" htmlFor="title">Judul</label>
            <input className="input" id="title" name="title" defaultValue={update?.title} maxLength={120} required />
            {error("title")}
          </div>
          <div>
            <label className="label" htmlFor="category">Kategori</label>
            <select className="input text-xs" id="category" name="category" defaultValue={update?.category ?? "fitur"}>
              {Object.entries(UPDATE_CATEGORY_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </div>
        </div>
        <div>
          <label className="label" htmlFor="summary">Ringkasan (tampil di kartu)</label>
          <textarea className="textarea min-h-20" id="summary" name="summary" defaultValue={update?.summary} maxLength={300} required />
          {error("summary")}
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="label" htmlFor="image">Gambar (PNG/JPG, maks 2 MB)</label>
            <input className="input text-xs" id="image" name="image" type="file" accept="image/png,image/jpeg" />
            {error("image")}
            {update?.imageUpdatedAt && (
              <label className="mt-2 flex items-center gap-2 text-xs text-muted">
                <input type="checkbox" name="resetImage" className="size-4 rounded border-border" />
                Hapus gambar yang sekarang
              </label>
            )}
          </div>
          <div>
            <label className="label" htmlFor="imageAlt">Deskripsi gambar (untuk pembaca layar)</label>
            <input className="input" id="imageAlt" name="imageAlt" defaultValue={update?.imageAlt ?? ""} maxLength={200} />
          </div>
        </div>
      </fieldset>

      <fieldset className="card space-y-4 p-5">
        <legend className="px-1 text-sm font-semibold text-foreground">Halaman detail</legend>
        <div>
          <label className="label" htmlFor="content">Isi lengkap (Markdown: ## judul, **tebal**, - daftar, [link](https://...))</label>
          <textarea className="textarea min-h-80 font-mono text-xs leading-relaxed" id="content" name="content" defaultValue={update?.content ?? ""} maxLength={20000} required />
          {error("content")}
        </div>
      </fieldset>

      <div>
        <label className="label" htmlFor="displayDays">Lama tampil di landing (hari)</label>
        <input
          className="input max-w-40"
          id="displayDays"
          name="displayDays"
          type="number"
          min={1}
          max={3650}
          defaultValue={update?.displayDays ?? ""}
          placeholder="Tanpa batas"
        />
        <p className="mt-1 text-xs text-muted">
          Dihitung sejak pertama tayang. Kosongkan untuk tanpa batas. Setelah lewat, info hilang dari landing dan halaman
          detail, tapi tetap ada di sini dan bisa diperpanjang.
          {update?.expiresAt && <> Berakhir: {formatDateTime(update.expiresAt)}.</>}
        </p>
        {error("displayDays")}
      </div>

      <div className="flex items-center gap-2.5 rounded-field border border-border bg-surface-subtle p-3.5">
        <input type="checkbox" id="published" name="published" defaultChecked={update?.status === "published"} className="size-4 rounded border-border text-primary focus:ring-primary/20" />
        <label htmlFor="published" className="text-xs font-medium text-foreground cursor-pointer mb-0">
          Tayangkan di landing page (tidak dicentang = draft)
        </label>
      </div>

      {state.error && (
        <div role="alert" className="rounded-field border border-red-200 bg-red-50 p-3 text-xs text-red-800">
          {state.error}
        </div>
      )}

      <button type="submit" className="btn-primary" disabled={pending}>
        {pending ? "Menyimpan..." : "Simpan Info"}
      </button>
    </form>
  );
}
