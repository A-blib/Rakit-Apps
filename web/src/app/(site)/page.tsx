import Link from "next/link";
import { getOverview } from "@/lib/api/public";
import type { PublicOverview } from "@/lib/api/types";
import { formatNumber } from "@/lib/format";

/**
 * Landing page (fondasi). Struktur: Hero + CTA → angka → fitur → ajakan akhir. Teks & tampilan disempurnakan tim
 * desain; data dari GET /public/overview (di-cache 5 menit).
 */
const FEATURES = [
  { title: "Pilih template organisasi", body: "Template OSIS, BEM, UKM, komunitas, dan UMKM buatan para kreator." },
  { title: "Edit langsung dari HP", body: "Ganti teks, foto, dan warna. Nama, logo, dan media sosial terisi otomatis." },
  { title: "Kerja bareng pengurus", body: "Satu ruang kerja untuk semua pengurus, dengan peran Pemilik, Admin, Editor." },
  { title: "Terbitkan dengan alamat sendiri", body: "Website online dengan alamat organisasimu, atau unduh ZIP-nya." },
];

export default async function LandingPage() {
  let overview: PublicOverview | null = null;
  try {
    overview = await getOverview();
  } catch {
    // Landing tetap tampil walau backend sedang mati; angka saja yang disembunyikan.
  }
  return (
    <>
      <section className="mx-auto max-w-6xl px-4 py-20">
        <span className="badge">Tanpa coding</span>
        <h1 className="mt-4 max-w-3xl text-4xl font-extrabold tracking-tight md:text-6xl">
          Website organisasimu, jadi dari HP.
        </h1>
        <p className="mt-4 max-w-2xl text-lg text-muted">
          OrgSpace membantu OSIS, BEM, UKM, dan komunitas membuat website sendiri: pilih template, isi, terbitkan.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          {overview?.androidDownloadUrl ? (
            <a className="btn-primary" href={overview.androidDownloadUrl}>Unduh app Android</a>
          ) : (
            <span className="btn-primary opacity-60" aria-disabled>App Android segera hadir</span>
          )}
          <Link className="btn-outline" href="/docs">Baca panduan</Link>
        </div>
      </section>

      {overview && (
        <section aria-label="OrgSpace dalam angka" className="mx-auto grid max-w-6xl grid-cols-2 gap-4 px-4 md:grid-cols-4">
          {[
            ["Organisasi", overview.stats.organizations],
            ["Website online", overview.stats.websitesOnline],
            ["Template", overview.stats.templates],
            ["Kreator template", overview.stats.providers],
          ].map(([label, value]) => (
            <div key={label} className="card">
              <p className="text-3xl font-bold">{formatNumber(value as number)}</p>
              <p className="text-sm text-muted">{label}</p>
            </div>
          ))}
        </section>
      )}

      <section id="fitur" className="mx-auto max-w-6xl px-4 py-20">
        <h2 className="text-3xl font-bold">Semua yang organisasi butuhkan</h2>
        <div className="mt-8 grid gap-4 md:grid-cols-2">
          {FEATURES.map((feature) => (
            <div key={feature.title} className="card">
              <h3 className="text-lg font-semibold">{feature.title}</h3>
              <p className="mt-2 text-muted">{feature.body}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 pb-20">
        <div className="rounded-card bg-primary p-10 text-on-primary">
          <h2 className="text-2xl font-bold">Siap bikin website organisasimu?</h2>
          <p className="mt-2 opacity-90">Mulai dari panduan singkat, lalu pasang app-nya.</p>
          <Link href="/docs/memulai-dari-hp" className="btn mt-6 bg-surface text-foreground">Mulai sekarang</Link>
        </div>
      </section>
    </>
  );
}
