"use client";

import { useState } from "react";
import Image from "next/image";
import type { GalleryTemplate } from "@/lib/api/types";
import { formatNumber, templateCategoryLabel, templateThumbnailSrc } from "@/lib/format";

/**
 * Data fallback contoh saat tabel landing_templates di backend belum diisi oleh admin.
 * Memastikan landing page tidak kosong dan calon pengguna tetap bisa melihat kualitas desain template OrgSpace.
 */
const FALLBACK_TEMPLATES: GalleryTemplate[] = [
  {
    id: "01111111-1111-1111-1111-111111111111",
    name: "Pramuka Tunas Rimba",
    category: "pramuka",
    thumbnailUrl: null,
    creatorName: "Kwartir Ranting",
    downloads: 148,
    publishedAt: "2026-10-25T10:00:00Z",
    contentTags: ["Profil", "Struktur", "Agenda", "Galeri"],
    previewUrl: "https://orgspace-hosting.my.id/demo/01111111-1111-1111-1111-111111111111/",
  },
  {
    id: "993d9fb6-e22a-4c27-9ee6-dac16d900bba",
    name: "Soundwave Collective",
    category: "seni_olahraga",
    thumbnailUrl: null,
    creatorName: "Echelon Studio",
    downloads: 112,
    publishedAt: "2026-10-26T12:00:00Z",
    contentTags: ["Profil", "Karya", "Kegiatan", "Kontak"],
    previewUrl: "https://orgspace-hosting.my.id/demo/993d9fb6-e22a-4c27-9ee6-dac16d900bba/",
  },
  {
    id: "41899727-2a08-470e-80d1-1e3beaf8da71",
    name: "OSIS & MPK Smansa",
    category: "osis_mpk",
    thumbnailUrl: null,
    creatorName: "Tim Kreatif",
    downloads: 235,
    publishedAt: "2026-10-27T08:00:00Z",
    contentTags: ["Profil", "Proker", "Aspirasi", "Struktur"],
    previewUrl: "https://orgspace-hosting.my.id/demo/41899727-2a08-470e-80d1-1e3beaf8da71/",
  },
  {
    id: "55555555-5555-5555-5555-555555555555",
    name: "BEM Universitas Sinergi",
    category: "bem_dema_dpm",
    thumbnailUrl: null,
    creatorName: "Kementerian Kominfo",
    downloads: 189,
    publishedAt: "2026-10-28T09:00:00Z",
    contentTags: ["Visi-Misi", "Kabinet", "Proker", "Aspirasi"],
    previewUrl: "https://orgspace-hosting.my.id/demo/55555555-5555-5555-5555-555555555555/",
  },
  {
    id: "66666666-6666-6666-6666-666666666666",
    name: "Himpunan Mahasiswa Informatika",
    category: "himpunan",
    thumbnailUrl: null,
    creatorName: "Divisi Medkraf",
    downloads: 142,
    publishedAt: "2026-10-29T11:00:00Z",
    contentTags: ["Profil", "Divisi", "Tutorial", "Event"],
    previewUrl: "https://orgspace-hosting.my.id/demo/66666666-6666-6666-6666-666666666666/",
  },
  {
    id: "77777777-7777-7777-7777-777777777777",
    name: "Pers Mahasiswa & Media Kampus",
    category: "jurnalistik_media",
    thumbnailUrl: null,
    creatorName: "Redaksi Kampus",
    downloads: 98,
    publishedAt: "2026-10-30T14:00:00Z",
    contentTags: ["Berita", "Opini", "Majalah", "Redaksi"],
    previewUrl: "https://orgspace-hosting.my.id/demo/77777777-7777-7777-7777-777777777777/",
  },
  {
    id: "88888888-8888-8888-8888-888888888888",
    name: "Relawan Bumi Lestari",
    category: "seni_olahraga",
    thumbnailUrl: null,
    creatorName: "Komunitas Hijau",
    downloads: 124,
    publishedAt: "2026-10-31T08:30:00Z",
    contentTags: ["Aksi", "Volunter", "Donasi", "Dokumentasi"],
    previewUrl: "https://orgspace-hosting.my.id/demo/88888888-8888-8888-8888-888888888888/",
  },
  {
    id: "99999999-9999-9999-9999-999999999999",
    name: "Paskibra Satya Bhakti",
    category: "pramuka",
    thumbnailUrl: null,
    creatorName: "Korps Paskibra",
    downloads: 87,
    publishedAt: "2026-11-01T10:00:00Z",
    contentTags: ["Profil", "Pasukan", "Prestasi", "Agenda"],
    previewUrl: "https://orgspace-hosting.my.id/demo/99999999-9999-9999-9999-999999999999/",
  },
];

