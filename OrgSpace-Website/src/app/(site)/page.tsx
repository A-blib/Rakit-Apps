import Link from "next/link";
import { getOverview } from "@/lib/api/public";
import type { PublicOverview } from "@/lib/api/types";
import { formatNumber } from "@/lib/format";

/**
 * Landing page OrgSpace — Clean Enterprise / Modern Corporate.
 */
const FEATURES = [
  {
    num: "01",
    title: "Katalog Template Khusus Organisasi",
    body: "Pilihan template terstruktur untuk profil OSIS, BEM, himpunan mahasiswa, UKM, dan kegiatan komunitas karya para kreator.",
  },
  {
    num: "02",
    title: "Kustomisasi Cepat Langsung dari HP",
    body: "Ubah susunan teks, foto dokumentasi, dan tema warna tanpa menyentuh kode. Info kontak dan media sosial terisi otomatis.",
  },
  {
    num: "03",
    title: "Kolaborasi Ruang Kerja Pengurus",
    body: "Kelola halaman bersama seluruh divisi dengan pembagian peran terstruktur (Pemilik, Admin, dan Editor) dalam satu akun tim.",
  },
  {
    num: "04",
    title: "Penerbitan Instan & Ekspor Mandiri",
    body: "Publikasikan website secara langsung dengan subdomain organisasi Anda atau unduh paket file HTML/CSS bersih siap hosting.",
  },
];

export default async function LandingPage() {
  let overview: PublicOverview | null = null;
  try {
    overview = await getOverview();
  } catch {
    // Landing tetap tampil normal bila backend sedang tidak aktif
  }

  return (
    <div className="space-y-16 sm:space-y-24 py-12 sm:py-20">
      {/* Hero Section */}
      <section className="mx-auto max-w-6xl px-4 sm:px-6">
        <div className="max-w-3xl">
          <div className="inline-flex items-center gap-2 rounded-full border border-border bg-surface px-3 py-1 text-xs font-semibold text-navy dark:text-cyan shadow-2xs">
            <span className="size-1.5 rounded-full bg-primary" />
            <span>Platform Pembuatan Website Organisasi</span>
          </div>

          <h1 className="mt-5 text-3xl font-extrabold tracking-tight text-navy dark:text-foreground sm:text-5xl lg:text-6xl leading-[1.12]">
            Website organisasimu, <br />
            siap terbit langsung dari HP.
          </h1>

          <p className="mt-5 text-base sm:text-lg text-muted leading-relaxed max-w-2xl">
            OrgSpace memudahkan OSIS, BEM, UKM, dan komunitas membangun website resmi tanpa ribet koding. Pilih template terverifikasi, sesuaikan konten bersama rekan pengurus, dan online dalam sekejap.
          </p>

          <div className="mt-8 flex flex-wrap items-center gap-3">
            {overview?.androidDownloadUrl ? (
              <a className="btn-primary h-11 px-5 text-sm" href={overview.androidDownloadUrl}>
                Unduh App Android
              </a>
            ) : (
              <span className="btn-primary h-11 px-5 text-sm opacity-70 cursor-not-allowed">
                Aplikasi Android Segera Rilis
              </span>
            )}
            <Link className="btn-outline h-11 px-5 text-sm" href="/docs">
              Pelajari Panduan
            </Link>
          </div>
        </div>
      </section>

      {/* Metrics Section */}
      {overview && (
        <section aria-label="Statistik Platform OrgSpace" className="mx-auto max-w-6xl px-4 sm:px-6">
          <div className="grid grid-cols-2 gap-3.5 sm:grid-cols-4">
            {[
              ["Organisasi Terdaftar", overview.stats.organizations],
              ["Website Diterbitkan", overview.stats.websitesOnline],
              ["Pilihan Template", overview.stats.templates],
              ["Kreator Template", overview.stats.providers],
            ].map(([label, value]) => (
              <div key={label} className="card p-5">
                <p className="text-2xl sm:text-3xl font-bold font-mono tracking-tight text-navy dark:text-foreground">
                  {formatNumber(value as number)}
                </p>
                <p className="mt-1 text-xs font-medium text-muted uppercase tracking-wider">{label}</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Features Grid */}
      <section id="fitur" className="mx-auto max-w-6xl px-4 sm:px-6 scroll-mt-20">
        <div className="border-b border-border pb-4">
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">Fitur Unggulan</p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-navy dark:text-foreground sm:text-3xl">
            Solusi Lengkap Kebutuhan Digital Organisasi
          </h2>
        </div>

        <div className="mt-8 grid gap-4 sm:grid-cols-2">
          {FEATURES.map((feature) => (
            <div key={feature.title} className="card p-6 flex flex-col justify-between">
              <div>
                <span className="text-xs font-mono font-bold text-primary">{feature.num}</span>
                <h3 className="mt-2 text-base font-semibold text-foreground">
                  {feature.title}
                </h3>
                <p className="mt-2 text-xs sm:text-sm text-muted leading-relaxed">
                  {feature.body}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Enterprise CTA Card */}
      <section className="mx-auto max-w-6xl px-4 sm:px-6">
        <div className="rounded-card border border-navy/20 bg-navy p-8 sm:p-12 text-white shadow-xs">
          <div className="max-w-2xl">
            <span className="inline-block rounded px-2 py-0.5 text-[11px] font-bold uppercase tracking-wider bg-white/10 text-cyan">
              Mulai Sekarang
            </span>
            <h2 className="mt-3 text-2xl sm:text-3xl font-bold tracking-tight">
              Siap mempublikasikan profil organisasimu?
            </h2>
            <p className="mt-2.5 text-sm sm:text-base text-white/80 leading-relaxed">
              Pelajari langkah awal pembuatan situs atau masuk ke ruang dashboard manajemen untuk melihat kinerja terkini.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link href="/docs/memulai-dari-hp" className="btn bg-white text-navy font-semibold hover:bg-white/90">
                Baca Panduan Memulai
              </Link>
              <Link href="/dashboard" className="btn border border-white/30 text-white hover:bg-white/10">
                Masuk Dashboard Tim
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
