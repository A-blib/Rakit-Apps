"use client";

import Image from "next/image";
import { useActionState } from "react";
import type { LandingHero } from "@/lib/api/types";
import { heroImageSrc } from "@/lib/format";
import { saveLandingHero, type FormState } from "../../actions";

/** CONTOH AWAL: form hero. Gambar baru opsional; jika tidak dipilih, gambar yang tampil sekarang dipertahankan. */
export function HeroForm({ hero }: { hero: LandingHero }) {
  const [state, action, pending] = useActionState<FormState, FormData>(saveLandingHero, {});
  const error = (field: string) =>
    state.fieldErrors?.[field] ? <p className="mt-1 text-xs text-rose-600 font-medium">{state.fieldErrors[field]}</p> : null;

  return (
    <form action={action} className="space-y-5">
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label className="label" htmlFor="headline">Judul</label>
          <input className="input" id="headline" name="headline" defaultValue={hero.headline} maxLength={120} required />
          {error("headline")}
        </div>
        <div>
          <label className="label" htmlFor="headlineHighlight">Lanjutan judul (berwarna)</label>
          <input className="input" id="headlineHighlight" name="headlineHighlight" defaultValue={hero.headlineHighlight} maxLength={80} required />
          {error("headlineHighlight")}
        </div>
      </div>

      <div>
        <label className="label" htmlFor="subheadline">Subjudul</label>
        <textarea className="textarea min-h-20" id="subheadline" name="subheadline" defaultValue={hero.subheadline} maxLength={300} required />
        {error("subheadline")}
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <div>
          <label className="label" htmlFor="primaryCtaLabel">Teks tombol utama</label>
          <input className="input" id="primaryCtaLabel" name="primaryCtaLabel" defaultValue={hero.primaryCtaLabel} maxLength={60} required />
          {error("primaryCtaLabel")}
        </div>
        <div>
          <label className="label" htmlFor="secondaryCtaLabel">Teks tombol kedua</label>
          <input className="input" id="secondaryCtaLabel" name="secondaryCtaLabel" defaultValue={hero.secondaryCtaLabel} maxLength={60} required />
          {error("secondaryCtaLabel")}
        </div>
        <div>
          <label className="label" htmlFor="secondaryCtaUrl">Link tombol kedua</label>
          <input className="input font-mono text-xs" id="secondaryCtaUrl" name="secondaryCtaUrl" defaultValue={hero.secondaryCtaUrl} maxLength={300} placeholder="/docs atau https://..." required />
          {error("secondaryCtaUrl")}
        </div>
      </div>

      <div>
        <label className="label" htmlFor="highlights">Poin di bawah tombol (satu per baris, maks 3)</label>
        <textarea className="textarea min-h-20" id="highlights" name="highlights" defaultValue={hero.highlights.join("\n")} />
        {error("highlights")}
      </div>

      <fieldset className="space-y-3 rounded-field border border-border p-4">
        <legend className="px-1 text-xs font-semibold text-foreground">Gambar hero</legend>
        <Image
          src={heroImageSrc(hero)}
          alt={hero.imageAlt}
          width={1548}
          height={1080}
          unoptimized
          className="h-auto w-full max-w-sm rounded-field border border-border bg-surface-subtle"
        />
        <div>
          <label className="label" htmlFor="image">Ganti gambar (PNG/JPG, maks 2 MB)</label>
          <input className="input text-xs" id="image" name="image" type="file" accept="image/png,image/jpeg" />
          {error("image")}
        </div>
        {hero.imageUpdatedAt && (
          <div className="flex items-center gap-2.5">
            <input type="checkbox" id="resetImage" name="resetImage" className="size-4 rounded border-border text-primary focus:ring-primary/20" />
            <label htmlFor="resetImage" className="text-xs font-medium text-foreground cursor-pointer mb-0">Kembali ke gambar bawaan</label>
          </div>
        )}
        <div>
          <label className="label" htmlFor="imageAlt">Deskripsi gambar (untuk pembaca layar)</label>
          <input className="input" id="imageAlt" name="imageAlt" defaultValue={hero.imageAlt} maxLength={200} required />
          {error("imageAlt")}
        </div>
      </fieldset>

      {state.error && (
        <div role="alert" className="rounded-field border border-red-200 bg-red-50 p-3 text-xs text-red-800">{state.error}</div>
      )}
      {state.success && (
        <div role="status" className="rounded-field border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800">{state.success}</div>
      )}

      <button type="submit" className="btn-primary" disabled={pending}>
        {pending ? "Menyimpan..." : "Simpan Hero"}
      </button>
    </form>
  );
}