/**
 * Pemetaan warna badge kategori yang selaras dengan palet landing page OrgSpace
 * (Biru Semangat, Hijau Emerald, Ungu Kreatif, Oranye Cerah)
 */
function getCategoryBadgeStyle(category?: string | null): string {
  switch (category) {
    case "pramuka":
      return "bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border-emerald-200/80 dark:border-emerald-800/60";
    case "osis_mpk":
    case "bem_dema_dpm":
    case "himpunan":
      return "bg-blue-50 dark:bg-blue-950/60 text-[#0070E0] dark:text-[#00C9F2] border-blue-200/80 dark:border-blue-800/60";
    case "seni_olahraga":
      return "bg-purple-50 dark:bg-purple-950/60 text-purple-700 dark:text-purple-300 border-purple-200/80 dark:border-purple-800/60";
    case "jurnalistik_media":
      return "bg-amber-50 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300 border-amber-200/80 dark:border-amber-800/60";
    default:
      return "bg-sky-50 dark:bg-sky-950/60 text-sky-700 dark:text-sky-300 border-sky-200/80 dark:border-sky-800/60";
  }
}

/**
 * Section "Website seperti ini yang akan kamu dapatkan".
 * Didesain selaras penuh dengan warna utama landing page (Navy #0B1E36, Biru Semangat #0099FF, Slate #F8FAFC).
 * Secara default menampilkan 6 template awal dengan tombol Show More untuk melihat lebih banyak.
 */
