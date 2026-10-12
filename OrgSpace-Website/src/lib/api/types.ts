/**
 * KONTRAK API — bentuk data persis seperti yang dikirim backend Spring Boot (backend/src/main/java/.../dto).
 * File ini fondasi: JANGAN diubah tanpa koordinasi dengan Aris, karena perubahan di sini harus sama dengan backend.
 * Daftar endpoint lengkap: ../OrgSpace-Apps/docs/rancangan/portal-web.md bagian 5, atau Swagger UI http://localhost:8080/swagger-ui.html
 *
 * Catatan tipe: tanggal-waktu = string ISO-8601 UTC ("2026-10-12T08:00:00Z"); tanggal saja = "2026-10-12".
 */

export type Uuid = string;
export type IsoDateTime = string;
export type IsoDate = string;

/** Bentuk error semua endpoint. `code` = konstanta ErrorCode di backend (mis. ADMIN_ONLY). */
export interface ErrorResponse {
  code: string;
  message: string;
  fieldErrors?: Record<string, string> | null;
  details?: Record<string, unknown> | null;
}

/** Daftar bernomor halaman untuk portal web. `page` mulai dari 0. */
export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

// ---------- Auth ----------

export interface UserResponse {
  id: Uuid;
  displayName: string;
  email: string | null;
  avatarUrl: string | null;
  activeMode: "creator" | "provider";
  onboardingCompleted: boolean;
  roles: ("creator" | "provider")[];
  providerStatus: "active" | "suspended" | null;
  lastOrganizationId: Uuid | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  /** Umur access token dalam detik. */
  expiresIn: number;
  isNewUser: boolean;
  user: UserResponse;
}

// ---------- Publik: landing & dokumentasi ----------

export interface PublicOverview {
  stats: { organizations: number; websitesOnline: number; templates: number; providers: number };
  /** null = link unduh app belum diatur; sembunyikan tombol unduh. */
  androidDownloadUrl: string | null;
}

export type DocCategoryValue =
  | "mulai" | "ruang-kerja" | "website" | "template" | "terbit" | "provider" | "akun" | "faq";
export type DocAudience = "all" | "creator" | "provider";
export type DocStatus = "draft" | "published";

export interface DocSummary {
  id: Uuid;
  slug: string;
  category: DocCategoryValue;
  audience: DocAudience;
  title: string;
  summary: string | null;
  sortOrder: number;
  status: DocStatus;
  publishedAt: IsoDateTime | null;
  updatedAt: IsoDateTime;
}

export interface DocDetail extends DocSummary {
  categoryLabel: string;
  /** Markdown (tabel GFM boleh). */
  body: string;
}

export interface DocTree {
  categories: { value: DocCategoryValue; label: string; articles: DocSummary[] }[];
}

/** Body POST/PUT /admin/docs. */
export interface DocRequest {
  slug: string;
  category: DocCategoryValue;
  audience?: DocAudience;
  title: string;
  summary?: string | null;
  body: string;
  sortOrder?: number;
  status?: DocStatus;
}

/** Item galeri template publik (GET /templates). */
export interface GalleryTemplate {
  id: Uuid;
  name: string;
  /** Kategori organisasi, mis. "osis_mpk" (label dari GET /meta/categories). */
  category: string;
  /** Alamat thumbnail apa adanya dari backend (kolom templates.thumbnail_url). */
  thumbnailUrl: string | null;
  creatorName: string;
  downloads: number;
  publishedAt: IsoDateTime;
  contentTags: string[];
}

// ---------- Admin ----------

export interface AdminMe {
  id: Uuid;
  displayName: string;
  email: string | null;
  avatarUrl: string | null;
}

export type StatsPeriod = "7d" | "30d" | "90d";

export interface Change {
  current: number;
  previous: number;
}

