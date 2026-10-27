"use client";

import Image from "next/image";
import { useState, useTransition } from "react";
import type { GalleryTemplate } from "@/lib/api/types";
import { templateThumbnailSrc } from "@/lib/format";
import { saveLandingTemplates, type FormState } from "../../actions";

/** Sama dengan batas backend (LandingTemplatesRequest). */
const MAX_FEATURED = 12;

/**
 * CONTOH AWAL: daftar unggulan (kiri, bisa diurutkan) + isi wadah galeri saat ini (kanan). Pilihan belum tersimpan
 * tetap ada saat pindah wadah, karena komponen ini tidak di-mount ulang oleh navigasi halaman galeri.
 */
export function FeaturedPicker({ initial, gallery }: { initial: GalleryTemplate[]; gallery: GalleryTemplate[] }) {
  const [selected, setSelected] = useState(initial);
  const [state, setState] = useState<FormState>({});
  const [pending, startTransition] = useTransition();
  const chosen = new Set(selected.map((t) => t.id));

  const move = (index: number, delta: number) =>
    setSelected((list) => {
      const next = [...list];
      [next[index], next[index + delta]] = [next[index + delta], next[index]];
      return next;
    });
  const save = () =>
    startTransition(async () => setState(await saveLandingTemplates(selected.map((t) => t.id))));

  return (
    <div className="grid gap-6 md:grid-cols-2">
      <div className="card p-4 space-y-3">
        <h3 className="text-sm font-semibold text-foreground">Dipajang ({selected.length}/{MAX_FEATURED})</h3>
        {selected.length === 0 && <p className="text-xs text-muted">Belum ada. Section template di landing disembunyikan.</p>}
        <ol className="space-y-2">
          {selected.map((template, i) => (
            <li key={template.id} className="flex items-center gap-2 text-xs">
              <span className="w-5 text-muted font-mono">{i + 1}.</span>
              <span className="flex-1 truncate text-foreground">{template.name}</span>
              <button type="button" className="btn-outline h-7 px-2" onClick={() => move(i, -1)} disabled={i === 0} aria-label={`Naikkan ${template.name}`}>↑</button>
              <button type="button" className="btn-outline h-7 px-2" onClick={() => move(i, 1)} disabled={i === selected.length - 1} aria-label={`Turunkan ${template.name}`}>↓</button>
              <button type="button" className="btn-outline h-7 px-2" onClick={() => setSelected((list) => list.filter((t) => t.id !== template.id))} aria-label={`Hapus ${template.name}`}>✕</button>
            </li>
          ))}
        </ol>
        {state.error && <div role="alert" className="rounded-field border border-red-200 bg-red-50 p-3 text-xs text-red-800">{state.error}</div>}
        {state.success && <div role="status" className="rounded-field border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800">{state.success}</div>}
        <button type="button" className="btn-primary" onClick={save} disabled={pending}>
          {pending ? "Menyimpan..." : "Simpan Template Unggulan"}
        </button>
      </div>

      <div className="space-y-2">
        <h3 className="text-sm font-semibold text-foreground">Galeri</h3>
        {gallery.length === 0 && <p className="text-xs text-muted">Tidak ada template di halaman ini.</p>}
        <ul className="grid gap-2 sm:grid-cols-2">
          {gallery.map((template) => {
            const thumbnail = templateThumbnailSrc(template);
            const disabled = chosen.has(template.id) || selected.length >= MAX_FEATURED;
            return (
              <li key={template.id} className="card overflow-hidden text-xs">
                <div className="aspect-[4/3] bg-surface-subtle">
                  {thumbnail && (
                    <Image src={thumbnail} alt="" width={320} height={240} unoptimized className="h-full w-full object-cover object-top" />
                  )}
                </div>
                <div className="p-2 space-y-1.5">
                  <p className="font-medium text-foreground truncate">{template.name}</p>
                  <p className="text-muted truncate">oleh {template.creatorName}</p>
                  <button
                    type="button"
                    className="btn-outline h-7 w-full text-xs"
                    disabled={disabled}
                    onClick={() => setSelected((list) => [...list, template])}
                  >
                    {chosen.has(template.id) ? "Sudah dipilih" : "Pajang"}
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}
