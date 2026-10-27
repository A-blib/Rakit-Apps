import type {
  DocCategoryValue,
  FeedbackCategory,
  FeedbackStatus,
  GalleryTemplate,
  LandingHero,
  PortalUpdate,
  PortalUpdateCategory,
  SenderRole,
} from "@/lib/api/types";

/** Label Bahasa Indonesia untuk nilai API. Satu tempat, agar semua halaman memakai kata yang sama. */

export const FEEDBACK_STATUS_LABEL: Record<FeedbackStatus, string> = {
  new: "Baru",
  in_progress: "Diproses",
  resolved: "Selesai",
  closed: "Ditutup",
};

export const FEEDBACK_CATEGORY_LABEL: Record<FeedbackCategory, string> = {
  bug: "Bug",
  saran: "Saran",
  pertanyaan: "Pertanyaan",
  keluhan: "Keluhan",
  lainnya: "Lainnya",
};

export const SENDER_ROLE_LABEL: Record<SenderRole, string> = {
  creator: "Pembuat website",
  provider: "Penyedia template",
};

export const DOC_CATEGORY_LABEL: Record<DocCategoryValue, string> = {
  mulai: "Memulai",
  "ruang-kerja": "Ruang kerja & anggota",
  website: "Membuat website",
  template: "Template",
  terbit: "Menerbitkan website",
  provider: "Penyedia template",
  akun: "Akun & keamanan",
  faq: "Tanya jawab",
};

export const UPDATE_CATEGORY_LABEL: Record<PortalUpdateCategory, string> = {
  fitur: "Fitur baru",
  optimasi: "Optimasi",
  desain: "Desain ulang",
  lainnya: "Lainnya",
};

const number = new Intl.NumberFormat("id-ID");
const dateTime = new Intl.DateTimeFormat("id-ID", { dateStyle: "medium", timeStyle: "short", timeZone: "Asia/Jakarta" });
const date = new Intl.DateTimeFormat("id-ID", { day: "numeric", month: "short", timeZone: "UTC" });

export const formatNumber = (value: number) => number.format(value);
export const formatDateTime = (iso: string) => dateTime.format(new Date(iso));
/** Tanggal "2026-10-12" → "12 Okt". */
export const formatDay = (isoDate: string) => date.format(new Date(`${isoDate}T00:00:00Z`));

/** Persentase perubahan untuk kartu statistik; null jika periode sebelumnya 0. */
export function percentChange(current: number, previous: number): number | null {
  return previous === 0 ? null : Math.round(((current - previous) / previous) * 100);
}

/** Gambar hero: unggahan admin (lewat rute /hero-image, ?v= agar cache browser ikut berganti) atau gambar bawaan. */
export const heroImageSrc = (hero: LandingHero) =>
  hero.imageUpdatedAt ? `/hero-image?v=${Date.parse(hero.imageUpdatedAt)}` : "/images/orgspace-workspace-hero.png";

/** Thumbnail template lewat rute /template-thumbnail (browser tidak memanggil backend); null = belum ada thumbnail. */
export const templateThumbnailSrc = (template: GalleryTemplate) =>
  template.thumbnailUrl
    ? `/template-thumbnail/${template.id}${new URL(template.thumbnailUrl, "http://x").search}`
    : null;

/** Gambar info terbaru lewat rute /update-image (hanya yang tayang); null = tanpa gambar. */
export const updateImageSrc = (update: PortalUpdate) =>
  update.imageUpdatedAt ? `/update-image/${update.id}?v=${Date.parse(update.imageUpdatedAt)}` : null;

const longDate = new Intl.DateTimeFormat("id-ID", { day: "numeric", month: "long", year: "numeric", timeZone: "Asia/Jakarta" });
/** "2026-10-27T..." → "27 Oktober 2026". */
export const formatDate = (iso: string) => longDate.format(new Date(iso));
