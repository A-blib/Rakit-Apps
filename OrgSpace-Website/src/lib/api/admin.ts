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
};
