import Image from "next/image";
import type { PortalUpdate, PortalUpdateCategory } from "@/lib/api/types";
import { UPDATE_CATEGORY_LABEL, formatDate, updateImageSrc } from "@/lib/format";

/**
 * Data fallback contoh jika tabel portal_updates di backend belum memiliki berita tayang.
 * Memastikan section berita tetap tampil memukau dan informatif bagi pengunjung landing page.
 */
const FALLBACK_UPDATES: PortalUpdate[] = [
  {
    id: "bfb82e06-a359-47f0-b979-5a9cd10baa7e",
    category: "fitur",
    status: "published",
    title: "Pratinjau Interaktif Template Langsung di Portal",
    summary:
      "Kini pengurus organisasi dapat mencoba dan menjelajahi template website secara live sebelum menerbitkan ke publik.",
    content: null,
    imageAlt: "Pratinjau Template Interaktif",
    imageUpdatedAt: null,
    publishedAt: "2026-10-27T20:46:27Z",
    displayDays: null,
    expiresAt: null,
    expired: false,
    updatedAt: "2026-10-27T20:46:27Z",
  },
  {
    id: "d2b2de5d-1d9c-48dc-b736-ed8c893a1be9",
    category: "desain",
    status: "published",
    title: "Pembaruan Antarmuka & Sistem Navigasi OrgSpace",
    summary:
      "Tampilan dashboard dan website builder kini lebih responsif, cepat, dan nyaman digunakan langsung dari smartphone.",
    content: null,
    imageAlt: "Desain Baru OrgSpace",
    imageUpdatedAt: null,
    publishedAt: "2026-10-28T14:20:00Z",
    displayDays: null,
    expiresAt: null,
    expired: false,
    updatedAt: "2026-10-28T14:20:00Z",
  },
  {
    id: "e4f5a6b7-8c9d-0e1f-2a3b-4c5d6e7f8a9b",
    category: "optimasi",
    status: "published",
    title: "Peningkatan Kecepatan Hosting & Sertifikat SSL Otomatis",
    summary:
      "Setiap website organisasi yang diterbitkan otomatis dilengkapi kompresi aset cepat dan sertifikat keamanan SSL gratis.",
    content: null,
    imageAlt: "Kecepatan Hosting OrgSpace",
    imageUpdatedAt: null,
    publishedAt: "2026-11-01T09:15:00Z",
    displayDays: null,
    expiresAt: null,
    expired: false,
    updatedAt: "2026-11-01T09:15:00Z",
  },
];

/**
 * Pemetaan warna badge kategori berita agar harmonis dengan palet desain OrgSpace
 */
function getCategoryPill(category: PortalUpdateCategory) {
  switch (category) {
    case "fitur":
      return "bg-blue-50 dark:bg-blue-950/60 text-[#0070E0] dark:text-[#00C9F2] border-blue-200/80 dark:border-blue-800/60";
    case "optimasi":
      return "bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border-emerald-200/80 dark:border-emerald-800/60";
    case "desain":
      return "bg-purple-50 dark:bg-purple-950/60 text-purple-700 dark:text-purple-300 border-purple-200/80 dark:border-purple-800/60";
    default:
      return "bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-200/80 dark:border-slate-700";
  }
}

/**
 * Placeholder grafis modern jika berita belum memiliki gambar banner
 */