export function FeaturedTemplates({ templates }: { templates: GalleryTemplate[] }) {
  const [showAll, setShowAll] = useState(false);
  const displayTemplates =
    templates.length >= 6
      ? templates
      : [...templates, ...FALLBACK_TEMPLATES.filter((fb) => !templates.some((t) => t.id === fb.id))];
  // Tampilkan 6 kartu secara default di landing page
  const visibleTemplates = showAll ? displayTemplates : displayTemplates.slice(0, 6);

  return (
    <section
      id="template-unggulan"
      className="relative w-full overflow-hidden bg-gradient-to-b from-[#FFFFFF] via-[#F8FAFC] to-[#FFFFFF] dark:from-[#07152B] dark:via-[#091D3A] dark:to-[#07152B] py-16 sm:py-24 lg:py-28 text-[#0B1E36] dark:text-white scroll-mt-20 transition-colors duration-300"
      aria-labelledby="template-unggulan-heading"
    >
      <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Header Section */}
        <div className="flex flex-col items-center text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 rounded-full border border-blue-200/90 dark:border-blue-800/80 bg-blue-50/80 dark:bg-blue-950/60 px-4 py-1.5 text-xs font-bold text-[#0070E0] dark:text-[#00C9F2] shadow-xs">
            <span className="relative flex size-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-[#0099FF] opacity-75" />
              <span className="relative inline-flex size-2 rounded-full bg-[#0099FF]" />
            </span>
            Koleksi Desain Siap Pakai
          </div>

          <h2
            id="template-unggulan-heading"
            className="mt-4 text-3xl font-black tracking-tight text-[#0B1E36] dark:text-white sm:text-4xl lg:text-[44px] leading-tight"
          >
            Website seperti ini yang akan kamu dapatkan
          </h2>

          <p className="mt-4 text-base sm:text-lg text-[#475569] dark:text-slate-300 leading-relaxed">
            Dirancang khusus untuk profil organisasi modern. Pilih template, sesuaikan konten dan foto langsung dari HP,
            lalu terbitkan dalam hitungan menit.
          </p>
        </div>

        {/* Grid Template Cards (Default 6 item) */}
        <div className="mt-12 sm:mt-16 grid gap-6 sm:gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {visibleTemplates.map((template) => {
            const thumbnail = templateThumbnailSrc(template);
            const slug = template.name
              .toLowerCase()
              .replace(/[^a-z0-9]+/g, "-")
              .replace(/^-+|-+$/g, "");
            const badgeStyle = getCategoryBadgeStyle(template.category);

            return (
              <article
                key={template.id}
                className="group relative flex flex-col overflow-hidden rounded-2xl border border-slate-200/90 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] shadow-xs transition-all duration-300 hover:-translate-y-1.5 hover:border-[#0099FF]/60 dark:hover:border-[#00C9F2]/60 hover:shadow-xl hover:shadow-blue-500/10 dark:hover:shadow-[#00C9F2]/10"
              >
                {/* Mockup Browser Window Header */}
                <div className="flex items-center justify-between border-b border-slate-200/80 dark:border-[#1E3E6B] bg-[#F8FAFC] dark:bg-[#081B36] px-4 py-3">
                  {/* Window Controls Dots */}
                  <div className="flex items-center gap-1.5" aria-hidden="true">
                    <span className="size-2.5 rounded-full bg-[#FF5F56]/90" />
                    <span className="size-2.5 rounded-full bg-[#FFBD2E]/90" />
                    <span className="size-2.5 rounded-full bg-[#27C93F]/90" />
                  </div>

                  {/* Browser Address Bar Pill */}
                  <div className="flex items-center gap-1.5 rounded-md border border-slate-200 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] px-3 py-1 text-[11px] font-mono text-[#64748B] dark:text-slate-300 shadow-2xs">
                    <svg className="size-3 text-[#10B981] shrink-0" viewBox="0 0 20 20" fill="currentColor">
                      <path
                        fillRule="evenodd"
                        d="M10 1a4.5 4.5 0 00-4.5 4.5V9H5a2 2 0 00-2 2v6a2 2 0 002 2h10a2 2 0 002-2v-6a2 2 0 00-2-2h-.5V5.5A4.5 4.5 0 0010 1zm3 8V5.5a3 3 0 10-6 0V9h6z"
                        clipRule="evenodd"
                      />
                    </svg>
                    <span className="truncate max-w-[150px] sm:max-w-[180px]">
                      orgspace.app/{slug}
                    </span>
                  </div>

                  {/* Refresh / Menu Icon placeholder */}
                  <div className="size-2.5" />
                </div>

                {/* Preview Thumbnail Container */}
                <div className="relative aspect-16/10 w-full overflow-hidden bg-slate-100 dark:bg-slate-900 border-b border-slate-100 dark:border-[#1E3E6B]">
                  {thumbnail ? (
                    <Image
                      src={thumbnail}
                      alt={`Pratinjau antarmuka template ${template.name}`}
                      fill
                      sizes="(min-width: 1024px) 33vw, (min-width: 640px) 50vw, 100vw"
                      className="object-cover object-top transition-transform duration-500 group-hover:scale-105"
                    />
                  ) : (
                    /* Elegant Geometric Wireframe Fallback */
                    <div className="relative flex h-full w-full flex-col justify-between p-5 bg-gradient-to-br from-[#F8FAFC] via-[#EFF6FF] to-[#E0F2FE] dark:from-[#081B36] dark:via-[#0C2E5B] dark:to-[#102C52]">
                      {/* Wireframe Mockup Navbar */}
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <div className="size-6 rounded-lg bg-[#0099FF]/20 flex items-center justify-center">
                            <span className="size-2.5 rounded-full bg-[#0099FF]" />
                          </div>
                          <div className="h-3 w-16 rounded bg-slate-300/80 dark:bg-slate-600/80" />
                        </div>
                        <div className="flex gap-2">
                          <div className="h-2.5 w-10 rounded bg-slate-200 dark:bg-slate-700" />
                          <div className="h-2.5 w-10 rounded bg-slate-200 dark:bg-slate-700" />
                        </div>
                      </div>

                      {/* Wireframe Hero Body */}
                      <div className="space-y-2 py-4">
                        <div className="h-4 w-3/4 rounded-md bg-[#0B1E36]/70 dark:bg-slate-200/70" />
                        <div className="h-2.5 w-1/2 rounded bg-slate-400/60 dark:bg-slate-500/60" />
                        <div className="pt-2 flex gap-2">
                          <div className="h-6 w-20 rounded-md bg-[#0099FF] shadow-xs" />
                          <div className="h-6 w-16 rounded-md bg-white dark:bg-[#0C2E5B] border border-slate-200 dark:border-[#1E3E6B]" />
                        </div>
                      </div>

                      {/* Wireframe Footer Strip */}
                      <div className="flex items-center justify-between border-t border-slate-200/60 dark:border-slate-700/60 pt-2 text-[10px] text-slate-400 dark:text-slate-400">
                        <span className="font-mono">Responsive Web</span>
                        <span>Full HD 1080p</span>
                      </div>
                    </div>
                  )}

                  {/* Category Pill Tag Overlay (Top-Left) */}
                  <div className="absolute top-3 left-3 z-10">
                    <span
                      className={`inline-flex items-center rounded-lg border px-2.5 py-1 text-xs font-bold shadow-xs backdrop-blur-md ${badgeStyle}`}
                    >
                      {templateCategoryLabel(template.category)}
                    </span>
                  </div>

                  {/* Live Interactive Hover Overlay */}
                  {template.previewUrl && (
                    <div className="absolute inset-0 z-20 flex flex-col items-center justify-center bg-[#0B1E36]/60 backdrop-blur-xs p-4 text-center opacity-0 transition-opacity duration-300 group-hover:opacity-100">
                      <a
                        href={template.previewUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-2 rounded-xl bg-white px-4 py-2.5 text-xs font-bold text-[#0B1E36] shadow-lg transition-transform duration-200 hover:scale-105 hover:bg-[#0099FF] hover:text-white"
                        aria-label={`Buka pratinjau template ${template.name}`}
                      >
                        <span>Lihat Demo Interaktif</span>
                        <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                          <path
                            fillRule="evenodd"
                            d="M5.22 14.78a.75.75 0 001.06 0l7.22-7.22v5.69a.75.75 0 001.5 0v-7.5a.75.75 0 00-.75-.75h-7.5a.75.75 0 000 1.5h5.69l-7.22 7.22a.75.75 0 000 1.06z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </a>
                    </div>
                  )}
                </div>

                {/* Card Content & Metadata */}
                <div className="flex flex-1 flex-col p-5 sm:p-6 bg-white dark:bg-[#0C2E5B]">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <h3 className="text-base font-bold text-[#0B1E36] dark:text-white transition-colors group-hover:text-[#0099FF] dark:group-hover:text-[#00C9F2] truncate">
                        {template.name}
                      </h3>
                      {template.creatorName && (
                        <p className="mt-1 text-xs text-[#64748B] dark:text-slate-300 flex items-center gap-1.5">
                          <svg className="size-3.5 text-[#94A3B8] dark:text-slate-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                            <path
                              fillRule="evenodd"
                              d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                              clipRule="evenodd"
                            />
                          </svg>
                          <span className="truncate">oleh {template.creatorName}</span>
                        </p>
                      )}
                    </div>

                    {/* Download/Usage Count Badge */}
                    <span className="inline-flex items-center gap-1 shrink-0 rounded-lg bg-slate-50 dark:bg-slate-800/80 border border-slate-200/80 dark:border-slate-700/80 px-2 py-1 text-[11px] font-bold text-[#475569] dark:text-slate-300">
                      <svg className="size-3 text-[#0099FF] dark:text-[#00C9F2]" viewBox="0 0 20 20" fill="currentColor">
                        <path
                          fillRule="evenodd"
                          d="M10 3a.75.75 0 01.75.75v10.638l3.96-4.158a.75.75 0 111.08 1.04l-5.25 5.5a.75.75 0 01-1.08 0l-5.25-5.5a.75.75 0 111.08-1.04l3.96 4.158V3.75A.75.75 0 0110 3z"
                          clipRule="evenodd"
                        />
                      </svg>
                      {formatNumber(template.downloads)}
                    </span>
                  </div>

                  {/* Tombol Aksi Bawah */}
                  <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-700/60">
                    {template.previewUrl ? (
                      <a
                        href={template.previewUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200/90 dark:border-[#1E3E6B] bg-slate-50/70 dark:bg-[#081B36]/80 py-2.5 text-xs font-bold text-[#0B1E36] dark:text-slate-100 transition-all duration-200 group-hover:border-[#0099FF] dark:group-hover:border-[#00C9F2] group-hover:bg-[#0099FF] dark:group-hover:bg-[#0099FF] group-hover:text-white group-hover:shadow-md group-hover:shadow-blue-500/20"
                        aria-label={`Buka pratinjau template ${template.name} di tab baru`}
                      >
                        <span>Buka Pratinjau Website</span>
                        <svg className="size-3.5 transition-transform group-hover:translate-x-0.5" viewBox="0 0 20 20" fill="currentColor">
                          <path
                            fillRule="evenodd"
                            d="M5.22 14.78a.75.75 0 001.06 0l7.22-7.22v5.69a.75.75 0 001.5 0v-7.5a.75.75 0 00-.75-.75h-7.5a.75.75 0 000 1.5h5.69l-7.22 7.22a.75.75 0 000 1.06z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </a>
                    ) : (
                      <button disabled className="w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 py-2.5 text-xs font-semibold text-[#94A3B8] cursor-not-allowed">
                        Pratinjau Belum Tersedia
                      </button>
                    )}
                  </div>
                </div>
              </article>
            );
          })}
        </div>

        {/* Tombol Show More / Lihat Lebih Banyak Template */}
        {displayTemplates.length > 6 && (
          <div className="mt-10 sm:mt-12 flex justify-center">
            <button
              type="button"
              onClick={() => setShowAll(!showAll)}
              className="inline-flex items-center gap-2 rounded-full border border-slate-300 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] px-7 py-3 text-xs sm:text-sm font-bold text-[#0B1E36] dark:text-white shadow-xs hover:border-[#0099FF] dark:hover:border-[#00C9F2] hover:text-[#0099FF] dark:hover:text-[#00C9F2] hover:bg-slate-50 dark:hover:bg-[#102C52] transition-all duration-200 cursor-pointer"
            >
              <span>
                {showAll
                  ? "Tampilkan Lebih Sedikit (Show Less)"
                  : `Lihat Lebih Banyak Template (${displayTemplates.length - 6} lainnya)`}
              </span>
              <svg
                className={`size-4 transition-transform duration-200 ${showAll ? "rotate-180" : ""}`}
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </div>
        )}
      </div>
    </section>
  );
}
