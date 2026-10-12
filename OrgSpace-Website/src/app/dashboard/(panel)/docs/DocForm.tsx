"use client";

import { useActionState } from "react";
import type { DocDetail } from "@/lib/api/types";
import { DOC_CATEGORY_LABEL } from "@/lib/format";
import { saveDoc, type FormState } from "../../actions";

/** Form buat/ubah artikel. Isi berformat Markdown; pratinjau tampil di halaman /docs setelah ditayangkan. */
export function DocForm({ doc }: { doc?: DocDetail }) {
  const [state, action, pending] = useActionState<FormState, FormData>(saveDoc.bind(null, doc?.id ?? null), {});
  const error = (field: string) =>
    state.fieldErrors?.[field] ? <p className="mt-1 text-sm text-error">{state.fieldErrors[field]}</p> : null;
  return (
    <form action={action} className="max-w-3xl space-y-4">
      <div>
        <label className="label" htmlFor="title">Judul</label>
        <input className="input" id="title" name="title" defaultValue={doc?.title} maxLength={150} required />
        {error("title")}
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label className="label" htmlFor="slug">Slug (alamat /docs/...)</label>
          <input className="input font-mono" id="slug" name="slug" defaultValue={doc?.slug} pattern="[a-z0-9]+(-[a-z0-9]+)*" required />
          {error("slug")}
        </div>
        <div>
          <label className="label" htmlFor="category">Kategori</label>
          <select className="input" id="category" name="category" defaultValue={doc?.category ?? "mulai"}>
            {Object.entries(DOC_CATEGORY_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="audience">Untuk</label>
          <select className="input" id="audience" name="audience" defaultValue={doc?.audience ?? "all"}>
            <option value="all">Semua</option>
            <option value="creator">Pembuat website</option>
            <option value="provider">Penyedia template</option>
          </select>
        </div>
        <div>
          <label className="label" htmlFor="sortOrder">Urutan di kategori</label>
          <input className="input" id="sortOrder" name="sortOrder" type="number" min={0} defaultValue={doc?.sortOrder ?? 0} />
        </div>
      </div>
      <div>
        <label className="label" htmlFor="summary">Ringkasan</label>
        <input className="input" id="summary" name="summary" defaultValue={doc?.summary ?? ""} maxLength={300} />
      </div>
      <div>
        <label className="label" htmlFor="body">Isi (Markdown)</label>
        <textarea className="textarea min-h-96 font-mono" id="body" name="body" defaultValue={doc?.body} required />
        {error("body")}
      </div>
      <label className="flex items-center gap-2 text-sm">
        <input type="checkbox" name="published" defaultChecked={doc?.status === "published"} /> Tayangkan di /docs
      </label>
      {state.error && <p role="alert" className="text-sm text-error">{state.error}</p>}
      <button className="btn-primary" disabled={pending}>{pending ? "Menyimpan..." : "Simpan"}</button>
    </form>
  );
}
