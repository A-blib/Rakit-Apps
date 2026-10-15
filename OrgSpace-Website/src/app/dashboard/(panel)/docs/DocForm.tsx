"use client";

import { useActionState } from "react";
import type { DocDetail } from "@/lib/api/types";
import { DOC_CATEGORY_LABEL } from "@/lib/format";
import { saveDoc, type FormState } from "../../actions";

/** Form buat/ubah artikel. Isi berformat Markdown; pratinjau tampil di halaman /docs setelah ditayangkan. */
export function DocForm({ doc }: { doc?: DocDetail }) {
  const [state, action, pending] = useActionState<FormState, FormData>(saveDoc.bind(null, doc?.id ?? null), {});
  const error = (field: string) =>
    state.fieldErrors?.[field] ? <p className="mt-1 text-xs text-rose-600 font-medium">{state.fieldErrors[field]}</p> : null;

  return (
    <form action={action} className="max-w-3xl space-y-5">
      <div>
        <label className="label" htmlFor="title">Judul Artikel</label>
        <input className="input" id="title" name="title" defaultValue={doc?.title} maxLength={150} required />
        {error("title")}
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label className="label" htmlFor="slug">Slug (URL /docs/...)</label>
          <input className="input font-mono text-xs" id="slug" name="slug" defaultValue={doc?.slug} pattern="[a-z0-9]+(-[a-z0-9]+)*" required />
          {error("slug")}
        </div>
        <div>
          <label className="label" htmlFor="category">Kategori Panduan</label>
          <select className="input text-xs" id="category" name="category" defaultValue={doc?.category ?? "mulai"}>
            {Object.entries(DOC_CATEGORY_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="audience">Target Pembaca</label>
          <select className="input text-xs" id="audience" name="audience" defaultValue={doc?.audience ?? "all"}>
            <option value="all">Semua Pengguna</option>
            <option value="creator">Pembuat website</option>
            <option value="provider">Penyedia template</option>
          </select>
        </div>
        <div>
          <label className="label" htmlFor="sortOrder">Urutan Tampil (Sort Order)</label>
          <input className="input text-xs" id="sortOrder" name="sortOrder" type="number" min={0} defaultValue={doc?.sortOrder ?? 0} />
        </div>
      </div>

      <div>
        <label className="label" htmlFor="summary">Ringkasan Singkat</label>
        <input className="input" id="summary" name="summary" defaultValue={doc?.summary ?? ""} maxLength={300} placeholder="Ikhtisar ringkas 1-2 kalimat..." />
      </div>

      <div>
        <label className="label" htmlFor="body">Konten Artikel (Format Markdown)</label>
        <textarea className="textarea min-h-80 font-mono text-xs leading-relaxed" id="body" name="body" defaultValue={doc?.body} required />
        {error("body")}
      </div>

      <div className="flex items-center gap-2.5 rounded-field border border-border bg-surface-subtle p-3.5">
        <input
          type="checkbox"
          id="published"
          name="published"
          defaultChecked={doc?.status === "published"}
          className="size-4 rounded border-border text-primary focus:ring-primary/20"
        />
        <label htmlFor="published" className="text-xs font-medium text-foreground cursor-pointer mb-0">
          Tayangkan artikel ini langsung di direktori publik <code className="text-navy font-mono">/docs</code>
        </label>
      </div>

      {state.error && (
        <div role="alert" className="rounded-field border border-red-200 bg-red-50 p-3 text-xs text-red-800">
          {state.error}
        </div>
      )}

      <div className="pt-2">
        <button type="submit" className="btn-primary" disabled={pending}>
          {pending ? "Menyimpan Artikel..." : "Simpan Perubahan"}
        </button>
      </div>
    </form>
  );
}
