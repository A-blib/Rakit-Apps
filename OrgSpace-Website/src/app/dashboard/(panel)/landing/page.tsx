import Link from "next/link";
import Form from "next/form";
import type { Metadata } from "next";
import { Pagination } from "@/components/Pagination";
import { adminApi } from "@/lib/api/admin";
import { getTemplates } from "@/lib/api/public";
import { adminToken } from "@/lib/auth/session";
import { FeaturedPicker } from "./FeaturedPicker";

export const metadata: Metadata = { title: "Kurasi Template Beranda" };

/** Satu wadah galeri = 16 template; admin pindah wadah lewat Pagination. */
const WADAH_SIZE = 16;

export default async function LandingContentPage({ searchParams }: PageProps<"/dashboard/landing">) {
  const params = await searchParams;
  const q = typeof params.q === "string" ? params.q.trim() : "";
  const page = Math.max(0, Number(params.page) || 0);
  const token = await adminToken();

  const [featured, gallery] = await Promise.all([
    adminApi.landingTemplates(token),
    getTemplates({ q: q || undefined, page, size: WADAH_SIZE }),
  ]);

  const hrefFor = (target: number) =>
    `/dashboard/landing?${new URLSearchParams({ ...(q && { q }), page: String(target) })}`;

  return (
    <div className="max-w-7xl space-y-8 animate-fade-in">
      {/* Executive Page Header */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-5">
        <div>
          <div className="flex items-center gap-2 mb-1.5">
            <span className="rounded-badge bg-primary/10 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-primary border border-primary/20">
              Template Unggulan
            </span>
            <span className="text-xs text-muted">·</span>
            <span className="text-xs text-muted">
              {featured.length} dari 12 template terpajang di beranda
            </span>
          </div>
          <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
            Kurasi Template & Website Beranda
          </h1>
          <p className="mt-1 text-xs text-muted max-w-2xl leading-relaxed">
            Pilih dan atur urutan template website yang dipajang pada seksi &ldquo;Template Unggulan&rdquo; di beranda orgspace.com. Anda dapat menguji pratinjau live website secara interaktif sebelum menentukan pilihan.
          </p>
        </div>

        {/* Quick External Link */}
        <div className="flex items-center gap-2.5 shrink-0">
          <Link
            href="/#template-unggulan"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 rounded-field border border-border bg-surface px-3 py-2 text-xs font-semibold text-muted transition-all hover:bg-surface-subtle hover:text-foreground hover:border-primary/50 shadow-2xs"
          >
            <span>Lihat di Beranda Publik</span>
            <svg className="size-3.5 text-muted/70" viewBox="0 0 20 20" fill="currentColor">
              <path d="M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z" />
              <path d="M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z" />
            </svg>
          </Link>
        </div>
      </header>

      {/* Bar Pencarian & Filter Cepat */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 bg-surface p-4 rounded-xl border border-border/80 shadow-2xs">
        <div>
          <h2 className="text-sm font-bold text-foreground">
            Katalog Template & Preview Interaktif
          </h2>
          <p className="text-xs text-muted">
            Klik ikon preview pada kartu untuk mencoba website secara langsung di dalam dashboard
          </p>
        </div>

        {/* Form Pencarian Template */}
        <Form action="/dashboard/landing" className="flex items-center gap-2">
          <input
            className="input text-xs w-64 h-9"
            name="q"
            defaultValue={q}
            placeholder="Cari nama template..."
            aria-label="Cari nama template"
          />
          <button type="submit" className="btn-outline h-9 px-3.5 text-xs font-semibold cursor-pointer">
            Cari
          </button>
          {q && (
            <Link
              href="/dashboard/landing"
              className="text-xs text-muted hover:text-foreground underline px-1"
            >
              Reset
            </Link>
          )}
        </Form>
      </div>

      {/* Komponen Utama Kurasi Template + Live Website Preview Modal */}
      <FeaturedPicker initial={featured} gallery={gallery.items} />

      {/* Pagination Galeri */}
      <div className="pt-2">
        <Pagination page={gallery.page} totalPages={gallery.totalPages} hrefFor={hrefFor} />
      </div>
    </div>
  );
}
