"use client";

import { createElement, useEffect, useState } from "react";
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
  ShieldCheck,
  RefreshCw,
  Users,
  Pencil,
  ArrowUp,
  ArrowDown,
  ImagePlus,
  Menu,
  X,
  Eye,
  EyeOff,
  Sun,
  Moon,
  Calendar,
  User,
  Trophy,
  Award,
  Star,
  Image as ImageIcon,
  Camera,
  Target,
  Flag,
  BookOpen,
  GraduationCap,
  Briefcase,
  Handshake,
  Heart,
  MapPin,
  Megaphone,
  ClipboardList,
  FileText,
  Folder,
  List,
  Tent,
  Compass,
  Music,
  Sparkles,
  Mail,
  Link as LinkIcon,
  type LucideIcon,
} from "lucide-react";
import type { FieldType } from "@/lib/types";
import {
  bottomNav,
  buildNav,
  cardFields,
  fieldGroups,
  humanize,
  moduleLabel,
  themeCss,
  type NavId,
  type NavLink,
  type PortalConfig,
} from "@/lib/portal-config";

/** Peran yang sedang masuk. Login Portal memakai kredensial peran organisasi (bukan akun OrgSpace pribadi). */
interface AdminUser {
  username: string;
  role: "owner" | "admin" | "editor";
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

interface ModuleField {
  key: string;
  value: string;
  type: FieldType;
  fixed?: boolean;
}

/** Ikon yang boleh dipilih provider di portal.json (daftar sama dengan PortalConfig.ICONS di backend). */
const ICONS: Record<string, LucideIcon> = {
  "layout-dashboard": LayoutDashboard,
  inbox: Inbox,
  newspaper: Newspaper,
  calendar: Calendar,
  users: Users,
  user: User,
  trophy: Trophy,
  award: Award,
  star: Star,
  image: ImageIcon,
  camera: Camera,
  target: Target,
  flag: Flag,
  "book-open": BookOpen,
  "graduation-cap": GraduationCap,
  briefcase: Briefcase,
  handshake: Handshake,
  heart: Heart,
  "map-pin": MapPin,
  megaphone: Megaphone,
  "clipboard-list": ClipboardList,
  "file-text": FileText,
  folder: Folder,
  list: List,
  boxes: Boxes,
  tent: Tent,
  compass: Compass,
  music: Music,
  sparkles: Sparkles,
  mail: Mail,
  link: LinkIcon,
};

const iconOf = (name: string | undefined): LucideIcon => (name && ICONS[name]) || Boxes;

const SIDEBAR_MIN = 200;
const SIDEBAR_MAX = 400;

/**
 * Optimasi ukuran foto sebelum dikirim ke backend (sisi terpanjang maks 1600px, JPEG quality 85%).
 */
async function shrinkPhoto(file: File): Promise<Blob> {
  const bitmap = await createImageBitmap(file);
  const scale = Math.min(1, 1600 / Math.max(bitmap.width, bitmap.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.round(bitmap.width * scale);
  canvas.height = Math.round(bitmap.height * scale);
  canvas.getContext("2d")!.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
  bitmap.close();
  return new Promise((resolve, reject) =>
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error("Foto tidak bisa diproses"))), "image/jpeg", 0.85)
  );
}

/** Pemanggil API Portal; path relatif terhadap `/api/public/sites/`, mis. "osis/admin/news". */
export type PortalApi = (path: string, init?: RequestInit) => Promise<Response>;

/** Akun login demo: sengaja publik dan ditampilkan di layar login, tidak membuka akun apa pun. */
export interface DemoLogin {
  username: string;
  password: string;
}

interface PortalPengurusProps {
  slug: string;
  siteHref: string;
  api: PortalApi;
  /** Diisi hanya untuk Portal demo (/demo/{id}/admin): token tidak disimpan di localStorage. */
  demo?: DemoLogin;
}

/**
 * Portal Pengurus website. Dipakai /{slug}/admin (api = backend) dan /demo/{id}/admin (api = data contoh di memori,
 * lihat lib/demo-portal.ts), agar demo selalu sama dengan Portal asli.
 */
