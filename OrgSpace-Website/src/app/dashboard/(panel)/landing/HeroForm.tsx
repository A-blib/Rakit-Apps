"use client";

import Image from "next/image";
import { useState, useActionState, useEffect, useId } from "react";
import type { LandingHero } from "@/lib/api/types";
import { heroImageSrc } from "@/lib/format";
import { saveLandingHero, type FormState } from "../../actions";

interface HeroFormProps {
  hero: LandingHero;
}

export function HeroForm({ hero }: HeroFormProps) {
  const [state, action, pending] = useActionState<FormState, FormData>(saveLandingHero, {});

  // Local live state untuk pratinjau instan
  const [headline, setHeadline] = useState(hero.headline);
  const [headlineHighlight, setHeadlineHighlight] = useState(hero.headlineHighlight);
  const [subheadline, setSubheadline] = useState(hero.subheadline);
  const [primaryCtaLabel, setPrimaryCtaLabel] = useState(hero.primaryCtaLabel);
  const [secondaryCtaLabel, setSecondaryCtaLabel] = useState(hero.secondaryCtaLabel);
  const [secondaryCtaUrl, setSecondaryCtaUrl] = useState(hero.secondaryCtaUrl);
  const [highlights, setHighlights] = useState<string[]>(
    hero.highlights.length ? hero.highlights.slice(0, 3) : ["Tanpa Kartu Kredit", "Siap Online 5 Menit", "Hosting & SSL Otomatis"]
  );
  const [imageAlt, setImageAlt] = useState(hero.imageAlt);
  const [previewImageUrl, setPreviewImageUrl] = useState<string | null>(null);
  const [resetImage, setResetImage] = useState(false);
  const [previewDevice, setPreviewDevice] = useState<"desktop" | "mobile">("desktop");

  const fallbackImageSrc = heroImageSrc(hero);
  const activeImageSrc = previewImageUrl || (resetImage ? "/images/orgspace-workspace-hero.png" : fallbackImageSrc);

  const resetImageCheckboxId = useId();

  // Cleanup object URL saat unmount atau file berubah
  useEffect(() => {
    return () => {
      if (previewImageUrl) {
        URL.revokeObjectURL(previewImageUrl);
      }
    };
  }, [previewImageUrl]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (previewImageUrl) {
        URL.revokeObjectURL(previewImageUrl);
      }
      setPreviewImageUrl(URL.createObjectURL(file));
      setResetImage(false);
    }
  };

  const handleHighlightChange = (index: number, val: string) => {
    setHighlights((prev) => {
      const copy = [...prev];
      copy[index] = val;
      return copy;
    });
  };

  const error = (field: string) =>
    state.fieldErrors?.[field] ? (
      <p className="mt-1 text-xs text-rose-600 font-semibold">{state.fieldErrors[field]}</p>
    ) : null;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
      {/* Kolom Kiri: Form Editor Konten Hero */}
      <form action={action} className="lg:col-span-7 space-y-6">
        {/* Status Alert */}
        {state.error && (
          <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-800 shadow-xs flex items-center gap-2.5">
            <svg className="size-4 text-rose-600 shrink-0" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clipRule="evenodd" />
            </svg>
            <span>{state.error}</span>
          </div>
        )}
        {state.success && (
          <div role="status" className="rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-xs text-emerald-800 shadow-xs flex items-center gap-2.5">
            <svg className="size-4 text-emerald-600 shrink-0" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
            </svg>
            <span>{state.success}</span>
          </div>
        )}

        {/* Seksi 1: Teks Utama & Headline */}
        <div className="card p-5 space-y-4">
          <div className="border-b border-border/70 pb-3 flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-foreground">1. Teks Headline & Subjudul</h3>
              <p className="text-xs text-muted">Pesan promosi pertama yang dibaca pengunjung</p>
            </div>
            <span className="rounded-badge bg-primary/10 px-2 py-0.5 text-[10px] font-bold text-primary">
              Header Beranda
            </span>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="label text-xs" htmlFor="headline">
                Judul Utama
              </label>
              <input
                className="input text-xs"
                id="headline"
                name="headline"
                value={headline}
                onChange={(e) => setHeadline(e.target.value)}
                maxLength={120}
                placeholder="Contoh: Bikin Website Impian,"
                required
              />
              {error("headline")}
            </div>

            <div>
              <label className="label text-xs" htmlFor="headlineHighlight">
                Lanjutan Judul Berwarna (Aksen)
              </label>
              <input
                className="input text-xs"
                id="headlineHighlight"
                name="headlineHighlight"
                value={headlineHighlight}
                onChange={(e) => setHeadlineHighlight(e.target.value)}
                maxLength={80}
                placeholder="Contoh: Tanpa Coding!"
                required
              />
              {error("headlineHighlight")}
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="label text-xs mb-0" htmlFor="subheadline">
                Subjudul Deskriptif
              </label>
              <span className="text-[10px] text-muted">{subheadline.length}/300</span>
            </div>
            <textarea
              className="textarea text-xs min-h-22 leading-relaxed"
              id="subheadline"
              name="subheadline"
              value={subheadline}
              onChange={(e) => setSubheadline(e.target.value)}
              maxLength={300}
              placeholder="Deskripsi singkat solusi dan kemudahan OrgSpace..."
              required
            />
            {error("subheadline")}
          </div>
        </div>

        {/* Seksi 2: Tombol Aksi (Call To Action) */}
        <div className="card p-5 space-y-4">
          <div className="border-b border-border/70 pb-3 flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-foreground">2. Tombol Aksi (CTA)</h3>
              <p className="text-xs text-muted">Arahkan pengunjung ke pendaftaran atau panduan</p>
            </div>
            <span className="rounded-badge bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300 px-2 py-0.5 text-[10px] font-bold border border-blue-200/60 dark:border-blue-800/60">
              Navigasi Cepat
            </span>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <div>
              <label className="label text-xs" htmlFor="primaryCtaLabel">
                Label Tombol Utama
              </label>
              <input
                className="input text-xs font-semibold"
                id="primaryCtaLabel"
                name="primaryCtaLabel"
                value={primaryCtaLabel}
                onChange={(e) => setPrimaryCtaLabel(e.target.value)}
                maxLength={60}
                placeholder="Mulai Sekarang – Gratis!"
                required
              />
              {error("primaryCtaLabel")}
            </div>

            <div>
              <label className="label text-xs" htmlFor="secondaryCtaLabel">
                Label Tombol Kedua
              </label>
              <input
                className="input text-xs"
                id="secondaryCtaLabel"
                name="secondaryCtaLabel"
                value={secondaryCtaLabel}
                onChange={(e) => setSecondaryCtaLabel(e.target.value)}
                maxLength={60}
                placeholder="Lihat Demo"
                required
              />
              {error("secondaryCtaLabel")}
            </div>

            <div>
              <label className="label text-xs" htmlFor="secondaryCtaUrl">
                Link URL Tombol Kedua
              </label>
              <input
                className="input font-mono text-xs"
                id="secondaryCtaUrl"
                name="secondaryCtaUrl"
                value={secondaryCtaUrl}
                onChange={(e) => setSecondaryCtaUrl(e.target.value)}
                maxLength={300}
                placeholder="/docs atau https://..."
                required
              />
              {error("secondaryCtaUrl")}
            </div>
          </div>
        </div>

        {/* Seksi 3: 3 Poin Keunggulan di Bawah Tombol */}
        <div className="card p-5 space-y-4">
          <div className="border-b border-border/70 pb-3 flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-foreground">3. Poin Keunggulan Singkat</h3>
              <p className="text-xs text-muted">Maksimal 3 poin mikro di bawah tombol aksi</p>
            </div>
            <span className="rounded-badge bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 px-2 py-0.5 text-[10px] font-bold border border-emerald-200/60 dark:border-emerald-800/60">
              3 Poin Mikro
            </span>
          </div>

          <div className="space-y-3">
            {[0, 1, 2].map((idx) => (
              <div key={idx} className="flex items-center gap-2.5">
                <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-emerald-700 text-xs font-bold dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-300/60">
                  {idx + 1}
                </span>
                <input
                  className="input text-xs flex-1"
                  value={highlights[idx] ?? ""}
                  onChange={(e) => handleHighlightChange(idx, e.target.value)}
                  placeholder={`Poin ke-${idx + 1} (mis. 100% Gratis)`}
                  maxLength={50}
                />
              </div>
            ))}
          </div>

          {/* Hidden textarea untuk menjaga format baris baru yang dibaca backend di actions.ts */}
          <textarea
            name="highlights"
            value={highlights.filter(Boolean).join("\n")}
            readOnly
            className="hidden"
          />
          {error("highlights")}
        </div>

        {/* Seksi 4: Gambar Banner Hero */}
        <div className="card p-5 space-y-4">
          <div className="border-b border-border/70 pb-3 flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-foreground">4. Aset Visual & Gambar Banner</h3>
              <p className="text-xs text-muted">Gambar ilustrasi atau mockup workspace di beranda</p>
            </div>
            <span className="rounded-badge bg-surface-subtle px-2 py-0.5 text-[10px] font-bold text-muted border border-border">
              PNG / JPG
            </span>
          </div>

          <div>
            <label className="label text-xs" htmlFor="image">
              Pilih Gambar Baru (Maks 2 MB, rasio 16:9 disarankan)
            </label>
            <input
              className="input text-xs cursor-pointer file:mr-3 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-primary/10 file:text-primary hover:file:bg-primary/20"
              id="image"
              name="image"
              type="file"
              accept="image/png,image/jpeg"
              onChange={handleFileChange}
            />
            {error("image")}
          </div>

          {hero.imageUpdatedAt && (
            <div className="flex items-center gap-2.5 rounded-lg bg-surface-subtle p-3 border border-border/60">
              <input
                type="checkbox"
                id={resetImageCheckboxId}
                name="resetImage"
                checked={resetImage}
                onChange={(e) => {
                  setResetImage(e.target.checked);
                  if (e.target.checked) setPreviewImageUrl(null);
                }}
                className="size-4 rounded border-border text-primary focus:ring-primary/20 cursor-pointer"
              />
              <label htmlFor={resetImageCheckboxId} className="text-xs font-medium text-foreground cursor-pointer mb-0">
                Kembalikan ke gambar bawaan OrgSpace (reset gambar kustom)
              </label>
            </div>
          )}

          <div>
            <label className="label text-xs" htmlFor="imageAlt">
              Deskripsi Alternatif Gambar (Accessibility / SEO)
            </label>
            <input
              className="input text-xs"
              id="imageAlt"
              name="imageAlt"
              value={imageAlt}
              onChange={(e) => setImageAlt(e.target.value)}
              maxLength={200}
              placeholder="Deskripsi untuk pembaca layar..."
              required
            />
            {error("imageAlt")}
          </div>
        </div>

        {/* Tombol Simpan Perubahan */}
        <div className="sticky bottom-4 z-20 flex items-center justify-between rounded-xl bg-surface/95 p-4 shadow-lg border border-border backdrop-blur-md">
          <p className="text-xs text-muted hidden sm:block">
            Perubahan langsung tersimpan ke halaman beranda publik.
          </p>
          <button
            type="submit"
            className="btn-primary w-full sm:w-auto px-6 py-2.5 text-xs font-bold cursor-pointer"
            disabled={pending}
          >
            {pending ? (
              <span className="flex items-center gap-2">
                <span className="size-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Menyimpan Hero...
              </span>
            ) : (
              "Simpan & Terapkan Perubahan"
            )}
          </button>
        </div>
      </form>

      {/* Kolom Kanan: Pratinjau Langsung (Live Visual Preview) */}
      <div className="lg:col-span-5 sticky top-20 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <span className="size-2 rounded-full bg-cyan animate-pulse" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-muted">
              Pratinjau Live Beranda
            </h3>
          </div>

          {/* Toggle Device Preview */}
          <div className="inline-flex rounded-lg border border-border bg-surface-subtle p-0.5 text-xs">
            <button
              type="button"
              onClick={() => setPreviewDevice("desktop")}
              className={`rounded px-2.5 py-1 text-[11px] font-semibold transition-all ${
                previewDevice === "desktop"
                  ? "bg-surface text-foreground shadow-2xs"
                  : "text-muted hover:text-foreground"
              }`}
            >
              Desktop
            </button>
            <button
              type="button"
              onClick={() => setPreviewDevice("mobile")}
              className={`rounded px-2.5 py-1 text-[11px] font-semibold transition-all ${
                previewDevice === "mobile"
                  ? "bg-surface text-foreground shadow-2xs"
                  : "text-muted hover:text-foreground"
              }`}
            >
              Mobile
            </button>
          </div>
        </div>

        {/* Simulated Screen Card */}
        <div
          className={`card overflow-hidden border border-border/80 shadow-md bg-white text-navy transition-all duration-300 ${
            previewDevice === "mobile" ? "max-w-[320px] mx-auto rounded-3xl border-4 border-slate-700" : "w-full"
          }`}
        >
          {/* Simulated Browser Bar */}
          <div className="flex items-center justify-between border-b border-border/50 bg-slate-100 px-3 py-1.5 text-[10px] text-muted select-none">
            <div className="flex items-center gap-1">
              <span className="size-2 rounded-full bg-rose-400" />
              <span className="size-2 rounded-full bg-amber-400" />
              <span className="size-2 rounded-full bg-emerald-400" />
            </div>
            <div className="rounded bg-white px-2 py-0.5 text-[9px] font-mono text-slate-600 border border-slate-200">
              orgspace.com
            </div>
            <div className="w-6" />
          </div>

          {/* Live Hero Simulation */}
          <div className="p-4 sm:p-5 space-y-4">
            <div className="space-y-2">
              <span className="inline-block rounded-full bg-cyan/15 text-navy px-2.5 py-0.5 text-[10px] font-bold">
                Platform Website Organisasi #1
              </span>
              <h4 className="text-lg sm:text-xl font-black leading-tight tracking-tight text-navy">
                {headline || "Judul Utama"}{" "}
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-cyan-500">
                  {headlineHighlight || "Aksen"}
                </span>
              </h4>
              <p className="text-xs text-muted leading-relaxed line-clamp-3">
                {subheadline || "Subjudul deskriptif akan tampil di sini..."}
              </p>
            </div>

            {/* Simulated CTA Buttons */}
            <div className="flex flex-wrap items-center gap-2 pt-1">
              <span className="inline-flex items-center rounded-lg bg-navy px-3 py-1.5 text-xs font-bold text-white shadow-xs">
                {primaryCtaLabel || "Mulai Gratis"}
              </span>
              <span className="inline-flex items-center rounded-lg border border-border px-3 py-1.5 text-xs font-medium text-navy bg-white">
                {secondaryCtaLabel || "Lihat Demo"}
              </span>
            </div>

            {/* Highlights micro pills */}
            <div className="flex flex-wrap items-center gap-1.5 pt-1 border-t border-border/40 text-[10px]">
              {highlights.filter(Boolean).map((h, i) => (
                <span
                  key={i}
                  className="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-0.5 font-medium text-slate-700 border border-slate-200"
                >
                  <svg className="size-2.5 text-emerald-600" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                  {h}
                </span>
              ))}
            </div>

            {/* Image Preview Box */}
            <div className="relative rounded-xl overflow-hidden border border-border bg-slate-50 aspect-video flex items-center justify-center">
              {activeImageSrc ? (
                <Image
                  src={activeImageSrc}
                  alt={imageAlt || "Hero Preview"}
                  fill
                  unoptimized
                  className="object-cover"
                />
              ) : (
                <span className="text-xs text-muted">Belum ada gambar</span>
              )}
            </div>
          </div>
        </div>

        <p className="text-[11px] text-muted text-center">
          Pratinjau di atas menyesuaikan secara otomatis saat Anda mengetik.
        </p>
      </div>
    </div>
  );
}