export interface Overview {
  from: IsoDate;
  to: IsoDate;
  totals: {
    users: number;
    organizations: number;
    personalSpaces: number;
    websites: number;
    sitesOnline: number;
    templatesPublished: number;
    providers: number;
    feedbackOpen: number;
  };
  period: {
    newUsers: Change;
    activeUsers: Change;
    newOrganizations: Change;
    newWebsites: Change;
    publishes: Change;
    templateDownloads: Change;
    feedback: Change;
    apiRequests: Change;
    apiErrors: Change;
  };
  active: { today: number; last7Days: number; last30Days: number };
}

export type StatsMetric =
  | "signups" | "active_users" | "new_organizations" | "new_websites" | "publishes"
  | "template_views" | "template_downloads" | "feedback" | "api_requests" | "api_errors";

export interface DailyPoint {
  date: IsoDate;
  count: number;
}

export interface Timeseries {
  metric: StatsMetric;
  from: IsoDate;
  to: IsoDate;
  total: number;
  points: DailyPoint[];
}

export interface SignupMethods {
  from: IsoDate;
  to: IsoDate;
  email: number;
  google: number;
  github: number;
}

export interface ActivePlatforms {
  from: IsoDate;
  to: IsoDate;
  android: number;
  web: number;
  other: number;
  total: number;
}

export interface Traffic {
  from: IsoDate;
  to: IsoDate;
  requests: number;
  /** Respons 5xx. */
  errors: number;
  /** Respons 4xx. */
  clientErrors: number;
  days: { date: IsoDate; requests: number; errors: number; clientErrors: number }[];
  topRoutes: {
    method: string;
    route: string;
    requests: number;
    errors: number;
    clientErrors: number;
    avgMs: number;
    maxMs: number;
  }[];
}

export interface AdminUser {
  id: Uuid;
  displayName: string;
  email: string | null;
  avatarUrl: string | null;
  admin: boolean;
  onboardingCompleted: boolean;
  loginMethods: ("local" | "google" | "github")[];
  providerStatus: "active" | "suspended" | null;
  organizations: number;
  websites: number;
  lastActiveDay: IsoDate | null;
  createdAt: IsoDateTime;
}

export interface SystemInfo {
  application: string;
  activeProfiles: string[];
  javaVersion: string;
  startedAt: IsoDateTime;
  uptimeSeconds: number;
  serverTime: IsoDateTime;
  timezone: string;
  databaseOk: boolean;
  schemaVersion: string | null;
}

// ---------- Kritik & saran ----------

export type FeedbackStatus = "new" | "in_progress" | "resolved" | "closed";
export type FeedbackCategory = "bug" | "saran" | "pertanyaan" | "keluhan" | "lainnya";
export type SenderRole = "creator" | "provider";

export interface FeedbackSender {
  id: Uuid;
  displayName: string;
  /** Hanya terisi untuk admin. */
  email: string | null;
}

export interface FeedbackSummary {
  id: Uuid;
  category: FeedbackCategory;
  subject: string;
  status: FeedbackStatus;
  senderRole: SenderRole;
  rating: number | null;
  source: "android" | "web";
  /** Ada pesan baru untuk yang melihat (admin: pesan pengirim belum dibaca tim). */
  unread: boolean;
  messageCount: number;
  sender: FeedbackSender | null;
  organizationName: string | null;
  lastMessageAt: IsoDateTime;
  createdAt: IsoDateTime;
}

export interface FeedbackMessage {
  id: Uuid;
  fromStaff: boolean;
  authorName: string;
  body: string;
  createdAt: IsoDateTime;
}

export interface FeedbackDetail {
  id: Uuid;
  category: FeedbackCategory;
  subject: string;
  status: FeedbackStatus;
  senderRole: SenderRole;
  rating: number | null;
  source: "android" | "web";
  sender: FeedbackSender;
  organizationId: Uuid | null;
  organizationName: string | null;
  appVersion: string | null;
  deviceInfo: string | null;
  /** messages[0] selalu pesan pertama pengirim. */
  messages: FeedbackMessage[];
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface FeedbackCounts {
  all: number;
  newCount: number;
  inProgress: number;
  resolved: number;
  closed: number;
  unread: number;
}
