import Link from "next/link";
import type { Metadata } from "next";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";

export const metadata: Metadata = { title: "Organisasi & Website" };

interface OrgWebsiteItem {
  id: string;
  orgName: string;
  websiteTitle: string;
  subdomain: string;
  status: "live" | "draft" | "inactive";
  statusText: string;
  storageMb: number;
  maxStorageMb: number;
  ownerEmail: string;
  updatedAt: string;
}

export default async function WebsitesManagementPage({
  searchParams,
}: {
  searchParams: Promise<{ status?: string; q?: string }>;
}) {
  const query = await searchParams;
  const statusFilter =
    query.status === "live" || query.status === "draft" || query.status === "inactive"
      ? query.status
      : "all";
  const q = query.q?.toLowerCase() || "";

  const token = await adminToken();
  const overview = await adminApi.overview(token, "30d");

  // Data representatif website & organisasi platform OrgSpace
  const allWebsites: OrgWebsiteItem[] = [
    {
      id: "org-site-1",
      orgName: "OSIS SMA Negeri 1 Teladan",
      websiteTitle: "Portal Resmi OSIS Teladan",
      subdomain: "osis-teladan.orgspace.app",
      status: "live",
      statusText: "Live (Diterbitkan)",
      storageMb: 42,
      maxStorageMb: 300,
      ownerEmail: "osis.teladan@school.test",
      updatedAt: "22 Okt 2026",
    },
    {
      id: "org-site-2",
      orgName: "BEM Fakultas Ilmu Komputer",
      websiteTitle: "Pusat Aspirasi & Informasi Fasilkom",
      subdomain: "bem-fasilkom.orgspace.app",
      status: "live",
      statusText: "Live (Diterbitkan)",
      storageMb: 118,
      maxStorageMb: 300,
      ownerEmail: "humas.bem@univ.test",
      updatedAt: "20 Okt 2026",
    },
    {
      id: "org-site-3",
      orgName: "UKM Paduan Suara Mahasiswa Harmoni",
      websiteTitle: "Harmoni Choir Showcase",
      subdomain: "harmoni-choir.orgspace.app",
      status: "draft",
      statusText: "Draft (Proses Edit)",
      storageMb: 85,
      maxStorageMb: 300,
      ownerEmail: "padus.harmoni@student.test",
      updatedAt: "18 Okt 2026",
    },
    {
      id: "org-site-4",
      orgName: "Komunitas Pengembang Terbuka Nusantara",
      websiteTitle: "Nusantara Open Tech Portal",
      subdomain: "open-nusantara.orgspace.app",
      status: "draft",
      statusText: "Draft (Proses Edit)",
      storageMb: 14,
      maxStorageMb: 300,
      ownerEmail: "dev@nusantara.test",
      updatedAt: "15 Okt 2026",
    },
    {
      id: "org-site-5",
      orgName: "Himpunan Mahasiswa Biologi Eksplorasi",
      websiteTitle: "Bio Research Hub",
      subdomain: "bio-eksplorasi.orgspace.app",
      status: "inactive",
      statusText: "Nonaktif / Limit Penuh",
      storageMb: 298,
      maxStorageMb: 300,
      ownerEmail: "hima.bio@kampus.test",
      updatedAt: "05 Okt 2026",
    },
  ];

  const filteredItems = allWebsites.filter((item) => {
    const matchesStatus = statusFilter === "all" || item.status === statusFilter;
    const matchesSearch =
      !q ||
      item.orgName.toLowerCase().includes(q) ||
      item.websiteTitle.toLowerCase().includes(q) ||
      item.subdomain.toLowerCase().includes(q);
    return matchesStatus && matchesSearch;
  });

  const hrefForStatus = (val: string) => {
    const params = new URLSearchParams();
    if (val !== "all") params.set("status", val);
    if (q) params.set("q", q);
    return `?${params.toString()}`;
  };

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header Halaman */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
              Manajemen Organisasi & Website
            </h1>
            <span className="rounded-badge bg-primary-container px-2 py-0.5 text-xs font-bold font-mono text-on-primary-container">
              {overview.totals.organizations} organisasi &bull; {overview.totals.websites} website
            </span>
          </div>
        </div>
      </div>

      {/* Ringkasan Cepat */}
      <section className="grid grid-cols-1 gap-3.5 sm:grid-cols-3">
        <div className="card p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-muted font-medium">Website Live (Online)</p>
            <p className="text-2xl font-bold font-mono text-emerald-600 dark:text-emerald-400 mt-1">
              {formatNumber(overview.totals.sitesOnline)}
            </p>
          </div>
          <span className="flex size-9 items-center justify-center rounded-xl bg-emerald-500/10 text-emerald-600">
            <span className="size-2.5 rounded-full bg-emerald-500 animate-pulse" />
          </span>
        </div>

        <div className="card p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-muted font-medium">Total Proyek Website</p>
            <p className="text-2xl font-bold font-mono text-foreground mt-1">
              {formatNumber(overview.totals.websites)}
            </p>
          </div>
          <span className="flex size-9 items-center justify-center rounded-xl bg-primary-container text-on-primary-container">
            <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4.25 2A2.25 2.25 0 002 4.25v11.5A2.25 2.25 0 004.25 18h11.5A2.25 2.25 0 0018 15.75V4.25A2.25 2.25 0 0015.75 2H4.25zm4.03 6.28a.75.75 0 00-1.06-1.06L4.97 9.47a.75.75 0 000 1.06l2.25 2.25a.75.75 0 001.06-1.06L6.56 10l1.72-1.72zm3.44-1.06a.75.75 0 111.06 1.06L11.06 10l1.72 1.72a.75.75 0 11-1.06 1.06l-2.25-2.25a.75.75 0 010-1.06l2.25-2.25z" clipRule="evenodd" />
            </svg>
          </span>
        </div>

        <div className="card p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-muted font-medium">Organisasi Terdaftar</p>
            <p className="text-2xl font-bold font-mono text-foreground mt-1">
              {formatNumber(overview.totals.organizations)}
            </p>
          </div>
          <span className="flex size-9 items-center justify-center rounded-xl bg-surface-subtle text-muted">
            <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4zm3 1h2v2H7V5zm4 0h2v2h-2V5zm-4 4h2v2H7V9zm4 0h2v2h-2V9zm-4 4h2v2H7v-2zm4 0h2v2h-2v-2z" clipRule="evenodd" />
            </svg>
          </span>
        </div>
      </section>

      {/* Filter Bar: Semua | Live (Diterbitkan) | Draft (Proses Edit) | Nonaktif */}
      <div className="space-y-3">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <nav
            className="inline-flex rounded-xl border border-border bg-surface-subtle p-1 gap-1"
            aria-label="Filter Status Website"
          >
            <Link
              href={hrefForStatus("all")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                statusFilter === "all"
                  ? "bg-surface font-semibold text-foreground border border-border"
                  : "text-muted hover:text-foreground"
              }`}
            >
              Semua
            </Link>
            <Link
              href={hrefForStatus("live")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                statusFilter === "live"
                  ? "bg-surface font-semibold text-emerald-700 dark:text-emerald-400 border border-border"
                  : "text-muted hover:text-emerald-600"
              }`}
            >
              <span className="size-2 rounded-full bg-emerald-500" />
              <span>Live (Diterbitkan)</span>
            </Link>
            <Link
              href={hrefForStatus("draft")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                statusFilter === "draft"
                  ? "bg-surface font-semibold text-muted border border-border"
                  : "text-muted hover:text-foreground"
              }`}
            >
              <span className="size-2 rounded-full bg-slate-400" />
              <span>Draft (Proses Edit)</span>
            </Link>
            <Link
              href={hrefForStatus("inactive")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                statusFilter === "inactive"
                  ? "bg-surface font-semibold text-rose-700 dark:text-rose-400 border border-border"
                  : "text-muted hover:text-rose-600"
              }`}
            >
              <span className="size-2 rounded-full bg-rose-500" />
              <span>Nonaktif / Limit Penuh</span>
            </Link>
          </nav>

          <span className="text-[11px] font-mono text-muted">
            Menampilkan <strong className="text-foreground">{filteredItems.length}</strong> entri
          </span>
        </div>

        {/* Toolbar Pencarian */}
        <form className="flex items-center gap-2 max-w-sm">
          <div className="relative flex-1">
            <input
              className="input pl-9 text-xs"
              name="q"
              defaultValue={q}
              placeholder="Cari organisasi atau subdomain..."
            />
            <svg
              className="pointer-events-none absolute left-3 top-3 size-4 text-muted/70"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          {statusFilter !== "all" && <input type="hidden" name="status" value={statusFilter} />}
          <button type="submit" className="btn-outline h-10 px-3.5 text-xs font-medium cursor-pointer">
            Cari
          </button>
        </form>
      </div>

      {/* Tabel Organisasi & Website */}
      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Organisasi & Judul Website</th>
                <th>Subdomain Akses</th>
                <th>Status Publikasi</th>
                <th>Kapasitas Storage</th>
                <th>Kontak Pengelola</th>
                <th>Terakhir Diubah</th>
                <th className="text-right">Aksi</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-xs text-muted">
                    Tidak ada organisasi atau website yang cocok dengan filter saat ini.
                  </td>
                </tr>
              ) : (
                filteredItems.map((item) => {
                  const pct = Math.round((item.storageMb / item.maxStorageMb) * 100);
                  const isNearLimit = pct >= 90;

                  return (
                    <tr key={item.id}>
                      {/* Organisasi & Judul */}
                      <td>
                        <div>
                          <p className="font-semibold text-foreground text-xs leading-tight">
                            {item.orgName}
                          </p>
                          <p className="text-[11px] text-muted leading-tight mt-0.5">
                            {item.websiteTitle}
                          </p>
                        </div>
                      </td>

                      {/* Subdomain */}
                      <td>
                        <span className="font-mono text-xs text-primary dark:text-cyan">
                          {item.subdomain}
                        </span>
                      </td>

                      {/* Status di Tabel Sesuai Panduan Desain:
                          Hijau (Live) | Abu-abu (Draft) | Merah/Kuning (Nonaktif / Limit) */}
                      <td>
                        {item.status === "live" && (
                          <span className="badge-success text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-emerald-500" />
                            Live (Diterbitkan)
                          </span>
                        )}
                        {item.status === "draft" && (
                          <span className="badge-neutral text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-slate-400" />
                            Draft (Proses Edit)
                          </span>
                        )}
                        {item.status === "inactive" && (
                          <span className="badge-error text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-rose-500" />
                            Nonaktif (Limit Penuh)
                          </span>
                        )}
                      </td>

                      {/* Kapasitas Storage */}
                      <td>
                        <div className="w-28 space-y-1">
                          <div className="flex justify-between text-[10px] font-mono text-muted">
                            <span>{item.storageMb} MB</span>
                            <span>{item.maxStorageMb} MB</span>
                          </div>
                          <div className="h-1.5 w-full bg-surface-subtle rounded-full overflow-hidden">
                            <div
                              style={{ width: `${pct}%` }}
                              className={`h-full ${
                                isNearLimit ? "bg-rose-500" : pct > 60 ? "bg-amber-500" : "bg-primary"
                              }`}
                            />
                          </div>
                        </div>
                      </td>

                      {/* Kontak */}
                      <td className="text-xs font-mono text-muted">
                        {item.ownerEmail}
                      </td>

                      {/* Terakhir Diubah */}
                      <td className="text-xs text-muted whitespace-nowrap">
                        {item.updatedAt}
                      </td>

                      {/* Aksi */}
                      <td className="text-right whitespace-nowrap">
                        <span className="inline-flex items-center gap-1 text-[11px] font-medium text-primary hover:underline cursor-pointer">
                          <span>Pratinjau</span>
                          <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                            <path
                              fillRule="evenodd"
                              d="M4.25 5.5a.75.75 0 00-.75.75v8.5c0 .414.336.75.75.75h8.5a.75.75 0 00.75-.75v-4a.75.75 0 011.5 0v4A2.25 2.25 0 0112.75 17h-8.5A2.25 2.25 0 012 14.75v-8.5A2.25 2.25 0 014.25 4h4a.75.75 0 010 1.5h-4z"
                              clipRule="evenodd"
                            />
                            <path
                              fillRule="evenodd"
                              d="M6.194 12.753a.75.75 0 001.06.053L16.5 4.44v2.81a.75.75 0 001.5 0v-4.5a.75.75 0 00-.75-.75h-4.5a.75.75 0 000 1.5h2.553l-9.056 8.194a.75.75 0 00-.053 1.06z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </span>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
