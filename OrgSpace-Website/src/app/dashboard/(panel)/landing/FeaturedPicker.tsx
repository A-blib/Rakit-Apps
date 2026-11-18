"use client";

import Image from "next/image";
import { useState, useTransition, useMemo } from "react";
import type { GalleryTemplate } from "@/lib/api/types";
import { templateThumbnailSrc } from "@/lib/format";
import { saveLandingTemplates, type FormState } from "../../actions";

const MAX_FEATURED = 12;

interface FeaturedPickerProps {
  initial: GalleryTemplate[];
  gallery: GalleryTemplate[];
}

export function FeaturedPicker({ initial, gallery }: FeaturedPickerProps) {
  const [selected, setSelected] = useState<GalleryTemplate[]>(initial);
  const [state, setState] = useState<FormState>({});
  const [pending, startTransition] = useTransition();
  const [filterCategory, setFilterCategory] = useState<string>("all");

  // State untuk Live Website Preview Modal
  const [previewTemplate, setPreviewTemplate] = useState<GalleryTemplate | null>(null);
  const [previewViewport, setPreviewViewport] = useState<"desktop" | "tablet" | "mobile">("desktop");

  const chosenSet = useMemo(() => new Set(selected.map((t) => t.id)), [selected]);

  // Kategori unik dari galeri untuk filter lokal cepat
  const categories = useMemo(() => {
    const set = new Set<string>();
    gallery.forEach((g) => {
      if (g.category) set.add(g.category);
    });
    return Array.from(set);
  }, [gallery]);

  const filteredGallery = useMemo(() => {
    if (filterCategory === "all") return gallery;
    return gallery.filter((g) => g.category === filterCategory);
  }, [gallery, filterCategory]);

  const move = (index: number, delta: number) => {
    setSelected((list) => {
      const next = [...list];
      const targetIndex = index + delta;
      if (targetIndex < 0 || targetIndex >= next.length) return next;
      [next[index], next[targetIndex]] = [next[targetIndex], next[index]];
      return next;
    });
  };

  const remove = (id: string) => {
    setSelected((list) => list.filter((t) => t.id !== id));
  };

  const add = (template: GalleryTemplate) => {
    if (selected.length >= MAX_FEATURED || chosenSet.has(template.id)) return;
    setSelected((list) => [...list, template]);
  };

  const save = () => {
    startTransition(async () => {
      const res = await saveLandingTemplates(selected.map((t) => t.id));
      setState(res);
    });
  };

  const pct = Math.round((selected.length / MAX_FEATURED) * 100);

  return (
    <>
      <div className="grid gap-8 lg:grid-cols-12 items-start">
        {/* Kolom Kiri: Template yang Terpilih / Dipajang (Maks 12) */}
        <div className="lg:col-span-5 space-y-4">
          <div className="card p-5 space-y-4 sticky top-20">
            <div className="border-b border-border/70 pb-3 flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-foreground">Template Dipajang di Beranda</h3>
                <p className="text-xs text-muted">Urutan kartu di bawah menentukan urutan tampil</p>
              </div>
              <span className="rounded-badge bg-primary/10 px-2 py-0.5 text-[11px] font-bold font-mono text-primary">
                {selected.length}/{MAX_FEATURED}
              </span>
            </div>

            {/* Progress Bar Kapasitas */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-[11px]">
                <span className="text-muted">Kapasitas Slot Beranda</span>
                <span className="font-semibold text-foreground">{pct}% terisi</span>
              </div>
              <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-300 ${
                    selected.length >= MAX_FEATURED ? "bg-amber-500" : "bg-primary"
                  }`}
                  style={{ width: `${pct}%` }}
                />
              </div>
            </div>

            {/* Alert Status */}
            {state.error && (
              <div role="alert" className="rounded-lg border border-rose-200 bg-rose-50 p-3 text-xs text-rose-800 flex items-center gap-2">
                <svg className="size-4 text-rose-600 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clipRule="evenodd" />
                </svg>
                <span>{state.error}</span>
              </div>
            )}
            {state.success && (
              <div role="status" className="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800 flex items-center gap-2">
                <svg className="size-4 text-emerald-600 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
                </svg>
                <span>{state.success}</span>
              </div>
            )}

            {/* List Kartu Terpilih */}
            {selected.length === 0 ? (
              <div className="rounded-xl border border-dashed border-border p-6 text-center text-xs text-muted">
                <p className="font-semibold text-foreground mb-1">Belum ada template yang dipilih</p>
                <p>Pilih dari galeri di sebelah kanan untuk memajang template di beranda.</p>
              </div>
            ) : (
              <ol className="space-y-2 max-h-[480px] overflow-y-auto pr-1">
                {selected.map((template, i) => {
                  const thumbnail = templateThumbnailSrc(template);
                  return (
                    <li
                      key={template.id}
                      className="group flex items-center gap-2.5 rounded-lg border border-border bg-surface p-2 transition-all hover:border-primary/50 hover:shadow-xs"
                    >
                      {/* Rank Number */}
                      <span className="flex size-5 shrink-0 items-center justify-center rounded bg-surface-subtle text-[11px] font-bold font-mono text-muted border border-border/60">
                        {i + 1}
                      </span>

                      {/* Thumbnail */}
                      <div className="relative size-10 shrink-0 rounded overflow-hidden bg-surface-subtle border border-border/60">
                        {thumbnail ? (
                          <Image
                            src={thumbnail}
                            alt={template.name}
                            fill
                            unoptimized
                            className="object-cover"
                          />
                        ) : (
                          <div className="size-full flex items-center justify-center text-[9px] text-muted">No IMG</div>
                        )}
                      </div>

                      {/* Metadata */}
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-xs font-bold text-foreground leading-tight">
                          {template.name}
                        </p>
                        <p className="truncate text-[10px] text-muted leading-tight mt-0.5">
                          oleh {template.creatorName}
                        </p>
                      </div>

                      {/* Action Controls */}
                      <div className="flex items-center gap-1 shrink-0">
                        {/* Tombol Preview Website */}
                        <button
                          type="button"
                          onClick={() => setPreviewTemplate(template)}
                          title="Lihat Pratinjau Website"
                          className="flex size-6 items-center justify-center rounded border border-cyan/40 bg-cyan/10 text-cyan hover:bg-cyan hover:text-navy cursor-pointer transition-colors"
                        >
                          <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M10 12.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z" />
                            <path fillRule="evenodd" d="M.664 10.59a1.651 1.651 0 010-1.186A10.004 10.004 0 0110 3c4.257 0 7.893 2.66 9.336 6.41.147.381.146.804 0 1.186A10.004 10.004 0 0110 17c-4.257 0-7.893-2.66-9.336-6.41zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                          </svg>
                        </button>

                        <button
                          type="button"
                          onClick={() => move(i, -1)}
                          disabled={i === 0}
                          title="Geser Naik"
                          aria-label={`Naikkan posisi ${template.name}`}
                          className="flex size-6 items-center justify-center rounded border border-border text-muted hover:bg-surface-subtle hover:text-foreground disabled:opacity-30 disabled:pointer-events-none cursor-pointer"
                        >
                          <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M10 17a.75.75 0 01-.75-.75V5.612L5.29 9.77a.75.75 0 01-1.08-1.04l5.25-5.5a.75.75 0 011.08 0l5.25 5.5a.75.75 0 11-1.08 1.04l-3.96-4.158V16.25A.75.75 0 0110 17z" clipRule="evenodd" />
                          </svg>
                        </button>
                        <button
                          type="button"
                          onClick={() => move(i, 1)}
                          disabled={i === selected.length - 1}
                          title="Geser Turun"
                          aria-label={`Turunkan posisi ${template.name}`}
                          className="flex size-6 items-center justify-center rounded border border-border text-muted hover:bg-surface-subtle hover:text-foreground disabled:opacity-30 disabled:pointer-events-none cursor-pointer"
                        >
                          <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M10 3a.75.75 0 01.75.75v10.638l3.96-4.158a.75.75 0 111.08 1.04l-5.25 5.5a.75.75 0 01-1.08 0l-5.25-5.5a.75.75 0 111.08-1.04l3.96 4.158V3.75A.75.75 0 0110 3z" clipRule="evenodd" />
                          </svg>
                        </button>
                        <button
                          type="button"
                          onClick={() => remove(template.id)}
                          title="Hapus dari Beranda"
                          aria-label={`Hapus ${template.name}`}
                          className="flex size-6 items-center justify-center rounded border border-rose-200 text-rose-600 hover:bg-rose-50 hover:text-rose-700 cursor-pointer dark:border-rose-900 dark:hover:bg-rose-950/40"
                        >
                          <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
                          </svg>
                        </button>
                      </div>
                    </li>
                  );
                })}
              </ol>
            )}

            {/* Tombol Simpan Urutan */}
            <button
              type="button"
              onClick={save}
              disabled={pending}
              className="btn-primary w-full py-2.5 text-xs font-bold cursor-pointer"
            >
              {pending ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="size-3 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Menyimpan Perubahan...
                </span>
              ) : (
                "Simpan Template Unggulan"
              )}
            </button>
          </div>
        </div>

        {/* Kolom Kanan: Galeri Template Publik untuk Dipilih */}
        <div className="lg:col-span-7 space-y-4">
          <div className="card p-5 space-y-4">
            <div className="border-b border-border/70 pb-3 flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-foreground">Katalog Template Galeri</h3>
                <p className="text-xs text-muted">Jelajahi template website, uji preview interaktif, dan pilih untuk beranda</p>
              </div>
              <span className="rounded-badge bg-surface-subtle px-2 py-0.5 text-[11px] font-bold text-muted border border-border">
                {filteredGallery.length} Tersedia
              </span>
            </div>

            {/* Filter Kategori Singkat */}
            {categories.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5 pb-1">
                <button
                  type="button"
                  onClick={() => setFilterCategory("all")}
                  className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold transition-colors cursor-pointer ${
                    filterCategory === "all"
                      ? "bg-navy text-white dark:bg-cyan dark:text-navy"
                      : "bg-surface-subtle text-muted hover:text-foreground border border-border/70"
                  }`}
                >
                  Semua Kategori
                </button>
                {categories.map((cat) => (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => setFilterCategory(cat)}
                    className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold uppercase transition-colors cursor-pointer ${
                      filterCategory === cat
                        ? "bg-navy text-white dark:bg-cyan dark:text-navy"
                        : "bg-surface-subtle text-muted hover:text-foreground border border-border/70"
                    }`}
                  >
                    {cat.replace(/_/g, " ")}
                  </button>
                ))}
              </div>
            )}

            {/* Grid Galeri Template */}
            {filteredGallery.length === 0 ? (
              <div className="rounded-xl border border-dashed border-border p-8 text-center text-xs text-muted">
                Tidak ada template yang cocok dengan filter atau kata kunci pencarian.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                {filteredGallery.map((template) => {
                  const thumbnail = templateThumbnailSrc(template);
                  const isSelected = chosenSet.has(template.id);
                  const isFull = selected.length >= MAX_FEATURED && !isSelected;

                  return (
                    <div
                      key={template.id}
                      className={`card overflow-hidden border p-0 flex flex-col justify-between transition-all duration-200 ${
                        isSelected
                          ? "border-primary/60 bg-primary/5 shadow-xs"
                          : "border-border hover:border-primary/40 hover:shadow-sm"
                      }`}
                    >
                      {/* Thumbnail View */}
                      <div className="relative aspect-[16/10] bg-surface-subtle overflow-hidden group">
                        {thumbnail ? (
                          <Image
                            src={thumbnail}
                            alt={template.name}
                            fill
                            unoptimized
                            className="object-cover transition-transform duration-200 group-hover:scale-105"
                          />
                        ) : (
                          <div className="size-full flex items-center justify-center text-xs text-muted">
                            Belum ada thumbnail
                          </div>
                        )}

                        {/* Category Badge Floating */}
                        <span className="absolute top-2 left-2 rounded-badge bg-black/60 backdrop-blur-xs px-2 py-0.5 text-[9px] font-bold uppercase tracking-wider text-white">
                          {template.category.replace(/_/g, " ")}
                        </span>

                        {/* Download badge */}
                        <span className="absolute bottom-2 right-2 rounded-badge bg-white/90 backdrop-blur-xs px-1.5 py-0.5 text-[9px] font-bold text-navy shadow-xs">
                          {template.downloads}x Dipakai
                        </span>

                        {/* Quick Hover Preview Button */}
                        <div className="absolute inset-0 bg-navy/60 backdrop-blur-2xs opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                          <button
                            type="button"
                            onClick={() => setPreviewTemplate(template)}
                            className="rounded-lg bg-cyan px-3 py-1.5 text-xs font-bold text-navy shadow-md hover:bg-cyan/90 transition-transform hover:scale-105 cursor-pointer flex items-center gap-1.5"
                          >
                            <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                              <path d="M10 12.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z" />
                              <path fillRule="evenodd" d="M.664 10.59a1.651 1.651 0 010-1.186A10.004 10.004 0 0110 3c4.257 0 7.893 2.66 9.336 6.41.147.381.146.804 0 1.186A10.004 10.004 0 0110 17c-4.257 0-7.893-2.66-9.336-6.41zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                            </svg>
                            <span>Uji Preview Website</span>
                          </button>
                        </div>
                      </div>

                      {/* Metadata Content */}
                      <div className="p-3.5 space-y-2 flex-1 flex flex-col justify-between">
                        <div>
                          <h4 className="font-bold text-xs text-foreground line-clamp-1 leading-tight">
                            {template.name}
                          </h4>
                          <p className="text-[11px] text-muted line-clamp-1 mt-0.5">
                            Kreator: <span className="font-medium text-foreground">{template.creatorName}</span>
                          </p>
                        </div>

                        <div className="pt-2 border-t border-border/50 flex items-center gap-2">
                          {/* Tombol Pasang / Status */}
                          {isSelected ? (
                            <button
                              type="button"
                              onClick={() => remove(template.id)}
                              className="flex-1 rounded-field border border-emerald-300 bg-emerald-50 py-1.5 px-2 text-xs font-bold text-emerald-700 hover:bg-rose-50 hover:text-rose-700 hover:border-rose-300 transition-colors cursor-pointer dark:bg-emerald-950/40 dark:border-emerald-800 dark:text-emerald-300"
                            >
                              ✓ Terpasang (Klik Hapus)
                            </button>
                          ) : (
                            <button
                              type="button"
                              disabled={isFull}
                              onClick={() => add(template)}
                              className="flex-1 btn-primary py-1.5 px-2 text-xs font-bold cursor-pointer disabled:opacity-40 disabled:pointer-events-none"
                            >
                              {isFull ? "Slot Penuh" : "+ Pajang di Beranda"}
                            </button>
                          )}

                          {/* Tombol Preview Website */}
                          <button
                            type="button"
                            onClick={() => setPreviewTemplate(template)}
                            title="Buka Preview Modal"
                            className="flex size-7 items-center justify-center rounded border border-border text-muted hover:bg-surface-subtle hover:text-foreground shrink-0 cursor-pointer"
                          >
                            <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                              <path d="M10 12.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z" />
                              <path fillRule="evenodd" d="M.664 10.59a1.651 1.651 0 010-1.186A10.004 10.004 0 0110 3c4.257 0 7.893 2.66 9.336 6.41.147.381.146.804 0 1.186A10.004 10.004 0 0110 17c-4.257 0-7.893-2.66-9.336-6.41zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                            </svg>
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* =========================================================================
          LIVE WEBSITE PREVIEW MODAL / DRAWER
         ========================================================================= */}
      {previewTemplate && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-3 sm:p-6 animate-fade-in"
        >
          <div className="relative flex flex-col w-full max-w-6xl h-[90vh] bg-surface rounded-2xl shadow-2xl border border-border overflow-hidden">
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-border px-5 py-3.5 bg-surface-subtle/50 shrink-0">
              <div className="flex items-center gap-3 min-w-0">
                <span className="rounded-badge bg-primary/10 text-primary px-2 py-0.5 text-xs font-bold uppercase tracking-wider">
                  {previewTemplate.category.replace(/_/g, " ")}
                </span>
                <div className="min-w-0">
                  <h3 className="text-sm font-bold text-foreground truncate">
                    {previewTemplate.name}
                  </h3>
                  <p className="text-[11px] text-muted truncate">
                    oleh {previewTemplate.creatorName}
                  </p>
                </div>
              </div>

              {/* Viewport Switcher Controls */}
              <div className="hidden sm:flex items-center gap-1 rounded-lg border border-border bg-surface p-0.5 text-xs">
                <button
                  type="button"
                  onClick={() => setPreviewViewport("desktop")}
                  className={`rounded px-2.5 py-1 text-xs font-semibold transition-all cursor-pointer ${
                    previewViewport === "desktop"
                      ? "bg-navy text-white dark:bg-cyan dark:text-navy shadow-xs"
                      : "text-muted hover:text-foreground"
                  }`}
                >
                  Desktop
                </button>
                <button
                  type="button"
                  onClick={() => setPreviewViewport("tablet")}
                  className={`rounded px-2.5 py-1 text-xs font-semibold transition-all cursor-pointer ${
                    previewViewport === "tablet"
                      ? "bg-navy text-white dark:bg-cyan dark:text-navy shadow-xs"
                      : "text-muted hover:text-foreground"
                  }`}
                >
                  Tablet (768px)
                </button>
                <button
                  type="button"
                  onClick={() => setPreviewViewport("mobile")}
                  className={`rounded px-2.5 py-1 text-xs font-semibold transition-all cursor-pointer ${
                    previewViewport === "mobile"
                      ? "bg-navy text-white dark:bg-cyan dark:text-navy shadow-xs"
                      : "text-muted hover:text-foreground"
                  }`}
                >
                  Mobile (375px)
                </button>
              </div>

              {/* Actions & Close */}
              <div className="flex items-center gap-2">
                <a
                  href={previewTemplate.previewUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hidden md:inline-flex items-center gap-1.5 rounded-field border border-border bg-surface px-2.5 py-1 text-xs font-medium text-muted hover:text-foreground hover:bg-surface-subtle"
                >
                  <span>Buka Tab Baru</span>
                  <svg className="size-3 text-muted/70" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z" />
                    <path d="M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z" />
                  </svg>
                </a>

                <button
                  type="button"
                  onClick={() => setPreviewTemplate(null)}
                  className="flex size-8 items-center justify-center rounded-lg border border-border text-muted hover:bg-surface-subtle hover:text-foreground cursor-pointer"
                  aria-label="Tutup Pratinjau"
                >
                  <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Simulated Browser Address Bar */}
            <div className="flex items-center justify-between border-b border-border/60 bg-slate-100 px-4 py-1.5 text-xs text-muted select-none dark:bg-slate-900">
              <div className="flex items-center gap-1.5">
                <span className="size-2.5 rounded-full bg-rose-400" />
                <span className="size-2.5 rounded-full bg-amber-400" />
                <span className="size-2.5 rounded-full bg-emerald-400" />
              </div>
              <div className="rounded bg-white px-3 py-0.5 text-[11px] font-mono text-slate-600 border border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700 max-w-md truncate">
                {previewTemplate.previewUrl}
              </div>
              <div className="w-10 text-[10px] text-right font-mono text-muted">
                {previewViewport === "desktop" ? "100%" : previewViewport === "tablet" ? "768px" : "375px"}
              </div>
            </div>

            {/* Iframe Viewport Container */}
            <div className="flex-1 bg-slate-200/60 dark:bg-slate-950 p-2 sm:p-4 overflow-auto flex items-center justify-center">
              <div
                className={`h-full bg-white transition-all duration-300 rounded-lg shadow-lg overflow-hidden border border-slate-300 dark:border-slate-800 ${
                  previewViewport === "desktop"
                    ? "w-full"
                    : previewViewport === "tablet"
                    ? "w-[768px]"
                    : "w-[375px]"
                }`}
              >
                <iframe
                  src={previewTemplate.previewUrl}
                  title={`Preview ${previewTemplate.name}`}
                  className="w-full h-full border-0"
                  sandbox="allow-scripts allow-forms"
                />
              </div>
            </div>

            {/* Modal Footer Actions */}
            <div className="border-t border-border px-5 py-3 bg-surface flex items-center justify-between shrink-0">
              <p className="text-xs text-muted hidden sm:block">
                Pratinjau interaktif ter-sandbox langsung dari runtime hosting OrgSpace.
              </p>
              <div className="flex items-center gap-2.5 w-full sm:w-auto justify-end">
                {chosenSet.has(previewTemplate.id) ? (
                  <button
                    type="button"
                    onClick={() => {
                      remove(previewTemplate.id);
                    }}
                    className="rounded-field border border-rose-300 bg-rose-50 px-4 py-2 text-xs font-bold text-rose-700 hover:bg-rose-100 transition-colors cursor-pointer"
                  >
                    Hapus dari Beranda
                  </button>
                ) : (
                  <button
                    type="button"
                    disabled={selected.length >= MAX_FEATURED}
                    onClick={() => {
                      add(previewTemplate);
                    }}
                    className="btn-primary px-4 py-2 text-xs font-bold cursor-pointer disabled:opacity-40"
                  >
                    {selected.length >= MAX_FEATURED ? "Slot Beranda Penuh" : "+ Pajang Template Ini di Beranda"}
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => setPreviewTemplate(null)}
                  className="btn-outline px-4 py-2 text-xs font-semibold cursor-pointer"
                >
                  Tutup
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
