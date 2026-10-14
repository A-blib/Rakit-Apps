"use client";

import { useEffect, useState, use } from "react";
import Link from "next/link";
import {
  LayoutDashboard,
  Inbox,
  Newspaper,
  Boxes,
  Plus,
  Trash2,
  LogOut,
  ExternalLink,
  CheckCircle2,
  AlertCircle,
  Loader2,
  User,
  ShieldCheck,
  RefreshCw,
} from "lucide-react";

interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: string;
}

interface Stats {
  submissionsCount: number;
  newsCount: number;
  customItemsCount: number;
}

interface Submission {
  id: string;
  formName: string;
  data: Record<string, any>;
  createdAt: string;
}

interface NewsItem {
  id: string;
  title: string;
  summary: string;
  content: string;
  imageUrl?: string;
  publishedAt?: string;
}

export default function AdminPage({ params }: { params: Promise<{ slug: string }> }) {
  const resolvedParams = use(params);
  const slug = resolvedParams.slug;
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

  // Auth states
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<AdminUser | null>(null);
  const [loadingAuth, setLoadingAuth] = useState(true);

  // Login form state
  const [emailInput, setEmailInput] = useState("");
  const [passwordInput, setPasswordInput] = useState("");
  const [loginError, setLoginError] = useState<string | null>(null);
  const [loggingIn, setLoggingIn] = useState(false);

  // Dashboard states
  const [activeTab, setActiveTab] = useState<"overview" | "submissions" | "news" | "collections">("overview");
  const [siteName, setSiteName] = useState<string>("");
  const [orgName, setOrgName] = useState<string>("");
  const [stats, setStats] = useState<Stats>({ submissionsCount: 0, newsCount: 0, customItemsCount: 0 });

  // Submissions state
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loadingSubmissions, setLoadingSubmissions] = useState(false);

  // News state
  const [newsList, setNewsList] = useState<NewsItem[]>([]);
  const [loadingNews, setLoadingNews] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [newSummary, setNewSummary] = useState("");
  const [newContent, setNewContent] = useState("");
  const [submittingNews, setSubmittingNews] = useState(false);

  // Collections state
  const [collections, setCollections] = useState<Record<string, Array<Record<string, any>>>>({});
  const [loadingCollections, setLoadingCollections] = useState(false);
  const [selectedCol, setSelectedCol] = useState<string>("prestasi");
  const [newColName, setNewColName] = useState("");
  const [dynamicFields, setDynamicFields] = useState<Array<{ key: string; value: string }>>([
    { key: "nama_juara", value: "" },
    { key: "lomba", value: "" },
    { key: "tahun", value: "2026" },
    { key: "tingkat", value: "Nasional" },
  ]);
  const [submittingColItem, setSubmittingColItem] = useState(false);

  // Success / error alerts
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error" = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Initial check on mount
  useEffect(() => {
    const savedToken = localStorage.getItem(`orgspace_site_token_${slug}`);
    if (savedToken) {
      verifyToken(savedToken);
    } else {
      setLoadingAuth(false);
      fetchSiteInfo();
    }
  }, [slug]);

  const verifyToken = async (jwt: string) => {
    setLoadingAuth(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/auth/me`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const userData = await res.json();
        setUser(userData.user || userData);
        setToken(jwt);
        fetchSiteInfo();
        loadAllDashboardData(jwt);
      } else {
        localStorage.removeItem(`orgspace_site_token_${slug}`);
        setToken(null);
        setUser(null);
        fetchSiteInfo();
      }
    } catch {
      localStorage.removeItem(`orgspace_site_token_${slug}`);
      setToken(null);
      setUser(null);
      fetchSiteInfo();
    } finally {
      setLoadingAuth(false);
    }
  };

  const fetchSiteInfo = async () => {
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/render/${slug}`);
      if (res.ok) {
        const data = await res.json();
        setSiteName(data.name || data.projectName);
        setOrgName(data.organizationName);
      }
    } catch (e) {
      console.error("Gagal memuat info website", e);
    }
  };

  const loadAllDashboardData = async (jwt: string) => {
    loadStats(jwt);
    loadSubmissions(jwt);
    loadNews(jwt);
    loadCollections(jwt);
  };

  const loadStats = async (jwt: string) => {
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/stats`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setStats(data);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const loadSubmissions = async (jwt: string) => {
    setLoadingSubmissions(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/submissions`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setSubmissions(data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingSubmissions(false);
    }
  };

  const loadNews = async (jwt: string) => {
    setLoadingNews(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/news`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setNewsList(data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingNews(false);
    }
  };

  const loadCollections = async (jwt: string) => {
    setLoadingCollections(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/collections`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setCollections(data);
        const colKeys = Object.keys(data);
        if (colKeys.length > 0 && !colKeys.includes(selectedCol)) {
          setSelectedCol(colKeys[0]);
        }
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingCollections(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError(null);
    setLoggingIn(true);

    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: emailInput.trim(), password: passwordInput }),
      });

      const data = await res.json();
      if (!res.ok) {
        setLoginError(data.message || "Gagal masuk. Periksa email dan password Anda.");
        return;
      }

      localStorage.setItem(`orgspace_site_token_${slug}`, data.token);
      setToken(data.token);
      setUser(data.user);
      loadAllDashboardData(data.token);
      showToast("Berhasil masuk sebagai pengurus organisasi!");
    } catch {
      setLoginError("Gagal menghubungi server. Pastikan koneksi backend aktif.");
    } finally {
      setLoggingIn(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem(`orgspace_site_token_${slug}`);
    setToken(null);
    setUser(null);
    showToast("Anda telah keluar dari portal pengurus.", "success");
  };

  const handleCreateNews = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !newTitle.trim()) return;
    setSubmittingNews(true);

    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/news`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: newTitle.trim(),
          summary: newSummary.trim(),
          content: newContent.trim(),
        }),
      });

      if (res.ok) {
        setNewTitle("");
        setNewSummary("");
        setNewContent("");
        showToast("Berita baru berhasil diterbitkan!");
        loadNews(token);
        loadStats(token);
      } else {
        const err = await res.json();
        showToast(err.message || "Gagal menerbitkan berita", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    } finally {
      setSubmittingNews(false);
    }
  };

  const handleDeleteNews = async (newsId: string) => {
    if (!token || !confirm("Yakin ingin menghapus berita ini?")) return;

    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/news/${newsId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        showToast("Berita berhasil dihapus");
        loadNews(token);
        loadStats(token);
      } else {
        showToast("Gagal menghapus berita", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    }
  };

  const handleCreateCollectionItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    const colName = (newColName.trim() || selectedCol).toLowerCase();
    if (!colName) {
      showToast("Pilih atau masukkan nama modul koleksi", "error");
      return;
    }

    // Convert dynamic fields to json object
    const payloadData: Record<string, string> = {};
    dynamicFields.forEach((f) => {
      if (f.key.trim() && f.value.trim()) {
        payloadData[f.key.trim()] = f.value.trim();
      }
    });

    if (Object.keys(payloadData).length === 0) {
      showToast("Mohon isi minimal satu data atribut field", "error");
      return;
    }

    setSubmittingColItem(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/collections/${colName}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payloadData),
      });

      if (res.ok) {
        showToast(`Item berhasil ditambahkan ke modul '${colName}'!`);
        setNewColName("");
        // reset field values while preserving keys
        setDynamicFields(dynamicFields.map((f) => ({ ...f, value: "" })));
        setSelectedCol(colName);
        loadCollections(token);
        loadStats(token);
      } else {
        const err = await res.json();
        showToast(err.message || "Gagal menambahkan item modul", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    } finally {
      setSubmittingColItem(false);
    }
  };

  const handleDeleteCollectionItem = async (colName: string, itemId: string) => {
    if (!token || !confirm("Yakin ingin menghapus item ini?")) return;

    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/collections/${colName}/${itemId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        showToast("Item berhasil dihapus dari modul");
        loadCollections(token);
        loadStats(token);
      } else {
        showToast("Gagal menghapus item", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    }
  };

  const addDynamicField = () => {
    setDynamicFields([...dynamicFields, { key: "", value: "" }]);
  };

  const removeDynamicField = (index: number) => {
    setDynamicFields(dynamicFields.filter((_, i) => i !== index));
  };

  // 1. Loading screen
  if (loadingAuth) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white">
        <Loader2 className="w-8 h-8 animate-spin text-blue-400 mb-3" />
        <p className="text-sm text-slate-300">Memeriksa sesi pengurus...</p>
      </div>
    );
  }

  // 2. Login Form (Unauthenticated)
  if (!token || !user) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-slate-900 to-slate-800 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 text-white mb-4 shadow-lg shadow-blue-500/30">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-white">
            Portal Pengurus Website
          </h2>
          <p className="mt-2 text-sm text-slate-400">
            {orgName ? `${orgName} (${slug})` : slug}
          </p>
        </div>

        <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
          <div className="bg-white py-8 px-6 shadow-xl rounded-2xl sm:px-10 border border-slate-100">
            {loginError && (
              <div className="mb-5 p-3.5 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-start gap-2.5">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{loginError}</span>
              </div>
            )}

            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Email / Akun Pengurus
                </label>
                <input
                  type="email"
                  required
                  placeholder="pengurus@orgspace.test"
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none transition"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Password
                </label>
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  value={passwordInput}
                  onChange={(e) => setPasswordInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none transition"
                />
              </div>

              <button
                type="submit"
                disabled={loggingIn}
                className="w-full mt-2 flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 px-4 rounded-xl text-sm shadow-md shadow-blue-600/20 transition disabled:opacity-50"
              >
                {loggingIn ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Memverifikasi...</span>
                  </>
                ) : (
                  <span>Masuk ke Dashboard</span>
                )}
              </button>
            </form>

            <div className="mt-6 pt-5 border-t border-slate-100 text-center">
              <Link
                href={`/${slug}`}
                className="text-xs text-slate-500 hover:text-slate-800 transition inline-flex items-center gap-1"
              >
                <span>&larr; Kembali ke Website Publik</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 3. Authenticated Dashboard View
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Toast Alert */}
      {toast && (
        <div
          className={`fixed bottom-5 right-5 z-50 flex items-center gap-2 px-4 py-3 rounded-xl shadow-lg text-sm font-medium transition animate-slide-up ${
            toast.type === "success"
              ? "bg-emerald-600 text-white"
              : "bg-red-600 text-white"
          }`}
        >
          {toast.type === "success" ? (
            <CheckCircle2 className="w-4 h-4" />
          ) : (
            <AlertCircle className="w-4 h-4" />
          )}
          <span>{toast.message}</span>
        </div>
      )}

      {/* Top Header */}
      <header className="bg-slate-900 text-white border-b border-slate-800 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center font-bold text-white shadow-md">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-base font-bold text-white leading-tight">
                  {orgName || siteName || "Portal Pengurus"}
                </h1>
                <span className="text-[10px] bg-blue-500/20 text-blue-300 border border-blue-400/30 px-1.5 py-0.5 rounded font-mono">
                  {slug}
                </span>
              </div>
              <p className="text-xs text-slate-400">Pengelolaan Konten Dinamis Website</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link
              href={`/${slug}`}
              target="_blank"
              className="hidden sm:inline-flex items-center gap-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs px-3 py-1.5 rounded-lg border border-slate-700 transition"
            >
              <ExternalLink className="w-3.5 h-3.5" />
              <span>Lihat Website</span>
            </Link>

            <div className="flex items-center gap-2 pl-2 border-l border-slate-800">
              <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300">
                <User className="w-4 h-4" />
              </div>
              <div className="hidden md:block text-left">
                <div className="text-xs font-semibold text-white">{user.name}</div>
                <div className="text-[10px] text-emerald-400 uppercase tracking-wider font-mono">
                  {user.role}
                </div>
              </div>
              <button
                onClick={handleLogout}
                title="Keluar"
                className="ml-1 p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex space-x-1 overflow-x-auto">
          {[
            { id: "overview", label: "Ringkasan", icon: LayoutDashboard },
            {
              id: "submissions",
              label: "Kotak Masuk Form",
              icon: Inbox,
              badge: stats.submissionsCount,
            },
            {
              id: "news",
              label: "Berita & Agenda",
              icon: Newspaper,
              badge: stats.newsCount,
            },
            {
              id: "collections",
              label: "Modul Kustom",
              icon: Boxes,
              badge: stats.customItemsCount,
            },
          ].map((tab) => {
            const Icon = tab.icon;
            const active = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex items-center gap-2 py-3 px-4 text-xs font-semibold border-b-2 transition shrink-0 ${
                  active
                    ? "border-blue-500 text-blue-400 bg-slate-800/40"
                    : "border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700"
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
                {tab.badge !== undefined && tab.badge > 0 && (
                  <span
                    className={`ml-1 px-1.5 py-0.2 rounded-full text-[10px] font-mono ${
                      active ? "bg-blue-500 text-white" : "bg-slate-800 text-slate-300"
                    }`}
                  >
                    {tab.badge}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full flex-1">
        {/* TAB 1: OVERVIEW */}
        {activeTab === "overview" && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              <div
                onClick={() => setActiveTab("submissions")}
                className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-slate-500 text-xs font-semibold mb-2">
                  <span>FORMULIR MASUK</span>
                  <Inbox className="w-5 h-5 text-blue-500" />
                </div>
                <div className="text-3xl font-extrabold text-slate-900">
                  {stats.submissionsCount}
                </div>
                <p className="text-xs text-slate-400 mt-1">Data pendaftaran & aspirasi pengunjung</p>
              </div>

              <div
                onClick={() => setActiveTab("news")}
                className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-slate-500 text-xs font-semibold mb-2">
                  <span>BERITA & KEGIATAN</span>
                  <Newspaper className="w-5 h-5 text-emerald-500" />
                </div>
                <div className="text-3xl font-extrabold text-slate-900">
                  {stats.newsCount}
                </div>
                <p className="text-xs text-slate-400 mt-1">Artikel aktif terbit di website</p>
              </div>

              <div
                onClick={() => setActiveTab("collections")}
                className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-slate-500 text-xs font-semibold mb-2">
                  <span>ITEM MODUL KUSTOM</span>
                  <Boxes className="w-5 h-5 text-purple-500" />
                </div>
                <div className="text-3xl font-extrabold text-slate-900">
                  {stats.customItemsCount}
                </div>
                <p className="text-xs text-slate-400 mt-1">Data prestasi, alumni, atau galeri</p>
              </div>
            </div>

            {/* Quick Actions Card */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
              <h2 className="text-base font-bold text-slate-900 mb-3">Panduan Akses Pengurus</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs text-slate-600 leading-relaxed">
                <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
                  <h3 className="font-semibold text-slate-800 mb-1 flex items-center gap-1.5">
                    <ShieldCheck className="w-4 h-4 text-blue-600" />
                    <span>Autentikasi Aman & Otomatis</span>
                  </h3>
                  <p>
                    Sebagai pengurus resmi organisasi, Anda memiliki akses mengelola data secara instan tanpa perlu menyiapkan server backend terpisah.
                  </p>
                </div>
                <div className="p-4 rounded-xl bg-slate-50 border border-slate-100">
                  <h3 className="font-semibold text-slate-800 mb-1 flex items-center gap-1.5">
                    <Boxes className="w-4 h-4 text-purple-600" />
                    <span>Dukungan Modul Bebas</span>
                  </h3>
                  <p>
                    Anda dapat menambah koleksi baru kapan saja di tab <strong>Modul Kustom</strong>. Data yang diisi akan otomatis tampil di template website publik.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: SUBMISSIONS */}
        {activeTab === "submissions" && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-bold text-slate-900">Kotak Masuk Formulir</h2>
                <p className="text-xs text-slate-500">
                  Respon dari pengunjung yang mengisi formulir pendaftaran / kontak di website.
                </p>
              </div>
              <button
                onClick={() => token && loadSubmissions(token)}
                className="flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 transition"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Segarkan</span>
              </button>
            </div>

            {loadingSubmissions ? (
              <div className="py-12 text-center text-slate-400">
                <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2" />
                <p className="text-xs">Memuat formulir masuk...</p>
              </div>
            ) : submissions.length === 0 ? (
              <div className="bg-white p-12 text-center rounded-2xl border border-slate-200 text-slate-400 text-xs">
                Belum ada formulir masuk dari pengunjung.
              </div>
            ) : (
              <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-slate-600">
                    <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                      <tr>
                        <th className="py-3 px-4">Waktu</th>
                        <th className="py-3 px-4">Formulir</th>
                        <th className="py-3 px-4">Data Masuk</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {submissions.map((sub) => (
                        <tr key={sub.id} className="hover:bg-slate-50/70 transition">
                          <td className="py-3 px-4 whitespace-nowrap text-slate-400 font-mono text-[11px]">
                            {new Date(sub.createdAt).toLocaleString("id-ID")}
                          </td>
                          <td className="py-3 px-4 whitespace-nowrap">
                            <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded font-mono font-semibold text-[11px]">
                              {sub.formName}
                            </span>
                          </td>
                          <td className="py-3 px-4">
                            <div className="space-y-1">
                              {Object.entries(sub.data || {}).map(([k, v]) => (
                                <div key={k} className="inline-block mr-3">
                                  <span className="font-semibold text-slate-700">{k}:</span>{" "}
                                  <span className="text-slate-600">{String(v)}</span>
                                </div>
                              ))}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 3: NEWS & EVENTS */}
        {activeTab === "news" && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Form Tambah Berita */}
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm h-fit">
              <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center gap-1.5">
                <Plus className="w-4 h-4 text-emerald-600" />
                <span>Terbitkan Berita / Agenda</span>
              </h3>
              <form onSubmit={handleCreateNews} className="space-y-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Judul Berita <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Contoh: Rapat Kerja Anggota 2026"
                    value={newTitle}
                    onChange={(e) => setNewTitle(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-300 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Ringkasan Singkat
                  </label>
                  <input
                    type="text"
                    placeholder="Deskripsi singkat yang tampil di beranda..."
                    value={newSummary}
                    onChange={(e) => setNewSummary(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-300 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Konten Lengkap
                  </label>
                  <textarea
                    rows={4}
                    placeholder="Tuliskan berita lengkap di sini..."
                    value={newContent}
                    onChange={(e) => setNewContent(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-300 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  ></textarea>
                </div>
                <button
                  type="submit"
                  disabled={submittingNews}
                  className="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-sm transition disabled:opacity-50"
                >
                  {submittingNews ? "Menerbitkan..." : "Terbitkan Berita"}
                </button>
              </form>
            </div>

            {/* List Berita */}
            <div className="lg:col-span-2 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-bold text-slate-900">Daftar Berita Aktif</h3>
                <span className="text-xs text-slate-400 font-mono">
                  {newsList.length} Berita
                </span>
              </div>

              {loadingNews ? (
                <div className="py-12 text-center text-slate-400">
                  <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2" />
                  <p className="text-xs">Memuat berita...</p>
                </div>
              ) : newsList.length === 0 ? (
                <div className="bg-white p-8 text-center rounded-2xl border border-slate-200 text-slate-400 text-xs">
                  Belum ada berita yang diterbitkan.
                </div>
              ) : (
                <div className="space-y-3">
                  {newsList.map((item) => (
                    <div
                      key={item.id}
                      className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex items-start justify-between gap-4"
                    >
                      <div className="space-y-1">
                        <h4 className="text-sm font-bold text-slate-900">{item.title}</h4>
                        <p className="text-xs text-slate-500">{item.summary}</p>
                        {item.publishedAt && (
                          <div className="text-[10px] text-slate-400 font-mono">
                            Diterbitkan: {item.publishedAt}
                          </div>
                        )}
                      </div>
                      <button
                        onClick={() => handleDeleteNews(item.id)}
                        className="p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition"
                        title="Hapus Berita"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 4: CUSTOM COLLECTIONS */}
        {activeTab === "collections" && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Form Tambah Item Modul */}
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm h-fit">
              <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center gap-1.5">
                <Plus className="w-4 h-4 text-purple-600" />
                <span>Tambah Data Modul</span>
              </h3>

              <form onSubmit={handleCreateCollectionItem} className="space-y-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Nama Modul / Koleksi
                  </label>
                  <div className="flex gap-2">
                    <select
                      value={selectedCol}
                      onChange={(e) => setSelectedCol(e.target.value)}
                      className="w-1/2 px-2.5 py-2 text-xs rounded-xl border border-slate-300 focus:outline-none"
                    >
                      {Object.keys(collections).length === 0 && (
                        <option value="prestasi">prestasi</option>
                      )}
                      {Object.keys(collections).map((k) => (
                        <option key={k} value={k}>
                          {k}
                        </option>
                      ))}
                    </select>
                    <input
                      type="text"
                      placeholder="Atau nama baru..."
                      value={newColName}
                      onChange={(e) => setNewColName(e.target.value)}
                      className="w-1/2 px-2.5 py-2 text-xs rounded-xl border border-slate-300 focus:outline-none"
                    />
                  </div>
                  <span className="text-[10px] text-slate-400 mt-1 block">
                    Gunakan huruf kecil tanpa spasi (misal: <code>prestasi</code>, <code>alumni</code>)
                  </span>
                </div>

                <div className="pt-2 border-t border-slate-100">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-semibold text-slate-700">Atribut Field Data:</span>
                    <button
                      type="button"
                      onClick={addDynamicField}
                      className="text-[11px] text-blue-600 hover:text-blue-800 font-semibold"
                    >
                      + Tambah Field
                    </button>
                  </div>

                  <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                    {dynamicFields.map((field, idx) => (
                      <div key={idx} className="flex items-center gap-1.5">
                        <input
                          type="text"
                          placeholder="Nama field (mis: nama)"
                          value={field.key}
                          onChange={(e) => {
                            const updated = [...dynamicFields];
                            updated[idx].key = e.target.value;
                            setDynamicFields(updated);
                          }}
                          className="w-1/3 px-2 py-1.5 text-[11px] rounded-lg border border-slate-300 font-mono"
                        />
                        <input
                          type="text"
                          placeholder="Isi data..."
                          value={field.value}
                          onChange={(e) => {
                            const updated = [...dynamicFields];
                            updated[idx].value = e.target.value;
                            setDynamicFields(updated);
                          }}
                          className="w-2/3 px-2 py-1.5 text-[11px] rounded-lg border border-slate-300"
                        />
                        {dynamicFields.length > 1 && (
                          <button
                            type="button"
                            onClick={() => removeDynamicField(idx)}
                            className="text-slate-400 hover:text-red-500 p-1"
                          >
                            &times;
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={submittingColItem}
                  className="w-full mt-2 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-xl text-xs font-semibold shadow-sm transition disabled:opacity-50"
                >
                  {submittingColItem ? "Menyimpan..." : "Simpan ke Modul"}
                </button>
              </form>
            </div>

            {/* List Item per Koleksi */}
            <div className="lg:col-span-2 space-y-4">
              <div className="flex items-center justify-between flex-wrap gap-2">
                <div>
                  <h3 className="text-sm font-bold text-slate-900">
                    Data Modul: <span className="font-mono text-purple-600 font-bold">{selectedCol}</span>
                  </h3>
                  <p className="text-xs text-slate-400">
                    Elemen ini otomatis menyuplai template dengan atribut <code>data-ws-list="{selectedCol}"</code>.
                  </p>
                </div>

                {/* Pilih Koleksi yang dilihat */}
                <div className="flex gap-1.5">
                  {Object.keys(collections).map((colName) => (
                    <button
                      key={colName}
                      onClick={() => setSelectedCol(colName)}
                      className={`px-2.5 py-1 text-xs rounded-lg font-mono font-semibold transition ${
                        selectedCol === colName
                          ? "bg-purple-600 text-white"
                          : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-50"
                      }`}
                    >
                      {colName} ({(collections[colName] || []).length})
                    </button>
                  ))}
                </div>
              </div>

              {loadingCollections ? (
                <div className="py-12 text-center text-slate-400">
                  <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2" />
                  <p className="text-xs">Memuat data modul kustom...</p>
                </div>
              ) : !collections[selectedCol] || collections[selectedCol].length === 0 ? (
                <div className="bg-white p-8 text-center rounded-2xl border border-slate-200 text-slate-400 text-xs">
                  Belum ada item dalam modul "{selectedCol}".
                </div>
              ) : (
                <div className="space-y-2.5">
                  {collections[selectedCol].map((item, idx) => (
                    <div
                      key={item.id || idx}
                      className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex items-start justify-between gap-4"
                    >
                      <div className="space-y-1">
                        <div className="flex flex-wrap gap-2">
                          {Object.entries(item).map(([k, v]) => {
                            if (k === "id") return null;
                            return (
                              <div
                                key={k}
                                className="inline-flex items-center gap-1 bg-slate-50 border border-slate-200 px-2 py-0.5 rounded text-xs"
                              >
                                <span className="font-mono text-slate-400 text-[10px]">{k}:</span>
                                <span className="font-semibold text-slate-800">{String(v)}</span>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                      {item.id && (
                        <button
                          onClick={() => handleDeleteCollectionItem(selectedCol, item.id)}
                          className="p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition"
                          title="Hapus Item"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