export function PortalPengurus({ slug, siteHref, api, demo }: PortalPengurusProps) {
  // Demo tidak pernah menyimpan sesi: token-nya palsu dan tidak boleh menimpa sesi pengurus asli di origin ini.
  const tokenKey = demo ? null : `orgspace_site_token_${slug}`;

  // Auth states
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<AdminUser | null>(null);
  const [loadingAuth, setLoadingAuth] = useState(true);

  // Login form state
  const [usernameInput, setUsernameInput] = useState("");
  const [passwordInput, setPasswordInput] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [loggingIn, setLoggingIn] = useState(false);

  // Theme state (Dark Mode / Light Mode - OrgSpace-Apps Biru Semangat)
  const [isDarkMode, setIsDarkMode] = useState(false);

  useEffect(() => {
    if (typeof document !== "undefined") {
      setIsDarkMode(document.documentElement.classList.contains("dark"));
    }
  }, []);

  const toggleTheme = () => {
    const nextDark = !isDarkMode;
    setIsDarkMode(nextDark);
    if (typeof document !== "undefined") {
      if (nextDark) {
        document.documentElement.classList.add("dark");
        localStorage.setItem("orgspace_theme", "dark");
      } else {
        document.documentElement.classList.remove("dark");
        localStorage.setItem("orgspace_theme", "light");
      }
    }
  };

  // Dashboard states
  const [activeTab, setActiveTab] = useState<NavId>("overview");
  const [portal, setPortal] = useState<PortalConfig>({});
  // Lebar sidebar hasil geser milik pengurus ini (disimpan di browser-nya), bukan pengaturan template.
  const sidebarKey = demo ? null : `orgspace_portal_sidebar_${slug}`;
  const [sidebarWidth, setSidebarWidth] = useState<number | null>(() => {
    // Sidebar baru tampil setelah login (bukan saat render server), jadi membaca localStorage di sini aman.
    if (!sidebarKey || typeof window === "undefined") return null;
    try {
      const saved = Number(localStorage.getItem(sidebarKey));
      return saved >= SIDEBAR_MIN && saved <= SIDEBAR_MAX ? saved : null;
    } catch {
      return null;
    }
  });
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);
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
  const [selectedCol, setSelectedCol] = useState<string>("");
  const [templateModules, setTemplateModules] = useState<Record<string, Record<string, FieldType>>>({});
  const [dynamicFields, setDynamicFields] = useState<ModuleField[]>([]);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [uploadingField, setUploadingField] = useState<number | null>(null);

  const templateFields = (name: string, modules = templateModules): ModuleField[] =>
    Object.entries(modules[name] || {}).map(([key, type]) => ({ key, value: "", type, fixed: true }));

  const chooseCollection = (name: string, modules = templateModules) => {
    setSelectedCol(name);
    setEditingId(null);
    // Modul yang sudah tidak ada di template tidak punya field: form hanya muncul saat mengubah item lamanya.
    setDynamicFields(templateFields(name, modules));
  };

  const [submittingColItem, setSubmittingColItem] = useState(false);

  // Success / error alerts
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error" = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Initial check on mount
  useEffect(() => {
    const savedToken = tokenKey ? localStorage.getItem(tokenKey) : null;
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
      const res = await api(`${slug}/auth/me`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const userData = await res.json();
        setUser(userData.user || userData);
        setToken(jwt);
        fetchSiteInfo();
        loadAllDashboardData(jwt);
      } else {
        if (tokenKey) localStorage.removeItem(tokenKey);
        setToken(null);
        setUser(null);
        fetchSiteInfo();
      }
    } catch {
      if (tokenKey) localStorage.removeItem(tokenKey);
      setToken(null);
      setUser(null);
      fetchSiteInfo();
    } finally {
      setLoadingAuth(false);
    }
  };

  const fetchSiteInfo = async () => {
    try {
      const res = await api(`render/${slug}`);
      if (res.ok) {
        const data = await res.json();
        setSiteName(data.name || data.projectName);
        setOrgName(data.organizationName);
        setTemplateModules(data.modules || {});
        setPortal(data.portal || {});
      }
    } catch (e) {
      console.error("Gagal memuat info website", e);
    }
  };

  const loadAllDashboardData = async (jwt: string) => {
    fetchSiteInfo();
    loadStats(jwt);
    loadSubmissions(jwt);
    loadNews(jwt);
    loadCollections(jwt);
  };

  const loadStats = async (jwt: string) => {
    try {
      const res = await api(`${slug}/admin/stats`, {
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
      const res = await api(`${slug}/admin/submissions`, {
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
      const res = await api(`${slug}/admin/news`, {
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
      const res = await api(`${slug}/admin/collections`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        setCollections(await res.json());
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
      const res = await api(`${slug}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: usernameInput.trim(), password: passwordInput }),
      });

      const data = await res.json();
      if (!res.ok) {
        // PORTAL_CREDENTIALS_NOT_SET: Pemilik belum mengatur kredensial di app (Kelola organisasi → Akses Portal website).
        setLoginError(data.message || "Gagal masuk. Periksa username dan password Anda.");
        return;
      }

      if (tokenKey) localStorage.setItem(tokenKey, data.token);
      setToken(data.token);
      setUser(data.user);
      loadAllDashboardData(data.token);
      showToast("Berhasil masuk ke portal pengurus!");
    } catch {
      setLoginError("Gagal menghubungi server. Pastikan koneksi backend aktif.");
    } finally {
      setLoggingIn(false);
    }
  };

  const handleLogout = () => {
    if (tokenKey) localStorage.removeItem(tokenKey);
    setToken(null);
    setUser(null);
    showToast("Anda telah keluar dari portal pengurus.", "success");
  };

  const handleCreateNews = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !newTitle.trim()) return;
    setSubmittingNews(true);

    try {
      const res = await api(`${slug}/admin/news`, {
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
      const res = await api(`${slug}/admin/news/${newsId}`, {
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

    const colName = selectedCol;
    if (!colName) return;

    const payloadData: Record<string, string> = {};
    dynamicFields.forEach((f) => {
      if (f.key.trim() && f.value.trim()) {
        payloadData[f.key.trim()] = f.value.trim();
      }
    });

    if (Object.keys(payloadData).length === 0) {
      showToast("Isi minimal satu kolom", "error");
      return;
    }

    setSubmittingColItem(true);
    try {
      const itemPath = editingId ? `/${editingId}` : "";
      const res = await api(`${slug}/admin/collections/${colName}${itemPath}`, {
        method: editingId ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payloadData),
      });

      if (res.ok) {
        showToast(editingId ? "Perubahan berhasil disimpan!" : `Data baru ditambahkan ke ${moduleLabel(portal, colName)}!`);
        setEditingId(null);
        setDynamicFields(templateFields(colName));
        loadCollections(token);
        loadStats(token);
      } else {
        const err = await res.json();
        showToast(err.message || "Gagal menyimpan item modul", "error");
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
      const res = await api(`${slug}/admin/collections/${colName}/${itemId}`, {
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

  const updateField = (index: number, change: Partial<ModuleField>) => {
    setDynamicFields((fields) => fields.map((f, i) => (i === index ? { ...f, ...change } : f)));
  };

  const startEditItem = (item: Record<string, any>) => {
    const fields = templateFields(selectedCol).map((f) => ({ ...f, value: item[f.key] != null ? String(item[f.key]) : "" }));
    Object.entries(item).forEach(([key, value]) => {
      if (key !== "id" && !fields.some((f) => f.key === key)) {
        fields.push({ key, value: String(value), type: "teks" });
      }
    });
    setEditingId(item.id);
    setDynamicFields(fields);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setDynamicFields(templateFields(selectedCol));
  };

  const handleUploadPhoto = async (index: number, file: File | undefined) => {
    if (!token || !file) return;
    setUploadingField(index);
    try {
      const body = new FormData();
      body.append("file", await shrinkPhoto(file), "foto.jpg");
      const res = await api(`${slug}/admin/media`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body,
      });
      const data = await res.json();
      if (res.ok) {
        updateField(index, { value: data.url });
      } else {
        showToast(data.message || "Gagal mengunggah foto", "error");
      }
    } catch {
      showToast("Foto gagal diunggah. Pastikan file berupa gambar lalu coba lagi.", "error");
    } finally {
      setUploadingField(null);
    }
  };

  const handleMoveItem = async (index: number, direction: -1 | 1) => {
    const items = [...(collections[selectedCol] || [])];
    const target = index + direction;
    if (!token || target < 0 || target >= items.length) return;
    [items[index], items[target]] = [items[target], items[index]];
    setCollections({ ...collections, [selectedCol]: items });
    try {
      const res = await api(`${slug}/admin/collections/${selectedCol}/order`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ ids: items.map((i) => i.id) }),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        showToast(err.message || "Gagal menyimpan urutan", "error");
        loadCollections(token);
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
      loadCollections(token);
    }
  };

  const openNav = (id: NavId) => {
    setActiveTab(id);
    if (id.startsWith("mod:")) chooseCollection(id.slice(4));
    setMobileSidebarOpen(false);
  };

  const saveSidebarWidth = (width: number) => {
    const clamped = Math.min(SIDEBAR_MAX, Math.max(SIDEBAR_MIN, Math.round(width)));
    setSidebarWidth(clamped);
    if (!sidebarKey) return;
    try {
      localStorage.setItem(sidebarKey, String(clamped));
    } catch {
      // Tidak tersimpan: lebar kembali ke bawaan saat Portal dibuka lagi.
    }
  };

  const themeStyle = themeCss(portal.theme);
  const themeTag = themeStyle ? <style>{themeStyle}</style> : null;

  // 1. Loading Screen
  if (loadingAuth) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center text-foreground transition-colors duration-200">
        {themeTag}
        <Loader2 className="w-8 h-8 animate-spin text-primary mb-3" />
        <p className="text-sm font-medium text-muted">Memeriksa sesi pengurus...</p>
      </div>
    );
  }

  // 2. Login Screen (Unauthenticated)
  if (!token || !user) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center px-4 py-12 antialiased transition-colors duration-200 relative">
        {themeTag}
        {/* Theme Toggle Button on Login Screen */}
        <div className="absolute top-4 right-4">
          <button
            type="button"
            onClick={toggleTheme}
            className="p-2.5 rounded-xl border border-border bg-surface text-muted hover:text-primary hover:bg-surface-subtle transition shadow-xs flex items-center gap-2 text-xs font-medium"
            aria-label={isDarkMode ? "Ganti ke Mode Terang" : "Ganti ke Mode Gelap"}
            title={isDarkMode ? "Mode Terang" : "Mode Gelap"}
          >
            {isDarkMode ? (
              <>
                <Sun className="w-4 h-4 text-accent" />
                <span className="hidden sm:inline">Mode Terang</span>
              </>
            ) : (
              <>
                <Moon className="w-4 h-4 text-primary" />
                <span className="hidden sm:inline">Mode Gelap</span>
              </>
            )}
          </button>
        </div>

        <div className="w-full max-w-md">
          <div className="bg-surface rounded-2xl border border-border shadow-sm p-8 sm:p-10 transition-colors duration-200">
            {/* Header / Branding */}
            <div className="text-center mb-8">
              <div className="w-12 h-12 rounded-xl bg-primary text-on-primary flex items-center justify-center mx-auto mb-4 shadow-sm">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <h1 className="text-xl font-bold tracking-tight text-foreground">
                Portal Pengurus Website
              </h1>
              <p className="mt-1.5 text-sm text-muted">
                {orgName ? (
                  <span>
                    {orgName}{" "}
                    <span className="font-mono text-xs text-primary bg-primary-container px-2 py-0.5 rounded-md">
                      {slug}
                    </span>
                  </span>
                ) : (
                  <span className="font-mono text-xs text-primary bg-primary-container px-2 py-0.5 rounded-md">
                    {slug}
                  </span>
                )}
              </p>
            </div>

            {demo && (
              <div className="mb-6 p-3.5 bg-primary-container border border-border text-on-primary-container text-xs rounded-xl space-y-2">
                <p className="leading-relaxed">
                  Portal Pengurus <strong>demo</strong> berisi data contoh. Perubahan hanya ada di tab ini dan hilang saat
                  halaman ditutup.
                </p>
                <p className="font-mono">
                  Username: {demo.username}
                  <br />
                  Password: {demo.password}
                </p>
                <button
                  type="button"
                  onClick={() => {
                    setUsernameInput(demo.username);
                    setPasswordInput(demo.password);
                  }}
                  className="font-semibold text-primary hover:underline"
                >
                  Isi akun demo
                </button>
              </div>
            )}

            {/* Error Message */}
            {loginError && (
              <div className="mb-6 p-3.5 bg-error/10 border border-error/30 text-error text-xs rounded-xl flex items-start gap-2.5">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span className="leading-relaxed">{loginError}</span>
              </div>
            )}

            {/* Login Form */}
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1.5">
                  Username
                </label>
                <input
                  type="text"
                  required
                  autoComplete="username"
                  autoCapitalize="none"
                  spellCheck={false}
                  placeholder="mis. admin.osis"
                  value={usernameInput}
                  onChange={(e) => setUsernameInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-border bg-surface text-foreground text-sm placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-foreground mb-1.5">
                  Password
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    placeholder="••••••••"
                    value={passwordInput}
                    onChange={(e) => setPasswordInput(e.target.value)}
                    className="w-full pl-3.5 pr-10 py-2.5 rounded-xl border border-border bg-surface text-foreground text-sm placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-muted hover:text-foreground transition"
                    aria-label={showPassword ? "Sembunyikan password" : "Lihat password"}
                    title={showPassword ? "Sembunyikan password" : "Lihat password"}
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loggingIn}
                className="w-full mt-2 flex items-center justify-center gap-2 bg-primary hover:opacity-90 text-on-primary font-semibold py-2.5 px-4 rounded-xl text-sm transition shadow-sm disabled:opacity-50"
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

            {/* Back Link */}
            <div className="mt-8 pt-6 border-t border-border text-center">
              <Link
                href={siteHref}
                className="text-xs font-medium text-muted hover:text-foreground transition inline-flex items-center gap-1.5"
              >
                <span>&larr; Kembali ke Website Publik</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 3. Dashboard (sudah masuk)
  // Isian formulir pengunjung (nama, nomor WhatsApp) hanya untuk Pemilik & Admin; server menolak Editor (403).
  const canViewSubmissions = user.role === "owner" || user.role === "admin";
  const navGroups = buildNav(portal, Object.keys(templateModules), Object.keys(collections), canViewSubmissions);
  const navLinks = navGroups.flatMap((g) => g.items);
  const current: NavLink | undefined = navLinks.find((l) => l.id === activeTab) || navLinks[0];
  const firstModule = navLinks.find((l) => l.id.startsWith("mod:"));
  const badgeOf = (id: NavId): number | undefined =>
    id === "submissions"
      ? stats.submissionsCount
      : id === "news"
        ? stats.newsCount
        : id.startsWith("mod:")
          ? (collections[id.slice(4)] || []).length
          : undefined;

  const sidebarRight = portal.layout?.sidebar?.position === "right";
  const resizable = !!portal.layout?.sidebar?.resizable;
  const width = sidebarWidth ?? portal.layout?.sidebar?.width ?? 256;
  const header = portal.layout?.header;
  const bannerImage = header?.variant === "banner" && header.image ? `${siteHref.replace(/\/$/, "")}/${header.image}` : null;
  const bottomLinks = portal.mobile?.nav === "bottom" ? bottomNav(portal, navGroups) : [];
  const initials = user.username.substring(0, 2).toUpperCase();

  // Halaman modul: field dari template; modul yang sudah tidak ada di template (modFields kosong) hanya bisa
  // dilihat, diubah, dan dihapus.
  const modName = current?.id.startsWith("mod:") ? current.id.slice(4) : null;
  const modFields = modName ? templateModules[modName] : undefined;
  const modSettings = modName ? portal.modules?.[modName] : undefined;
  const modItems = modName ? collections[modName] || [] : [];
  const card = modFields ? cardFields(modSettings, modFields) : { title: undefined, subtitle: undefined, image: undefined };
  const fieldLabel = (key: string) => modSettings?.fields?.[key]?.label || humanize(key);
  const shownValue = (key: string, value: unknown) => {
    const text = value == null ? "" : String(value);
    if (modFields?.[key] === "tanggal" && /^\d{4}-\d{2}-\d{2}$/.test(text)) {
      return new Date(`${text}T00:00:00`).toLocaleDateString("id-ID", { day: "numeric", month: "long", year: "numeric" });
    }
    return text;
  };

  const startResize = (e: React.PointerEvent<HTMLDivElement>) => {
    e.preventDefault();
    const startX = e.clientX;
    const startWidth = width;
    const widthAt = (x: number) => startWidth + (sidebarRight ? startX - x : x - startX);
    const move = (ev: PointerEvent) =>
      setSidebarWidth(Math.min(SIDEBAR_MAX, Math.max(SIDEBAR_MIN, widthAt(ev.clientX))));
    const up = (ev: PointerEvent) => {
      window.removeEventListener("pointermove", move);
      window.removeEventListener("pointerup", up);
      saveSidebarWidth(widthAt(ev.clientX));
    };
    window.addEventListener("pointermove", move);
    window.addEventListener("pointerup", up);
  };

  const resizeByKey = (e: React.KeyboardEvent<HTMLDivElement>) => {
    const step = e.key === "ArrowRight" ? 16 : e.key === "ArrowLeft" ? -16 : 0;
    if (!step) return;
    e.preventDefault();
    saveSidebarWidth(width + (sidebarRight ? -step : step));
  };

  const inputClass =
    "w-full px-3 py-2 text-sm rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition";
  const actionClass =
    "w-full flex items-center gap-2.5 px-3 py-2.5 text-xs font-medium text-foreground hover:bg-surface-subtle rounded-xl transition";
  const iconButton = "p-2 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition disabled:opacity-30";

  const navButton = (link: NavLink) => {
    const Icon = iconOf(link.icon);
    const active = current?.id === link.id;
    const badge = badgeOf(link.id);
    return (
      <button
        key={link.id}
        type="button"
        onClick={() => openNav(link.id)}
        aria-current={active ? "page" : undefined}
        className={`w-full flex items-center justify-between gap-2 px-3 py-2.5 rounded-xl text-xs font-semibold transition ${
          active ? "bg-primary text-on-primary shadow-xs" : "text-foreground hover:bg-surface-subtle"
        }`}
      >
        <span className="flex items-center gap-2.5 min-w-0">
          <Icon className="w-4 h-4 shrink-0" />
          <span className="truncate">{link.label}</span>
        </span>
        {badge !== undefined && badge > 0 && (
          <span
            className={`px-2 py-0.5 rounded-full text-[10px] font-mono shrink-0 ${
              active ? "bg-on-primary/20 text-on-primary" : "bg-surface-subtle text-primary border border-border"
            }`}
          >
            {badge}
          </span>
        )}
      </button>
    );
  };

  const navList = (
    <nav className="px-3 py-3 space-y-4" aria-label="Menu Portal">
      {navGroups.map((group, i) => (
        <div key={group.title || i} className="space-y-1">
          {group.title && (
            <div className="px-3 pb-1 text-[11px] font-semibold text-muted uppercase tracking-wider">{group.title}</div>
          )}
          {group.items.map(navButton)}
        </div>
      ))}
    </nav>
  );

  const branding = (
    <div className="flex items-center gap-3 min-w-0">
      <div className="w-9 h-9 rounded-xl bg-primary text-on-primary flex items-center justify-center shrink-0">
        <ShieldCheck className="w-5 h-5" />
      </div>
      <div className="min-w-0">
        <div className="text-sm font-bold text-foreground truncate">{orgName || siteName || "Portal Pengurus"}</div>
        <div className="text-[11px] text-muted truncate">
          Portal Pengurus · <span className="font-mono">{slug}</span>
        </div>
      </div>
    </div>
  );

  // Akun, tema, lihat website, dan keluar selalu milik OrgSpace (tidak bisa diatur portal.json).
  const accountActions = (
    <div className="p-3 border-t border-border space-y-1">
      <div className="flex items-center gap-2.5 px-3 py-2">
        <div className="w-8 h-8 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-semibold text-on-primary-container shrink-0">
          {initials}
        </div>
        <div className="min-w-0 flex-1">
          <div className="text-xs font-semibold text-foreground truncate">{user.username}</div>
          <div className="text-[10px] uppercase font-mono font-semibold tracking-wider text-muted">{user.role}</div>
        </div>
      </div>
      <button type="button" onClick={toggleTheme} className={actionClass}>
        {isDarkMode ? <Sun className="w-4 h-4 text-accent" /> : <Moon className="w-4 h-4 text-primary" />}
        <span>{isDarkMode ? "Mode Terang" : "Mode Gelap"}</span>
      </button>
      <Link href={siteHref} target="_blank" className={actionClass}>
        <ExternalLink className="w-4 h-4 text-muted" />
        <span>Lihat Website</span>
      </Link>
      <button
        type="button"
        onClick={() => {
          setMobileSidebarOpen(false);
          handleLogout();
        }}
        className="w-full flex items-center gap-2.5 px-3 py-2.5 text-xs font-semibold text-error hover:bg-error/10 rounded-xl transition"
      >
        <LogOut className="w-4 h-4" />
        <span>Keluar dari Portal</span>
      </button>
    </div>
  );

  const itemActions = (item: (typeof modItems)[number], idx: number) => (
    <div className="flex items-center gap-0.5 shrink-0">
      <button
        type="button"
        onClick={() => handleMoveItem(idx, -1)}
        disabled={idx === 0}
        className={iconButton}
        title="Naikkan urutan"
        aria-label="Naikkan urutan"
      >
        <ArrowUp className="w-4 h-4" />
      </button>
      <button
        type="button"
        onClick={() => handleMoveItem(idx, 1)}
        disabled={idx === modItems.length - 1}
        className={iconButton}
        title="Turunkan urutan"
        aria-label="Turunkan urutan"
      >
        <ArrowDown className="w-4 h-4" />
      </button>
      <button type="button" onClick={() => startEditItem(item)} className={iconButton} title="Ubah" aria-label="Ubah">
        <Pencil className="w-4 h-4" />
      </button>
      <button
        type="button"
        onClick={() => modName && handleDeleteCollectionItem(modName, item.id)}
        className="p-2 text-muted hover:text-error hover:bg-error/10 rounded-lg transition"
        title="Hapus"
        aria-label="Hapus"
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  );

  const fieldInput = (field: ModuleField, idx: number) => {
    const id = `field-${idx}`;
    const help = modSettings?.fields?.[field.key]?.help;
    return (
      <div key={field.key || idx} className="space-y-1.5">
        <label htmlFor={field.type === "gambar" ? undefined : id} className="block text-xs font-semibold text-foreground">
          {fieldLabel(field.key)}
        </label>
        {field.type === "paragraf" ? (
          <textarea
            id={id}
            rows={4}
            value={field.value}
            onChange={(e) => updateField(idx, { value: e.target.value })}
            className={inputClass}
          />
        ) : field.type === "gambar" ? (
          <div className="flex items-center gap-3">
            {field.value ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={field.value}
                alt={fieldLabel(field.key)}
                className="w-16 h-16 rounded-xl object-cover border border-border bg-surface"
              />
            ) : (
              <div className="w-16 h-16 rounded-xl border border-dashed border-border grid place-items-center text-muted bg-surface">
                <ImagePlus className="w-5 h-5" />
              </div>
            )}
            <div className="flex-1 space-y-1">
              <label className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium rounded-xl border border-border bg-surface text-foreground cursor-pointer hover:bg-surface-subtle transition">
                {uploadingField === idx ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin text-primary" />
                ) : (
                  <ImagePlus className="w-3.5 h-3.5" />
                )}
                <span>{uploadingField === idx ? "Mengunggah..." : field.value ? "Ganti foto" : "Pilih foto"}</span>
                <input
                  type="file"
                  accept="image/png,image/jpeg,image/webp"
                  className="sr-only"
                  disabled={uploadingField !== null}
                  onChange={(e) => {
                    handleUploadPhoto(idx, e.target.files?.[0]);
                    e.target.value = "";
                  }}
                />
              </label>
              {field.value && (
                <button
                  type="button"
                  onClick={() => updateField(idx, { value: "" })}
                  className="block text-[11px] text-muted hover:text-error transition"
                >
                  Hapus foto
                </button>
              )}
            </div>
          </div>
        ) : (
          <input
            id={id}
            type={field.type === "angka" ? "number" : field.type === "tanggal" ? "date" : field.type === "link" ? "url" : "text"}
            placeholder={field.type === "link" ? "https://..." : undefined}
            value={field.value}
            onChange={(e) => updateField(idx, { value: e.target.value })}
            className={inputClass}
          />
        )}
        {help && <p className="text-[11px] text-muted leading-snug">{help}</p>}
      </div>
    );
  };

  const moduleLabelText = modName ? moduleLabel(portal, modName) : "";

  return (
    <div className="min-h-screen bg-background text-foreground antialiased font-sans transition-colors duration-200">
      {themeTag}
      {toast && (
        <div
          role="status"
          className={`fixed ${bottomLinks.length ? "bottom-24" : "bottom-6"} md:bottom-6 right-6 z-50 flex items-center gap-2.5 px-4 py-3 rounded-xl shadow-lg text-xs font-semibold transition ${
            toast.type === "success"
              ? "bg-primary text-on-primary border border-primary/30"
              : "bg-error text-white border border-error/50"
          }`}
        >
          {toast.type === "success" ? (
            <CheckCircle2 className="w-4 h-4 text-accent" />
          ) : (
            <AlertCircle className="w-4 h-4 text-white" />
          )}
          <span>{toast.message}</span>
        </div>
      )}

      <div className={`flex min-h-screen ${sidebarRight ? "flex-row-reverse" : ""}`}>
        {/* Sidebar desktop: posisi, lebar, dan bisa digeser diatur portal.json (layout.sidebar). */}
        <aside
          className={`hidden md:flex flex-col shrink-0 bg-surface border-border sticky top-0 h-screen ${
            sidebarRight ? "border-l" : "border-r"
          }`}
          style={{ width }}
        >
          <div className="h-16 px-4 flex items-center border-b border-border">{branding}</div>
          <div className="flex-1 overflow-y-auto">{navList}</div>
          {accountActions}
          {resizable && (
            <div
              role="separator"
              aria-orientation="vertical"
              aria-label="Ubah lebar menu"
              aria-valuemin={SIDEBAR_MIN}
              aria-valuemax={SIDEBAR_MAX}
              aria-valuenow={width}
              tabIndex={0}
              onPointerDown={startResize}
              onKeyDown={resizeByKey}
              title="Geser untuk mengubah lebar menu"
              className={`absolute top-0 ${sidebarRight ? "-left-1.5" : "-right-1.5"} w-3 h-full cursor-col-resize group z-10 focus:outline-none`}
            >
              <span className="block mx-auto w-0.5 h-full bg-transparent group-hover:bg-primary/40 group-focus-visible:bg-primary transition" />
            </div>
          )}
        </aside>

        <div className="flex-1 min-w-0 flex flex-col">
          {demo && (
            <div className="bg-primary-container text-on-primary-container text-xs text-center px-4 py-2">
              Mode demo: semua data contoh dan tidak tersimpan.{" "}
              <Link href={siteHref} className="font-semibold underline">
                Kembali ke preview website
              </Link>
            </div>
          )}
          {/* Bar atas HP */}
          <header className="md:hidden bg-surface border-b border-border sticky top-0 z-30">
            <div className="h-14 px-4 flex items-center justify-between gap-2">
              <div className="flex items-center gap-2 min-w-0">
                {bottomLinks.length === 0 && (
                  <button
                    type="button"
                    onClick={() => setMobileSidebarOpen(true)}
                    className="p-2 -ml-2 rounded-xl text-foreground hover:bg-surface-subtle transition"
                    aria-label="Buka menu navigasi"
                  >
                    <Menu className="w-5 h-5" />
                  </button>
                )}
                <h1 className="text-sm font-bold text-foreground truncate">{current?.label || "Portal Pengurus"}</h1>
              </div>
              <div className="flex items-center gap-1">
                <button
                  type="button"
                  onClick={toggleTheme}
                  className="p-2 rounded-xl text-muted hover:text-foreground hover:bg-surface-subtle transition"
                  aria-label={isDarkMode ? "Ganti ke Mode Terang" : "Ganti ke Mode Gelap"}
                >
                  {isDarkMode ? <Sun className="w-4 h-4 text-accent" /> : <Moon className="w-4 h-4 text-primary" />}
                </button>
                <button
                  type="button"
                  onClick={() => setMobileSidebarOpen(true)}
                  className="w-9 h-9 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-semibold text-on-primary-container"
                  aria-label="Buka menu pengurus"
                >
                  {initials}
                </button>
              </div>
            </div>
          </header>

          {/* Header banner dari portal.json (layout.header); tanpa itu Portal memakai header minimal. */}
          {header?.variant === "banner" && (
            <div className={`relative overflow-hidden ${bannerImage ? "text-white bg-foreground" : "bg-primary text-on-primary"}`}>
              {bannerImage && (
                <>
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={bannerImage} alt="" className="absolute inset-0 w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-linear-to-r from-black/75 via-black/45 to-black/15" />
                </>
              )}
              <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 md:py-12">
                <p className="text-[11px] font-semibold uppercase tracking-wider opacity-80">Portal Pengurus</p>
                <h2 className="text-xl md:text-3xl font-bold tracking-tight mt-1">
                  {header.title || orgName || siteName}
                </h2>
                {header.subtitle && <p className="text-sm md:text-base opacity-90 mt-2 max-w-2xl">{header.subtitle}</p>}
              </div>
            </div>
          )}

          <main
            className={`max-w-6xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-6 md:py-8 flex-1 ${
              bottomLinks.length ? "pb-28 md:pb-8" : ""
            }`}
          >
            {/* RINGKASAN */}
            {current?.id === "overview" && (
              <div className="space-y-6">
                <div>
                  <h2 className="text-xl font-bold tracking-tight text-foreground">Ringkasan Website</h2>
                  <p className="text-sm text-muted mt-0.5">Data yang Anda ubah di sini langsung tampil di website.</p>
                </div>

                <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
                  {[
                    {
                      label: "Formulir Masuk",
                      value: stats.submissionsCount,
                      note: "Respon formulir",
                      icon: Inbox,
                      go: canViewSubmissions ? ("submissions" as NavId) : null,
                    },
                    { label: "Berita", value: stats.newsCount, note: "Artikel aktif", icon: Newspaper, go: "news" as NavId },
                    {
                      label: "Data Website",
                      value: stats.customItemsCount,
                      note: "Item di semua menu data",
                      icon: Boxes,
                      go: firstModule ? firstModule.id : null,
                    },
                  ].map((kpi) => (
                    <button
                      key={kpi.label}
                      type="button"
                      disabled={!kpi.go}
                      onClick={() => kpi.go && openNav(kpi.go)}
                      className="text-left bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs transition enabled:hover:border-border-strong disabled:cursor-default"
                    >
                      <div className="flex items-center justify-between mb-2 sm:mb-3">
                        <span className="text-[11px] sm:text-xs font-semibold tracking-wider uppercase text-muted">
                          {kpi.label}
                        </span>
                        <div className="w-8 h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                          <kpi.icon className="w-4 h-4" />
                        </div>
                      </div>
                      <div className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">{kpi.value}</div>
                      <p className="text-[11px] sm:text-xs text-muted mt-1 leading-snug">{kpi.note}</p>
                    </button>
                  ))}
                  <div className="bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs">
                    <div className="flex items-center justify-between mb-2 sm:mb-3">
                      <span className="text-[11px] sm:text-xs font-semibold tracking-wider uppercase text-muted">
                        Masuk sebagai
                      </span>
                      <div className="w-8 h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                        <Users className="w-4 h-4" />
                      </div>
                    </div>
                    <div className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground capitalize">{user.role}</div>
                    <p className="text-[11px] sm:text-xs text-muted mt-1 leading-snug font-mono truncate">{user.username}</p>
                  </div>
                </div>

                {navLinks.some((l) => l.id.startsWith("mod:")) && (
                  <div className="space-y-3">
                    <h3 className="text-sm font-bold text-foreground">Data website</h3>
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-3">
                      {navLinks
                        .filter((l) => l.id.startsWith("mod:"))
                        .map((l) => {
                          const Icon = iconOf(l.icon);
                          const name = l.id.slice(4);
                          return (
                            <button
                              key={l.id}
                              type="button"
                              onClick={() => openNav(l.id)}
                              className="flex items-center gap-3 text-left bg-surface p-4 rounded-2xl border border-border shadow-xs hover:border-border-strong transition"
                            >
                              <div className="w-10 h-10 rounded-xl bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                                <Icon className="w-5 h-5" />
                              </div>
                              <div className="min-w-0 flex-1">
                                <div className="text-sm font-semibold text-foreground truncate">{l.label}</div>
                                <div className="text-xs text-muted truncate">
                                  {portal.modules?.[name]?.description || `${(collections[name] || []).length} item`}
                                </div>
                              </div>
                            </button>
                          );
                        })}
                    </div>
                  </div>
                )}

                <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs">
                  <div className="flex items-center gap-2 mb-2">
                    <div className="w-7 h-7 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                      <ShieldCheck className="w-4 h-4" />
                    </div>
                    <h3 className="text-sm font-bold text-foreground">Akses Portal</h3>
                  </div>
                  <p className="text-xs text-muted leading-relaxed mt-2">
                    Portal punya tiga akses: Pemilik, Admin, dan Editor. Username dan password tiap akses diatur Pemilik di
                    app OrgSpace (Kelola organisasi → Akses Portal website) dan berlaku untuk semua website organisasi.
                  </p>
                </div>
              </div>
            )}

            {/* KOTAK MASUK FORMULIR */}
            {current?.id === "submissions" && canViewSubmissions && (
              <div className="space-y-5">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h2 className="text-xl font-bold tracking-tight text-foreground">{current.label}</h2>
                    <p className="text-sm text-muted mt-0.5">
                      Isian pengunjung dari formulir di website. Hanya Pemilik dan Admin yang bisa melihatnya.
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => token && loadSubmissions(token)}
                    className="inline-flex items-center gap-1.5 text-xs font-medium px-3 py-2 rounded-lg border border-border bg-surface hover:bg-surface-subtle text-foreground transition shrink-0"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                    <span>Segarkan</span>
                  </button>
                </div>

                {loadingSubmissions ? (
                  <div className="py-16 text-center text-muted">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                    <p className="text-xs">Memuat formulir masuk...</p>
                  </div>
                ) : submissions.length === 0 ? (
                  <div className="bg-surface p-12 text-center rounded-2xl border border-border text-muted text-xs shadow-xs">
                    Belum ada isian formulir dari pengunjung website.
                  </div>
                ) : (
                  <div className="bg-surface rounded-2xl border border-border overflow-hidden shadow-xs">
                    <div className="overflow-x-auto">
                      <table className="w-full text-left text-xs text-foreground">
                        <thead className="bg-surface-subtle border-b border-border text-muted tracking-wider">
                          <tr>
                            <th className="py-3 px-4 font-semibold">Waktu Masuk</th>
                            <th className="py-3 px-4 font-semibold">Formulir</th>
                            <th className="py-3 px-4 font-semibold">Data Isian</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-border">
                          {submissions.map((sub) => (
                            <tr key={sub.id} className="hover:bg-surface-subtle/50 transition">
                              <td className="py-3 px-4 whitespace-nowrap text-muted font-mono text-xs">
                                {new Date(sub.createdAt).toLocaleString("id-ID")}
                              </td>
                              <td className="py-3 px-4 whitespace-nowrap">
                                <span className="bg-primary-container text-on-primary-container px-2 py-0.5 rounded font-semibold text-xs border border-border">
                                  {humanize(sub.formName)}
                                </span>
                              </td>
                              <td className="py-3 px-4">
                                <div className="flex flex-wrap gap-1.5">
                                  {Object.entries(sub.data || {}).map(([k, v]) => (
                                    <div
                                      key={k}
                                      className="inline-flex items-center gap-1 bg-surface-subtle border border-border px-2 py-0.5 rounded text-xs"
                                    >
                                      <span className="text-muted text-[11px]">{humanize(k)}:</span>
                                      <span className="font-medium text-foreground">{String(v)}</span>
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

            {/* BERITA */}
            {current?.id === "news" && (
              <div className="space-y-5">
                <div>
                  <h2 className="text-xl font-bold tracking-tight text-foreground">{current.label}</h2>
                  <p className="text-sm text-muted mt-0.5">Artikel, pengumuman, dan agenda yang tampil di bagian berita website.</p>
                </div>

                <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                  <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs h-fit xl:sticky xl:top-6">
                    <h3 className="text-sm font-bold text-foreground mb-4 flex items-center gap-2">
                      <Plus className="w-4 h-4 text-primary" />
                      <span>Terbitkan Berita</span>
                    </h3>
                    <form onSubmit={handleCreateNews} className="space-y-4">
                      <div className="space-y-1.5">
                        <label htmlFor="news-title" className="block text-xs font-semibold text-foreground">
                          Judul <span className="text-error">*</span>
                        </label>
                        <input
                          id="news-title"
                          type="text"
                          required
                          placeholder="Contoh: Rapat Kerja Anggota 2026"
                          value={newTitle}
                          onChange={(e) => setNewTitle(e.target.value)}
                          className={inputClass}
                        />
                      </div>
                      <div className="space-y-1.5">
                        <label htmlFor="news-summary" className="block text-xs font-semibold text-foreground">
                          Ringkasan
                        </label>
                        <input
                          id="news-summary"
                          type="text"
                          placeholder="Tampil di daftar berita"
                          value={newSummary}
                          onChange={(e) => setNewSummary(e.target.value)}
                          className={inputClass}
                        />
                      </div>
                      <div className="space-y-1.5">
                        <label htmlFor="news-content" className="block text-xs font-semibold text-foreground">
                          Isi lengkap
                        </label>
                        <textarea
                          id="news-content"
                          rows={5}
                          value={newContent}
                          onChange={(e) => setNewContent(e.target.value)}
                          className={inputClass}
                        />
                      </div>
                      <button
                        type="submit"
                        disabled={submittingNews}
                        className="w-full py-2.5 bg-primary hover:opacity-90 text-on-primary rounded-xl text-sm font-semibold shadow-xs transition disabled:opacity-50"
                      >
                        {submittingNews ? "Menerbitkan..." : "Terbitkan"}
                      </button>
                    </form>
                  </div>

                  <div className="xl:col-span-2 space-y-3">
                    <div className="flex items-center justify-between">
                      <h3 className="text-sm font-bold text-foreground">Berita aktif</h3>
                      <span className="text-xs text-muted font-mono">{newsList.length} berita</span>
                    </div>
                    {loadingNews ? (
                      <div className="py-12 text-center text-muted">
                        <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                        <p className="text-xs">Memuat berita...</p>
                      </div>
                    ) : newsList.length === 0 ? (
                      <div className="bg-surface p-8 text-center rounded-2xl border border-border text-muted text-xs shadow-xs">
                        Belum ada berita yang diterbitkan.
                      </div>
                    ) : (
                      newsList.map((item) => (
                        <div
                          key={item.id}
                          className="bg-surface p-5 rounded-2xl border border-border shadow-xs flex items-start justify-between gap-4 hover:border-border-strong transition"
                        >
                          <div className="space-y-1.5 flex-1 min-w-0">
                            <h4 className="text-sm font-bold text-foreground leading-snug">{item.title}</h4>
                            {item.summary && <p className="text-xs text-muted leading-relaxed">{item.summary}</p>}
                            {item.publishedAt && (
                              <div className="text-[11px] text-muted/80 font-mono pt-1">{item.publishedAt}</div>
                            )}
                          </div>
                          <button
                            type="button"
                            onClick={() => handleDeleteNews(item.id)}
                            className="p-2 text-muted hover:text-error hover:bg-error/10 rounded-lg transition shrink-0"
                            title="Hapus berita"
                            aria-label="Hapus berita"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* HALAMAN MODUL (satu menu per modul template) */}
            {modName && (
              <div className="space-y-5">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                    {createElement(iconOf(current?.icon), { className: "w-5 h-5" })}
                  </div>
                  <div className="min-w-0">
                    <h2 className="text-xl font-bold tracking-tight text-foreground">{current?.label}</h2>
                    <p className="text-sm text-muted mt-0.5">
                      {modFields
                        ? modSettings?.description || `Data ${moduleLabelText} yang tampil di website.`
                        : "Template yang dipakai sekarang tidak menampilkan data ini lagi. Anda masih bisa mengubah atau menghapusnya."}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                  {(modFields || editingId) && (
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs h-fit xl:sticky xl:top-6">
                      <h3 className="text-sm font-bold text-foreground mb-4 flex items-center gap-2">
                        {editingId ? <Pencil className="w-4 h-4 text-primary" /> : <Plus className="w-4 h-4 text-primary" />}
                        <span>{editingId ? `Ubah ${moduleLabelText}` : `Tambah ${moduleLabelText}`}</span>
                      </h3>
                      <form onSubmit={handleCreateCollectionItem} className="space-y-5">
                        {fieldGroups(
                          modSettings,
                          dynamicFields.map((f) => f.key)
                        ).map((group, gi) => (
                          <fieldset key={group.title || gi} className="space-y-4">
                            {group.title && (
                              <legend className="text-[11px] font-semibold text-muted uppercase tracking-wider pb-1">
                                {group.title}
                              </legend>
                            )}
                            {group.fields.map((key) => {
                              const idx = dynamicFields.findIndex((f) => f.key === key);
                              return fieldInput(dynamicFields[idx], idx);
                            })}
                          </fieldset>
                        ))}
                        <button
                          type="submit"
                          disabled={submittingColItem}
                          className="w-full py-2.5 bg-primary hover:opacity-90 text-on-primary rounded-xl text-sm font-semibold shadow-xs transition disabled:opacity-50"
                        >
                          {submittingColItem ? "Menyimpan..." : editingId ? "Simpan Perubahan" : "Simpan"}
                        </button>
                        {editingId && (
                          <button
                            type="button"
                            onClick={cancelEdit}
                            className="w-full py-2 text-xs font-semibold text-muted hover:text-foreground rounded-xl border border-border bg-surface hover:bg-surface-subtle transition"
                          >
                            Batal
                          </button>
                        )}
                      </form>
                    </div>
                  )}

                  <div className={`space-y-3 ${modFields || editingId ? "xl:col-span-2" : "xl:col-span-3"}`}>
                    <div className="flex items-center justify-between">
                      <h3 className="text-sm font-bold text-foreground">Daftar {moduleLabelText}</h3>
                      <span className="text-xs text-muted font-mono">{modItems.length} item</span>
                    </div>

                    {loadingCollections ? (
                      <div className="py-12 text-center text-muted">
                        <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                        <p className="text-xs">Memuat data...</p>
                      </div>
                    ) : modItems.length === 0 ? (
                      <div className="bg-surface p-8 text-center rounded-2xl border border-border text-muted text-xs shadow-xs">
                        Belum ada data. Isi formulir untuk menambahkan yang pertama.
                      </div>
                    ) : (
                      <>
                        {/* Tabel hanya di layar lebar; HP selalu memakai kartu. */}
                        {modSettings?.view === "tabel" && modFields && (
                          <div className="hidden md:block bg-surface rounded-2xl border border-border overflow-hidden shadow-xs">
                            <div className="overflow-x-auto">
                              <table className="w-full text-left text-xs text-foreground">
                                <thead className="bg-surface-subtle border-b border-border text-muted">
                                  <tr>
                                    {Object.keys(modFields).map((key) => (
                                      <th key={key} className="py-3 px-4 font-semibold whitespace-nowrap">
                                        {fieldLabel(key)}
                                      </th>
                                    ))}
                                    <th className="py-3 px-4">
                                      <span className="sr-only">Aksi</span>
                                    </th>
                                  </tr>
                                </thead>
                                <tbody className="divide-y divide-border">
                                  {modItems.map((item, idx) => (
                                    <tr
                                      key={item.id || idx}
                                      className={editingId === item.id ? "bg-primary-container/40" : "hover:bg-surface-subtle/50"}
                                    >
                                      {Object.keys(modFields).map((key) => (
                                        <td key={key} className="py-2.5 px-4 align-middle">
                                          {modFields[key] === "gambar" ? (
                                            item[key] ? (
                                              // eslint-disable-next-line @next/next/no-img-element
                                              <img
                                                src={String(item[key])}
                                                alt=""
                                                className="w-10 h-10 rounded-lg object-cover border border-border"
                                              />
                                            ) : null
                                          ) : (
                                            <span className="line-clamp-2">{shownValue(key, item[key])}</span>
                                          )}
                                        </td>
                                      ))}
                                      <td className="py-2 px-2 text-right">{itemActions(item, idx)}</td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          </div>
                        )}

                        <div
                          className={`grid grid-cols-1 sm:grid-cols-2 gap-4 ${
                            modSettings?.view === "tabel" && modFields ? "md:hidden" : ""
                          } ${modFields || editingId ? "" : "xl:grid-cols-3"}`}
                        >
                          {modItems.map((item, idx) => {
                            const image = card.image ? item[card.image] : null;
                            return (
                              <div
                                key={item.id || idx}
                                className={`bg-surface rounded-2xl border shadow-xs overflow-hidden flex flex-col transition hover:border-border-strong ${
                                  editingId === item.id ? "border-primary ring-1 ring-primary" : "border-border"
                                }`}
                              >
                                {image && (
                                  // eslint-disable-next-line @next/next/no-img-element
                                  <img src={String(image)} alt="" className="w-full aspect-video object-cover bg-surface-subtle" />
                                )}
                                <div className="p-4 flex-1 space-y-1">
                                  {card.title ? (
                                    <>
                                      <h4 className="text-sm font-bold text-foreground leading-snug line-clamp-2">
                                        {shownValue(card.title, item[card.title]) || "(tanpa judul)"}
                                      </h4>
                                      {card.subtitle && item[card.subtitle] && (
                                        <p className="text-xs text-muted line-clamp-2">{shownValue(card.subtitle, item[card.subtitle])}</p>
                                      )}
                                    </>
                                  ) : (
                                    <div className="flex flex-wrap gap-1.5">
                                      {Object.entries(item)
                                        .filter(([k]) => k !== "id")
                                        .map(([k, v]) => (
                                          <span key={k} className="text-xs bg-surface-subtle border border-border px-2 py-0.5 rounded-lg">
                                            <span className="text-muted">{humanize(k)}:</span> {String(v)}
                                          </span>
                                        ))}
                                    </div>
                                  )}
                                </div>
                                {item.id && (
                                  <div className="px-2 py-1.5 border-t border-border flex justify-end">{itemActions(item, idx)}</div>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </div>
            )}
          </main>
        </div>
      </div>

      {/* Drawer HP: semua menu + akun. Muncul dari sisi yang sama dengan sidebar desktop. */}
      <div
        className={`fixed inset-0 z-50 md:hidden transition-all duration-300 ${
          mobileSidebarOpen ? "visible pointer-events-auto" : "invisible pointer-events-none"
        }`}
        role="dialog"
        aria-modal="true"
        aria-label="Menu Portal"
      >
        <div
          className={`fixed inset-0 bg-black/60 transition-opacity duration-300 ${mobileSidebarOpen ? "opacity-100" : "opacity-0"}`}
          onClick={() => setMobileSidebarOpen(false)}
          aria-hidden="true"
        />
        <div
          className={`fixed inset-y-0 ${sidebarRight ? "right-0 border-l" : "left-0 border-r"} w-72 max-w-[85vw] bg-surface border-border shadow-2xl flex flex-col transform transition-transform duration-300 ease-out ${
            mobileSidebarOpen ? "translate-x-0" : sidebarRight ? "translate-x-full" : "-translate-x-full"
          }`}
        >
          <div className="h-14 px-4 flex items-center justify-between gap-2 border-b border-border">
            {branding}
            <button
              type="button"
              onClick={() => setMobileSidebarOpen(false)}
              className="p-2 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition shrink-0"
              aria-label="Tutup menu"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
          <div className="flex-1 overflow-y-auto">{navList}</div>
          <div className="pb-[env(safe-area-inset-bottom)]">{accountActions}</div>
        </div>
      </div>

      {/* Menu bawah HP (portal.json mobile.nav = "bottom"): maks. 4 menu + Lainnya. */}
      {bottomLinks.length > 0 && (
        <nav
          className="md:hidden fixed bottom-0 inset-x-0 z-40 bg-surface border-t border-border pb-[env(safe-area-inset-bottom)]"
          aria-label="Menu cepat"
        >
          <div className="grid" style={{ gridTemplateColumns: `repeat(${bottomLinks.length + 1}, minmax(0, 1fr))` }}>
            {bottomLinks.map((link) => {
              const Icon = iconOf(link.icon);
              const active = current?.id === link.id;
              return (
                <button
                  key={link.id}
                  type="button"
                  onClick={() => openNav(link.id)}
                  aria-current={active ? "page" : undefined}
                  className={`flex flex-col items-center justify-center gap-1 min-h-[56px] px-1 text-[10px] font-semibold transition ${
                    active ? "text-primary" : "text-muted hover:text-foreground"
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  <span className="truncate max-w-full">{link.label}</span>
                </button>
              );
            })}
            <button
              type="button"
              onClick={() => setMobileSidebarOpen(true)}
              className="flex flex-col items-center justify-center gap-1 min-h-[56px] px-1 text-[10px] font-semibold text-muted hover:text-foreground transition"
            >
              <Menu className="w-5 h-5" />
              <span>Lainnya</span>
            </button>
          </div>
        </nav>
      )}
    </div>
  );
}
