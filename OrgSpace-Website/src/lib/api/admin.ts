import { apiFetch } from "./http";
import type {
  ActivePlatforms,
  AdminMe,
  AdminUser,
  DocDetail,
  DocRequest,
  DocSummary,
  FeedbackCategory,
  FeedbackCounts,
  FeedbackDetail,
  FeedbackStatus,
  FeedbackSummary,
  GalleryTemplate,
  LandingHero,
  LandingHeroRequest,
  PortalUpdate,
  PortalUpdateRequest,
  Overview,
  PageResponse,
  SenderRole,
  SignupMethods,
  StatsMetric,
  StatsPeriod,
  SystemInfo,
  Timeseries,
  Traffic,
} from "./types";

/** Endpoint /admin/** (khusus akun is_admin). Semua butuh access token dari sesi (lib/auth/session.ts). */

export const adminApi = {
  me: (token: string) => apiFetch<AdminMe>("/admin/me", { token }),
  system: (token: string) => apiFetch<SystemInfo>("/admin/system", { token }),

  // ---------- monitoring ----------
  overview: (token: string, period: StatsPeriod) =>
    apiFetch<Overview>("/admin/stats/overview", { token, query: { period } }),
  timeseries: (token: string, metric: StatsMetric, period: StatsPeriod) =>
    apiFetch<Timeseries>("/admin/stats/timeseries", { token, query: { metric, period } }),
  signupMethods: (token: string, period: StatsPeriod) =>
    apiFetch<SignupMethods>("/admin/stats/signup-methods", { token, query: { period } }),
  activePlatforms: (token: string, period: StatsPeriod) =>
    apiFetch<ActivePlatforms>("/admin/stats/active-platforms", { token, query: { period } }),
  traffic: (token: string, period: StatsPeriod) =>
    apiFetch<Traffic>("/admin/stats/traffic", { token, query: { period } }),

  // ---------- pengguna ----------
  users: (token: string, params: { q?: string; role?: "creator" | "provider" | "admin"; page?: number; size?: number }) =>
    apiFetch<PageResponse<AdminUser>>("/admin/users", { token, query: params }),
  user: (token: string, id: string) => apiFetch<AdminUser>(`/admin/users/${id}`, { token }),

  // ---------- kritik & saran ----------
  feedback: (
    token: string,
    params: {
      status?: FeedbackStatus;
      role?: SenderRole;
      category?: FeedbackCategory;
      unread?: boolean;
      q?: string;
      page?: number;
      size?: number;
    },
  ) => apiFetch<PageResponse<FeedbackSummary>>("/admin/feedback", { token, query: params }),
  feedbackCounts: (token: string) => apiFetch<FeedbackCounts>("/admin/feedback/counts", { token }),
  /** Membuka percakapan juga menghapus tanda "belum dibaca tim". */
  feedbackDetail: (token: string, id: string) => apiFetch<FeedbackDetail>(`/admin/feedback/${id}`, { token }),
  replyFeedback: (token: string, id: string, body: string) =>
    apiFetch<FeedbackDetail>(`/admin/feedback/${id}/messages`, { token, method: "POST", body: { body } }),
  setFeedbackStatus: (token: string, id: string, status: FeedbackStatus) =>
    apiFetch<FeedbackDetail>(`/admin/feedback/${id}`, { token, method: "PATCH", body: { status } }),

  // ---------- dokumentasi ----------
  docs: (token: string) => apiFetch<DocSummary[]>("/admin/docs", { token }),
  doc: (token: string, id: string) => apiFetch<DocDetail>(`/admin/docs/${id}`, { token }),
  createDoc: (token: string, request: DocRequest) =>
    apiFetch<DocDetail>("/admin/docs", { token, method: "POST", body: request }),
  updateDoc: (token: string, id: string, request: DocRequest) =>
    apiFetch<DocDetail>(`/admin/docs/${id}`, { token, method: "PUT", body: request }),
  deleteDoc: (token: string, id: string) => apiFetch<void>(`/admin/docs/${id}`, { token, method: "DELETE" }),

  // ---------- hero landing page ----------
  landingHero: (token: string) => apiFetch<LandingHero>("/admin/landing/hero", { token }),
  updateLandingHero: (token: string, request: LandingHeroRequest) =>
    apiFetch<LandingHero>("/admin/landing/hero", { token, method: "PUT", body: request }),
  /** PNG/JPG maks 2 MB; backend memperkecil maks 1600 px. */
  uploadHeroImage: (token: string, file: File) => {
    const body = new FormData();
    body.set("file", file);
    return apiFetch<LandingHero>("/admin/landing/hero/image", { token, method: "POST", body });
  },
  deleteHeroImage: (token: string) =>
    apiFetch<LandingHero>("/admin/landing/hero/image", { token, method: "DELETE" }),

  // ---------- info terbaru ----------
  updates: (token: string) => apiFetch<PortalUpdate[]>("/admin/updates", { token }),
  update: (token: string, id: string) => apiFetch<PortalUpdate>(`/admin/updates/${encodeURIComponent(id)}`, { token }),
  createUpdate: (token: string, request: PortalUpdateRequest) =>
    apiFetch<PortalUpdate>("/admin/updates", { token, method: "POST", body: request }),
  saveUpdate: (token: string, id: string, request: PortalUpdateRequest) =>
    apiFetch<PortalUpdate>(`/admin/updates/${encodeURIComponent(id)}`, { token, method: "PUT", body: request }),
  setUpdatePublished: (token: string, id: string, published: boolean) =>
    apiFetch<PortalUpdate>(`/admin/updates/${encodeURIComponent(id)}/published`, { token, method: "PUT", body: { published } }),
  deleteUpdate: (token: string, id: string) =>
    apiFetch<void>(`/admin/updates/${encodeURIComponent(id)}`, { token, method: "DELETE" }),
  /** PNG/JPG maks 2 MB; backend memperkecil maks 1600 px. */
  uploadUpdateImage: (token: string, id: string, file: File) => {
    const body = new FormData();
    body.set("file", file);
    return apiFetch<PortalUpdate>(`/admin/updates/${encodeURIComponent(id)}/image`, { token, method: "POST", body });
  },
  deleteUpdateImage: (token: string, id: string) =>
    apiFetch<PortalUpdate>(`/admin/updates/${encodeURIComponent(id)}/image`, { token, method: "DELETE" }),

  // ---------- template unggulan landing page ----------
  /** Untuk memilih, jelajahi galeri per halaman dengan getTemplates() di lib/api/public.ts. */
  landingTemplates: (token: string) => apiFetch<GalleryTemplate[]>("/admin/landing/templates", { token }),
  /** Mengganti seluruh daftar (maks 12); urutan array = urutan tampil. */
  updateLandingTemplates: (token: string, templateIds: string[]) =>
    apiFetch<GalleryTemplate[]>("/admin/landing/templates", { token, method: "PUT", body: { templateIds } }),
};