function NewsGraphicPlaceholder({ category }: { category: PortalUpdateCategory }) {
  if (category === "fitur") {
    return (
      <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-[#EBF5FF] via-[#DBEAFE] to-[#BFDBFE] dark:from-[#0B1E36] dark:via-[#0F2D54] dark:to-[#173D6E] p-6 text-[#0070E0] dark:text-[#00C9F2]">
        <div className="flex flex-col items-center gap-2">
          <div className="size-14 rounded-2xl bg-white/90 dark:bg-[#07152B]/90 p-3 shadow-md shadow-blue-500/10 flex items-center justify-center">
            <svg className="size-8 text-[#0084FF] dark:text-[#00C9F2]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
          </div>
          <span className="text-[11px] font-bold uppercase tracking-wider text-[#0070E0] dark:text-[#00C9F2]">Fitur Baru</span>
        </div>
      </div>
    );
  }

  if (category === "desain") {
    return (
      <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-[#F5F3FF] via-[#EDE9FE] to-[#DDD6FE] dark:from-[#1E1138] dark:via-[#2E1854] dark:to-[#3E2270] p-6 text-purple-700 dark:text-purple-300">
        <div className="flex flex-col items-center gap-2">
          <div className="size-14 rounded-2xl bg-white/90 dark:bg-[#07152B]/90 p-3 shadow-md shadow-purple-500/10 flex items-center justify-center">
            <svg className="size-8 text-purple-600 dark:text-purple-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c3.31 0 6-2.69 6-6 0-4.97-4.48-9-10-9z" />
              <circle cx="7.5" cy="11.5" r="1.5" fill="currentColor" />
              <circle cx="10.5" cy="7.5" r="1.5" fill="currentColor" />
              <circle cx="14.5" cy="7.5" r="1.5" fill="currentColor" />
            </svg>
          </div>
          <span className="text-[11px] font-bold uppercase tracking-wider text-purple-700 dark:text-purple-300">Desain Antarmuka</span>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-[#ECFDF5] via-[#D1FAE5] to-[#A7F3D0] dark:from-[#07241A] dark:via-[#0E3D2D] dark:to-[#14523D] p-6 text-emerald-700 dark:text-emerald-300">
      <div className="flex flex-col items-center gap-2">
        <div className="size-14 rounded-2xl bg-white/90 dark:bg-[#07152B]/90 p-3 shadow-md shadow-emerald-500/10 flex items-center justify-center">
          <svg className="size-8 text-emerald-600 dark:text-emerald-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
          </svg>
        </div>
        <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-300">Optimasi Sistem</span>
      </div>
    </div>
  );
}

/**
 * Section "Berita terbaru dari kami".
 * Menampilkan kartu artikel berita yang rapi, modern, dan selaras dengan tema landing page OrgSpace.
 */
export function LatestUpdates({ updates }: { updates: PortalUpdate[] }) {
  const displayUpdates = updates.length > 0 ? updates : FALLBACK_UPDATES;

  return (
    <section
      id="berita-terbaru"
      className="relative w-full overflow-hidden bg-gradient-to-b from-[#FFFFFF] via-[#F8FAFC] to-[#FFFFFF] dark:from-[#07152B] dark:via-[#091D3A] dark:to-[#07152B] py-16 sm:py-24 lg:py-28 text-[#0B1E36] dark:text-white transition-colors duration-300"
      aria-labelledby="berita-terbaru-heading"
    >
      <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Header Section */}
        <div className="flex flex-col items-center text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 rounded-full border border-blue-200/90 dark:border-blue-800/80 bg-blue-50/80 dark:bg-blue-950/60 px-4 py-1.5 text-xs font-bold text-[#0070E0] dark:text-[#00C9F2] shadow-xs">
            <span className="relative flex size-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-[#0099FF] opacity-75" />
              <span className="relative inline-flex size-2 rounded-full bg-[#0099FF]" />
            </span>
            Kabar &amp; Pembaruan
          </div>

          <h2
            id="berita-terbaru-heading"
            className="mt-4 text-3xl font-black tracking-tight text-[#0B1E36] dark:text-white sm:text-4xl lg:text-[44px] leading-tight"
          >
            Berita terbaru dari kami
          </h2>

          <p className="mt-4 text-base sm:text-lg text-[#475569] dark:text-slate-300 leading-relaxed">
            Ikuti rilis fitur terbaru, tips optimasi website organisasi, dan kabar pengembangan ekosistem OrgSpace.
          </p>
        </div>

        {/* Grid Kartu Berita */}
        <div className="mt-12 sm:mt-16 grid gap-6 sm:gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {displayUpdates.map((update) => {
            const image = updateImageSrc(update);
            const categoryPillClass = getCategoryPill(update.category);

            return (
              <article
                key={update.id}
                className="group relative flex flex-col overflow-hidden rounded-2xl border border-slate-200/90 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] shadow-xs transition-all duration-300 hover:-translate-y-1.5 hover:border-[#0099FF]/60 dark:hover:border-[#00C9F2]/60 hover:shadow-xl hover:shadow-blue-500/10 dark:hover:shadow-[#00C9F2]/10"
              >
                {/* Visual Banner / Gambar Berita */}
                <div className="relative aspect-[16/10] w-full overflow-hidden bg-slate-50 dark:bg-slate-900 border-b border-slate-100 dark:border-[#1E3E6B]">
                  {image ? (
                    <Image
                      src={image}
                      alt={update.imageAlt ?? update.title}
                      width={640}
                      height={400}
                      unoptimized
                      className="h-full w-full object-cover transition-transform duration-500 ease-out group-hover:scale-105"
                    />
                  ) : (
                    <NewsGraphicPlaceholder category={update.category} />
                  )}

                  {/* Kategori Badge Melayang */}
                  <div className="absolute top-3.5 left-3.5">
                    <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider shadow-xs backdrop-blur-md ${categoryPillClass}`}>
                      {UPDATE_CATEGORY_LABEL[update.category]}
                    </span>
                  </div>

                  {/* Tanggal Rilis di Pojok Kanan Atas */}
                  {update.publishedAt && (
                    <div className="absolute top-3.5 right-3.5">
                      <span className="inline-flex items-center gap-1 rounded-full bg-white/90 dark:bg-[#07152B]/90 border border-slate-200/80 dark:border-[#1E3E6B] px-2.5 py-1 text-[10px] font-bold text-[#475569] dark:text-slate-300 shadow-xs backdrop-blur-md">
                        <svg className="size-3 text-[#0099FF] dark:text-[#00C9F2]" viewBox="0 0 20 20" fill="currentColor">
                          <path
                            fillRule="evenodd"
                            d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z"
                            clipRule="evenodd"
                          />
                        </svg>
                        {formatDate(update.publishedAt)}
                      </span>
                    </div>
                  )}
                </div>

                {/* Konten & Deskripsi Berita */}
                <div className="flex flex-1 flex-col p-5 sm:p-6 bg-white dark:bg-[#0C2E5B]">
                  {/* Judul Berita */}
                  <h3 className="text-base sm:text-lg font-bold text-[#0B1E36] dark:text-white transition-colors group-hover:text-[#0099FF] dark:group-hover:text-[#00C9F2] leading-snug line-clamp-2">
                    {update.title}
                  </h3>

                  {/* Ringkasan Singkat */}
                  <p className="mt-2.5 flex-1 text-sm text-[#475569] dark:text-slate-300 leading-relaxed line-clamp-3">
                    {update.summary}
                  </p>

                  {/* Tombol Aksi Bawah */}
                  <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-700/60 flex items-center justify-between">
                    <a
                      href={`/info/${update.id}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200/90 dark:border-[#1E3E6B] bg-slate-50/70 dark:bg-[#081B36]/80 py-2.5 text-xs font-bold text-[#0B1E36] dark:text-slate-100 transition-all duration-200 group-hover:border-[#0099FF] dark:group-hover:border-[#00C9F2] group-hover:bg-[#0099FF] dark:group-hover:bg-[#0099FF] group-hover:text-white group-hover:shadow-md group-hover:shadow-blue-500/20"
                      aria-label={`Lihat detail berita: ${update.title} (tab baru)`}
                    >
                      <span>Baca Selengkapnya</span>
                      <svg className="size-3.5 transition-transform group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                        <path
                          fillRule="evenodd"
                          d="M5.22 14.78a.75.75 0 001.06 0l7.22-7.22v5.69a.75.75 0 001.5 0v-7.5a.75.75 0 00-.75-.75h-7.5a.75.75 0 000 1.5h5.69l-7.22 7.22a.75.75 0 000 1.06z"
                          clipRule="evenodd"
                        />
                      </svg>
                    </a>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
}
