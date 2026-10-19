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
  Users,
  UserPlus,
  Shield,
  Pencil,
  ArrowUp,
  ArrowDown,
  ImagePlus,
  Globe,
  Menu,
  X,
  ChevronRight,
  Eye,
  EyeOff,
  Sun,
  Moon,
} from "lucide-react";
import type { FieldType } from "@/lib/types";

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

interface ModuleField {
  key: string;
  value: string;
  type: FieldType;
  fixed?: boolean;
}

const FIELD_TYPE_LABELS: Record<FieldType, string> = {
  teks: "Teks Singkat",
  paragraf: "Paragraf",
  angka: "Angka",
  tanggal: "Tanggal",
  link: "Tautan URL",
  gambar: "Foto",
};

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

interface Member {
  userId: string;
  name: string;
  email: string;
  role: "owner" | "admin" | "editor";
  positionTitle?: string;
  joinedAt: string;
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
  const [activeTab, setActiveTab] = useState<"overview" | "submissions" | "news" | "collections" | "members">("overview");
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
  const [newColName, setNewColName] = useState("");
  const [templateModules, setTemplateModules] = useState<Record<string, Record<string, FieldType>>>({});
  const [dynamicFields, setDynamicFields] = useState<ModuleField[]>([{ key: "", value: "", type: "teks" }]);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [uploadingField, setUploadingField] = useState<number | null>(null);

  const templateFields = (name: string, modules = templateModules): ModuleField[] =>
    Object.entries(modules[name] || {}).map(([key, type]) => ({ key, value: "", type, fixed: true }));

  const chooseCollection = (name: string, modules = templateModules) => {
    setSelectedCol(name);
    setEditingId(null);
    const fields = templateFields(name, modules);
    if (fields.length > 0) {
      setDynamicFields(fields);
    }
  };

  const fieldTypeOf = (colName: string, key: string): FieldType => templateModules[colName]?.[key] || "teks";
  const [submittingColItem, setSubmittingColItem] = useState(false);

  // Members & Access Control state
  const [members, setMembers] = useState<Member[]>([]);
  const [loadingMembers, setLoadingMembers] = useState(false);
  const [newMemberEmail, setNewMemberEmail] = useState("");
  const [newMemberName, setNewMemberName] = useState("");
  const [newMemberPosition, setNewMemberPosition] = useState("");
  const [newMemberRole, setNewMemberRole] = useState<"admin" | "editor">("editor");
  const [newMemberPassword, setNewMemberPassword] = useState("password123");
  const [submittingMember, setSubmittingMember] = useState(false);

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
        const modules: Record<string, Record<string, FieldType>> = data.modules || {};
        setTemplateModules(modules);
        const first = Object.keys(modules)[0];
        if (first) chooseCollection(first, modules);
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
    loadMembers(jwt);
  };

  const loadMembers = async (jwt: string) => {
    setLoadingMembers(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/members`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setMembers(data);
      }
    } catch (e) {
      console.error("Gagal memuat daftar pengurus", e);
    } finally {
      setLoadingMembers(false);
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !newMemberEmail.trim()) return;
    setSubmittingMember(true);
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/members`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          email: newMemberEmail.trim(),
          name: newMemberName.trim(),
          role: newMemberRole,
          positionTitle: newMemberPosition.trim(),
          password: newMemberPassword.trim() || "password123",
        }),
      });
      const data = await res.json();
      if (res.ok) {
        showToast("Pengurus baru berhasil ditambahkan!");
        setNewMemberEmail("");
        setNewMemberName("");
        setNewMemberPosition("");
        setNewMemberPassword("password123");
        loadMembers(token);
      } else {
        showToast(data.message || "Gagal menambahkan pengurus", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    } finally {
      setSubmittingMember(false);
    }
  };

  const handleUpdateRole = async (targetUserId: string, newRole: "admin" | "editor") => {
    if (!token) return;
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/members/${targetUserId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ role: newRole }),
      });
      const data = await res.json();
      if (res.ok) {
        showToast("Peran pengurus berhasil diperbarui!");
        loadMembers(token);
      } else {
        showToast(data.message || "Gagal memperbarui peran", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    }
  };

  const handleRemoveMember = async (targetUserId: string, memberName: string) => {
    if (!token || !confirm(`Yakin ingin mencabut hak akses pengurus ${memberName}?`)) return;
    try {
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/members/${targetUserId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      if (res.ok) {
        showToast("Hak akses pengurus berhasil dicabut!");
        loadMembers(token);
      } else {
        showToast(data.message || "Gagal mencabut hak akses", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    }
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
        if (!selectedCol && colKeys.length > 0) {
          chooseCollection(colKeys[0]);
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
      showToast("Berhasil masuk ke portal pengurus!");
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

    const colName = editingId ? selectedCol : (newColName.trim() || selectedCol).toLowerCase();
    if (!colName) {
      showToast("Pilih atau masukkan nama modul koleksi", "error");
      return;
    }

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
      const itemPath = editingId ? `/${editingId}` : "";
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/collections/${colName}${itemPath}`, {
        method: editingId ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payloadData),
      });

      if (res.ok) {
        showToast(editingId ? "Perubahan item berhasil disimpan!" : `Item berhasil ditambahkan ke modul '${colName}'!`);
        setNewColName("");
        setEditingId(null);
        setDynamicFields(dynamicFields.map((f) => ({ ...f, value: "" })));
        setSelectedCol(colName);
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
    setDynamicFields([...dynamicFields, { key: "", value: "", type: "teks" }]);
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
    setNewColName("");
    setDynamicFields(fields);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    const fields = templateFields(selectedCol);
    setDynamicFields(fields.length > 0 ? fields : dynamicFields.map((f) => ({ ...f, value: "" })));
  };

  const handleUploadPhoto = async (index: number, file: File | undefined) => {
    if (!token || !file) return;
    setUploadingField(index);
    try {
      const body = new FormData();
      body.append("file", await shrinkPhoto(file), "foto.jpg");
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/media`, {
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
      const res = await fetch(`${backendUrl}/api/public/sites/${slug}/admin/collections/${selectedCol}/order`, {
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

  const removeDynamicField = (index: number) => {
    setDynamicFields(dynamicFields.filter((_, i) => i !== index));
  };

  // 1. Loading Screen
  if (loadingAuth) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center text-foreground transition-colors duration-200">
        <Loader2 className="w-8 h-8 animate-spin text-primary mb-3" />
        <p className="text-sm font-medium text-muted">Memeriksa sesi pengurus...</p>
      </div>
    );
  }

  // 2. Login Screen (Unauthenticated)
  if (!token || !user) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center px-4 py-12 antialiased transition-colors duration-200 relative">
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
                  Email Akun Pengurus
                </label>
                <input
                  type="email"
                  required
                  placeholder="pengurus@organisasi.id"
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
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
                href={`/${slug}`}
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

  // 3. Authenticated Dashboard View
  interface TabItem {
    id: "overview" | "submissions" | "news" | "collections" | "members";
    label: string;
    icon: React.ComponentType<{ className?: string }>;
    badge?: number;
  }

  const tabs: TabItem[] = [
    { id: "overview", label: "Ringkasan", icon: LayoutDashboard },
    { id: "submissions", label: "Kotak Masuk Form", icon: Inbox, badge: stats.submissionsCount },
    { id: "news", label: "Berita & Agenda", icon: Newspaper, badge: stats.newsCount },
    { id: "collections", label: "Modul Kustom", icon: Boxes, badge: stats.customItemsCount },
    { id: "members", label: "Pengurus & Hak Akses", icon: Users, badge: members.length },
  ];

  return (
    <div className="min-h-screen bg-background text-foreground antialiased flex flex-col font-sans transition-colors duration-200">
      {/* Toast Notification */}
      {toast && (
        <div
          className={`fixed bottom-6 right-6 z-50 flex items-center gap-2.5 px-4 py-3 rounded-xl shadow-lg text-xs font-semibold transition ${
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

      {/* Top Header */}
      <header className="bg-surface border-b border-border sticky top-0 z-30 transition-colors duration-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="h-16 flex items-center justify-between gap-3">
            {/* Mobile Header Bar (md:hidden) */}
            <div className="flex items-center gap-2.5 min-w-0 md:hidden">
              {/* Mobile Hamburger Button on the LEFT */}
              <button
                type="button"
                onClick={() => setMobileSidebarOpen(true)}
                className="p-2 -ml-1 rounded-xl text-foreground hover:bg-surface-subtle transition flex items-center justify-center shrink-0"
                aria-label="Buka menu navigasi"
              >
                <Menu className="w-5 h-5" />
              </button>

              <h1 className="text-sm font-bold text-foreground truncate">
                {tabs.find((t) => t.id === activeTab)?.label || "Portal Pengurus"}
              </h1>
            </div>

            {/* Mobile Actions on the Right (Theme Toggle + User Avatar) */}
            <div className="flex items-center gap-1.5 md:hidden">
              <button
                type="button"
                onClick={toggleTheme}
                className="p-2 rounded-xl text-muted hover:text-foreground hover:bg-surface-subtle transition"
                aria-label={isDarkMode ? "Ganti ke Mode Terang" : "Ganti ke Mode Gelap"}
                title={isDarkMode ? "Mode Terang" : "Mode Gelap"}
              >
                {isDarkMode ? <Sun className="w-4 h-4 text-accent" /> : <Moon className="w-4 h-4 text-primary" />}
              </button>
              <button
                type="button"
                onClick={() => setMobileSidebarOpen(true)}
                className="w-8 h-8 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-semibold text-on-primary-container"
                title="Profil & Menu"
                aria-label="Buka menu pengurus"
              >
                {(user.name || user.email).substring(0, 2).toUpperCase()}
              </button>
            </div>

            {/* Desktop Left Branding (hidden md:flex) */}
            <div className="hidden md:flex items-center gap-3 min-w-0">
              <div className="w-9 h-9 rounded-xl bg-primary text-on-primary flex items-center justify-center font-bold text-sm shadow-xs shrink-0">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <h1 className="text-sm font-bold text-foreground truncate">
                    {orgName || siteName || "Portal Pengurus"}
                  </h1>
                  <span className="font-mono text-xs bg-surface-subtle text-primary border border-border px-2 py-0.5 rounded-md shrink-0">
                    {slug}
                  </span>
                </div>
                <p className="text-xs text-muted truncate">Portal Pengurus Website</p>
              </div>
            </div>

            {/* Desktop Actions & User Profile */}
            <div className="hidden md:flex items-center gap-3">
              <button
                type="button"
                onClick={toggleTheme}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-border bg-surface hover:bg-surface-subtle text-muted hover:text-foreground text-xs font-medium transition"
                aria-label={isDarkMode ? "Ganti ke Mode Terang" : "Ganti ke Mode Gelap"}
                title={isDarkMode ? "Mode Terang" : "Mode Gelap"}
              >
                {isDarkMode ? <Sun className="w-3.5 h-3.5 text-accent" /> : <Moon className="w-3.5 h-3.5 text-primary" />}
                <span>{isDarkMode ? "Terang" : "Gelap"}</span>
              </button>

              <Link
                href={`/${slug}`}
                target="_blank"
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-border bg-surface hover:bg-surface-subtle text-foreground text-xs font-medium transition"
              >
                <ExternalLink className="w-3.5 h-3.5" />
                <span>Lihat Website</span>
              </Link>

              <div className="h-5 w-px bg-border" />

              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-semibold text-on-primary-container">
                  {(user.name || user.email).substring(0, 2).toUpperCase()}
                </div>
                <div className="text-left">
                  <div className="text-xs font-semibold text-foreground leading-tight">{user.name}</div>
                  <div className="text-[10px] uppercase font-mono font-semibold tracking-wider text-muted">
                    {user.role}
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  title="Keluar dari portal"
                  className="p-2 text-muted hover:text-error hover:bg-error/10 rounded-lg transition ml-1"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          {/* Desktop Navigation Bar (Tabs) */}
          <nav className="hidden md:flex space-x-1 overflow-x-auto border-t border-border -mb-px">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const active = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 py-3 px-3.5 text-xs font-medium border-b-2 whitespace-nowrap transition ${
                    active
                      ? "border-primary text-primary font-bold"
                      : "border-transparent text-muted hover:text-foreground hover:border-border-strong"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{tab.label}</span>
                  {tab.badge !== undefined && tab.badge > 0 && (
                    <span
                      className={`ml-1 px-1.5 py-0.2 rounded-full text-[10px] font-mono ${
                        active ? "bg-primary text-on-primary" : "bg-surface-subtle text-primary"
                      }`}
                    >
                      {tab.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>
      </header>

      {/* Mobile Off-Canvas Sidebar Drawer with Smooth Transition */}
      <div
        className={`fixed inset-0 z-50 md:hidden transition-all duration-300 ease-in-out ${
          mobileSidebarOpen ? "visible pointer-events-auto" : "invisible pointer-events-none"
        }`}
        role="dialog"
        aria-modal="true"
      >
        {/* Backdrop Overlay with smooth fade */}
        <div
          className={`fixed inset-0 bg-black/60 backdrop-blur-xs transition-opacity duration-300 ease-in-out ${
            mobileSidebarOpen ? "opacity-100" : "opacity-0"
          }`}
          onClick={() => setMobileSidebarOpen(false)}
          aria-hidden="true"
        />

        {/* Drawer Panel with smooth slide-in from left */}
        <div
          className={`fixed inset-y-0 left-0 w-72 max-w-[85vw] bg-surface border-r border-border shadow-2xl flex flex-col justify-between z-50 transform transition-transform duration-300 ease-out ${
            mobileSidebarOpen ? "translate-x-0" : "-translate-x-full"
          }`}
        >
            <div className="flex flex-col flex-1 overflow-y-auto">
              {/* Organization Branding prominently showcased inside Sidebar */}
              <div className="p-4 border-b border-border flex items-start justify-between gap-3 bg-surface">
                <div className="flex items-start gap-3 min-w-0">
                  <div className="w-10 h-10 rounded-xl bg-primary text-on-primary flex items-center justify-center font-bold text-sm shadow-xs shrink-0 mt-0.5">
                    <ShieldCheck className="w-5 h-5" />
                  </div>
                  <div className="min-w-0">
                    <div className="text-[11px] font-bold text-muted uppercase tracking-wider">
                      Organisasi
                    </div>
                    <div className="text-sm font-bold text-foreground leading-snug break-words">
                      {orgName || siteName || "Portal Pengurus"}
                    </div>
                    <div className="flex items-center gap-1.5 mt-1">
                      <span className="font-mono text-[11px] bg-surface-subtle text-primary border border-border px-2 py-0.5 rounded-md font-semibold">
                        {slug}
                      </span>
                    </div>
                  </div>
                </div>
                <button
                  onClick={() => setMobileSidebarOpen(false)}
                  className="p-1.5 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition shrink-0"
                  aria-label="Tutup menu"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {/* User Profile Card */}
              <div className="p-3.5 m-3 bg-surface-subtle rounded-xl border border-border">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-bold text-on-primary-container shrink-0">
                    {(user.name || user.email).substring(0, 2).toUpperCase()}
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="text-xs font-bold text-foreground truncate leading-tight">{user.name}</div>
                    <div className="text-[11px] text-muted font-mono truncate">{user.email}</div>
                    <div className="mt-1">
                      <span className="text-[10px] font-mono font-semibold uppercase px-2 py-0.5 rounded-full bg-surface border border-border text-primary">
                        {user.role}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Navigation Menu List */}
              <div className="px-3 py-1 space-y-1">
                <div className="px-3 py-1 text-[11px] font-semibold text-muted uppercase tracking-wider">
                  Menu Utama
                </div>
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  const active = activeTab === tab.id;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => {
                        setActiveTab(tab.id);
                        setMobileSidebarOpen(false);
                      }}
                      className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition ${
                        active
                          ? "bg-primary text-on-primary shadow-xs font-bold"
                          : "text-foreground hover:bg-surface-subtle"
                      }`}
                    >
                      <div className="flex items-center gap-2.5">
                        <Icon className="w-4 h-4" />
                        <span>{tab.label}</span>
                      </div>
                      {tab.badge !== undefined && tab.badge > 0 && (
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-mono ${
                            active ? "bg-white/20 text-white dark:bg-black/20" : "bg-surface-subtle text-primary border border-border"
                          }`}
                        >
                          {tab.badge}
                        </span>
                      )}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Bottom Actions: Theme Toggle, Lihat Website & Tombol Logout */}
            <div className="p-4 pb-8 border-t border-border space-y-1.5">
              <button
                type="button"
                onClick={toggleTheme}
                className="w-full flex items-center justify-between px-3 py-2.5 text-xs font-medium text-foreground hover:bg-surface-subtle rounded-xl transition"
              >
                <div className="flex items-center gap-2.5">
                  {isDarkMode ? <Sun className="w-4 h-4 text-accent" /> : <Moon className="w-4 h-4 text-primary" />}
                  <span>Tema Tampilan</span>
                </div>
                <span className="text-[11px] font-mono px-2 py-0.5 rounded-md bg-surface-subtle text-primary border border-border">
                  {isDarkMode ? "🌙 Gelap" : "☀️ Terang"}
                </span>
              </button>

              <Link
                href={`/${slug}`}
                target="_blank"
                onClick={() => setMobileSidebarOpen(false)}
                className="w-full flex items-center gap-2.5 px-3 py-2.5 text-xs font-medium text-foreground hover:bg-surface-subtle rounded-xl transition"
              >
                <ExternalLink className="w-4 h-4 text-muted" />
                <span>Lihat Website Publik</span>
              </Link>

              <button
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
          </div>
        </div>

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full flex-1">
        {/* ========================================================= */}
        {/* TAB 1: RINGKASAN (OVERVIEW)                                */}
        {/* ========================================================= */}
        {activeTab === "overview" && (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-bold tracking-tight text-foreground">Ringkasan Website</h2>
            </div>

            {/* KPI Cards Grid (2 kolom di mobile: tingkat 1 kiri Formulir, kanan Berita; tingkat 2 kiri Modul, kanan Pengurus) */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
              <div
                onClick={() => setActiveTab("submissions")}
                className="bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs hover:border-border-strong transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-muted mb-2 sm:mb-3">
                  <span className="text-[11px] sm:text-xs font-semibold tracking-wider uppercase text-muted">
                    Formulir Masuk
                  </span>
                  <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                    <Inbox className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
                  </div>
                </div>
                <div className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
                  {stats.submissionsCount}
                </div>
                <p className="text-[11px] sm:text-xs text-muted mt-1 sm:mt-1.5 leading-snug">
                  Respon formulir
                </p>
              </div>

              <div
                onClick={() => setActiveTab("news")}
                className="bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs hover:border-border-strong transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-muted mb-2 sm:mb-3">
                  <span className="text-[11px] sm:text-xs font-semibold tracking-wider uppercase text-muted">
                    Berita & Agenda
                  </span>
                  <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                    <Newspaper className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
                  </div>
                </div>
                <div className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
                  {stats.newsCount}
                </div>
                <p className="text-[11px] sm:text-xs text-muted mt-1 sm:mt-1.5 leading-snug">
                  Artikel aktif
                </p>
              </div>

              <div
                onClick={() => setActiveTab("collections")}
                className="bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs hover:border-border-strong transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-muted mb-2 sm:mb-3">
                  <span className="text-[11px] sm:text-xs font-semibold tracking-wider uppercase text-muted">
                    Modul Kustom
                  </span>
                  <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                    <Boxes className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
                  </div>
                </div>
                <div className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
                  {stats.customItemsCount}
                </div>
                <p className="text-[11px] sm:text-xs text-muted mt-1 sm:mt-1.5 leading-snug">
                  Item data modul
                </p>
              </div>

              <div
                onClick={() => setActiveTab("members")}
                className="bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs hover:border-border-strong transition cursor-pointer"
              >
                <div className="flex items-center justify-between text-muted mb-2 sm:mb-3">
                  <span className="text-[11px] sm:text-xs font-semibold tracking-wider uppercase text-muted">
                    Pengurus Aktif
                  </span>
                  <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
                    <Users className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
                  </div>
                </div>
                <div className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
                  {members.length}
                </div>
                <p className="text-[11px] sm:text-xs text-muted mt-1 sm:mt-1.5 leading-snug">
                  Akun pengelola
                </p>
              </div>
            </div>

            {/* Information & Integration Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-7 h-7 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                    <Globe className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-bold text-foreground">Integrasi Template Otomatis</h3>
                </div>
                <p className="text-xs text-muted leading-relaxed mt-2">
                  Data yang Anda tambahkan di tab <strong>Modul Kustom</strong> dan <strong>Berita & Agenda</strong> akan otomatis disinkronkan ke dalam halaman website publik melalui penanda modul <code className="font-mono text-primary bg-surface-subtle border border-border px-1.5 py-0.5 rounded">data-ws-*</code>.
                </p>
                <div className="mt-4 pt-3 border-t border-border flex items-center justify-between text-xs">
                  <span className="text-muted">Status Penyajian:</span>
                  <span className="font-medium text-foreground">Dinamis (Sandbox Terisolasi)</span>
                </div>
              </div>

              <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-7 h-7 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                    <ShieldCheck className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-bold text-foreground">Keamanan Akses Terpusat</h3>
                </div>
                <p className="text-xs text-muted leading-relaxed mt-2">
                  Setiap pengurus organisasi dapat diberi hak akses berjenjang (Pemilik, Admin, atau Editor). Kredensial dan sesi dienkripsi dengan standar industri tanpa membebani pengelolaan server mandiri.
                </p>
                <div className="mt-4 pt-3 border-t border-border flex items-center justify-between text-xs">
                  <span className="text-muted">Peran Anda:</span>
                  <span className="font-mono font-semibold text-primary uppercase">{user.role}</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* TAB 2: KOTAK MASUK FORMULIR (SUBMISSIONS)                 */}
        {/* ========================================================= */}
        {activeTab === "submissions" && (
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold tracking-tight text-foreground">Kotak Masuk Formulir</h2>
                <p className="text-sm text-muted mt-0.5">
                  Respon dari pengunjung yang mengisi formulir pendaftaran, kontak, atau feedback pada website.
                </p>
              </div>
              <button
                onClick={() => token && loadSubmissions(token)}
                className="inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-lg border border-border bg-surface hover:bg-surface-subtle text-foreground transition"
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
                Belum ada data formulir masuk dari pengunjung website.
              </div>
            ) : (
              <div className="bg-surface rounded-2xl border border-border overflow-hidden shadow-xs">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-foreground">
                    <thead className="bg-surface-subtle border-b border-border text-muted font-semibold tracking-wider">
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
                            <span className="bg-primary-container text-on-primary-container px-2 py-0.5 rounded font-mono font-semibold text-xs border border-border">
                              {sub.formName}
                            </span>
                          </td>
                          <td className="py-3 px-4">
                            <div className="flex flex-wrap gap-1.5">
                              {Object.entries(sub.data || {}).map(([k, v]) => (
                                <div
                                  key={k}
                                  className="inline-flex items-center gap-1 bg-surface-subtle border border-border px-2 py-0.5 rounded text-xs"
                                >
                                  <span className="font-mono text-muted text-[11px]">{k}:</span>
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

        {/* ========================================================= */}
        {/* TAB 3: BERITA & AGENDA (NEWS)                             */}
        {/* ========================================================= */}
        {activeTab === "news" && (
          <div className="space-y-5">
            <div>
              <h2 className="text-xl font-bold tracking-tight text-foreground">Berita & Agenda Kegiatan</h2>
              <p className="text-sm text-muted mt-0.5">
                Kelola artikel, pengumuman, dan agenda yang tampil pada modul berita website.
              </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Form Terbitkan Berita */}
              <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs h-fit">
                <h3 className="text-sm font-bold text-foreground mb-4 flex items-center gap-2">
                  <Plus className="w-4 h-4 text-primary" />
                  <span>Terbitkan Berita / Agenda</span>
                </h3>

                <form onSubmit={handleCreateNews} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-foreground mb-1.5">
                      Judul Berita <span className="text-error">*</span>
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="Contoh: Rapat Kerja Anggota 2026"
                      value={newTitle}
                      onChange={(e) => setNewTitle(e.target.value)}
                      className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-foreground mb-1.5">
                      Ringkasan Singkat
                    </label>
                    <input
                      type="text"
                      placeholder="Deskripsi singkat yang tampil di beranda..."
                      value={newSummary}
                      onChange={(e) => setNewSummary(e.target.value)}
                      className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-foreground mb-1.5">
                      Konten Lengkap
                    </label>
                    <textarea
                      rows={4}
                      placeholder="Tuliskan berita lengkap di sini..."
                      value={newContent}
                      onChange={(e) => setNewContent(e.target.value)}
                      className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                    ></textarea>
                  </div>

                  <button
                    type="submit"
                    disabled={submittingNews}
                    className="w-full py-2.5 bg-primary hover:opacity-90 text-on-primary rounded-xl text-xs font-semibold shadow-xs transition disabled:opacity-50"
                  >
                    {submittingNews ? "Menerbitkan..." : "Terbitkan Berita"}
                  </button>
                </form>
              </div>

              {/* List Berita */}
              <div className="lg:col-span-2 space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-bold text-foreground">Daftar Berita Aktif</h3>
                  <span className="text-xs text-muted font-mono">
                    {newsList.length} Berita
                  </span>
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
                  <div className="space-y-3">
                    {newsList.map((item) => (
                      <div
                        key={item.id}
                        className="bg-surface p-5 rounded-2xl border border-border shadow-xs flex items-start justify-between gap-4 hover:border-border-strong transition"
                      >
                        <div className="space-y-1.5 flex-1 min-w-0">
                          <h4 className="text-sm font-bold text-foreground leading-snug">{item.title}</h4>
                          {item.summary && (
                            <p className="text-xs text-muted leading-relaxed">{item.summary}</p>
                          )}
                          {item.publishedAt && (
                            <div className="text-[11px] text-muted/80 font-mono pt-1">
                              Diterbitkan: {item.publishedAt}
                            </div>
                          )}
                        </div>
                        <button
                          onClick={() => handleDeleteNews(item.id)}
                          className="p-1.5 text-muted hover:text-error hover:bg-error/10 rounded-lg transition shrink-0"
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
          </div>
        )}

        {/* ========================================================= */}
        {/* TAB 4: MODUL KUSTOM (COLLECTIONS)                         */}
        {/* ========================================================= */}
        {activeTab === "collections" && (
          <div className="space-y-5">
            <div>
              <h2 className="text-xl font-bold tracking-tight text-foreground">Modul Kustom Dinamis</h2>
              <p className="text-sm text-muted mt-0.5">
                Kelola data dinamis seperti prestasi, struktur pengurus, portfolio, atau galeri yang terhubung ke template.
              </p>
            </div>

            {/* Collection Tabs Selector */}
            <div className="flex items-center gap-2 overflow-x-auto pb-1">
              {Array.from(new Set([...Object.keys(templateModules), ...Object.keys(collections)])).map((colName) => {
                const count = (collections[colName] || []).length;
                const isSelected = selectedCol === colName;
                return (
                  <button
                    key={colName}
                    onClick={() => chooseCollection(colName)}
                    className={`px-3 py-1.5 text-xs rounded-xl font-mono transition whitespace-nowrap ${
                      isSelected
                        ? "bg-primary text-on-primary font-semibold shadow-xs"
                        : "bg-surface text-foreground border border-border hover:bg-surface-subtle font-medium"
                    }`}
                  >
                    {colName} ({count})
                  </button>
                );
              })}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Form Tambah / Edit Item Modul */}
              <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs h-fit">
                <h3 className="text-sm font-bold text-foreground mb-4 flex items-center gap-2">
                  {editingId ? <Pencil className="w-4 h-4 text-primary" /> : <Plus className="w-4 h-4 text-primary" />}
                  <span>{editingId ? "Edit Item Modul" : "Tambah Item Modul"}</span>
                </h3>

                <form onSubmit={handleCreateCollectionItem} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-foreground mb-1.5">
                      Nama Modul Koleksi
                    </label>
                    <div className="flex gap-2">
                      <select
                        value={selectedCol}
                        disabled={!!editingId}
                        onChange={(e) => chooseCollection(e.target.value)}
                        className="w-1/2 px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary disabled:bg-surface-subtle transition"
                      >
                        {Array.from(new Set([...Object.keys(templateModules), ...Object.keys(collections)])).map((k) => (
                          <option key={k} value={k}>
                            {k}
                          </option>
                        ))}
                      </select>
                      <input
                        type="text"
                        placeholder="Atau nama baru..."
                        disabled={!!editingId}
                        value={newColName}
                        onChange={(e) => setNewColName(e.target.value)}
                        className="w-1/2 px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                      />
                    </div>
                    <span className="text-[11px] text-muted mt-1 block">
                      Gunakan huruf kecil tanpa spasi (misal: <code className="font-mono text-foreground bg-surface-subtle px-1 py-0.5 rounded border border-border">prestasi</code>, <code className="font-mono text-foreground bg-surface-subtle px-1 py-0.5 rounded border border-border">alumni</code>)
                    </span>
                  </div>

                  <div className="pt-3 border-t border-border">
                    <div className="flex items-center justify-between mb-2.5">
                      <span className="text-xs font-semibold text-foreground">Atribut Field:</span>
                      <button
                        type="button"
                        onClick={addDynamicField}
                        className="text-xs text-primary hover:text-primary/80 font-semibold"
                      >
                        + Tambah Field
                      </button>
                    </div>

                    <div className="space-y-3.5 max-h-[28rem] overflow-y-auto pr-1">
                      {dynamicFields.map((field, idx) => {
                        const inputClass = "w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition";
                        return (
                          <div key={idx} className="space-y-1.5 bg-surface-subtle/70 p-3 rounded-xl border border-border">
                            <div className="flex items-center gap-2">
                              {field.fixed ? (
                                <span className="flex-1 font-mono text-xs font-semibold text-foreground">{field.key}</span>
                              ) : (
                                <input
                                  type="text"
                                  placeholder="Nama field (mis: nama)"
                                  aria-label="Nama field"
                                  value={field.key}
                                  onChange={(e) => updateField(idx, { key: e.target.value })}
                                  className="flex-1 px-2.5 py-1.5 text-xs rounded-lg border border-border bg-surface text-foreground font-mono placeholder:text-muted/60 focus:outline-none focus:ring-1 focus:ring-primary"
                                />
                              )}
                              {field.fixed ? (
                                <span className="text-[11px] text-muted bg-surface px-2 py-0.5 rounded border border-border">
                                  {FIELD_TYPE_LABELS[field.type]}
                                </span>
                              ) : (
                                <select
                                  aria-label="Jenis field"
                                  value={field.type}
                                  onChange={(e) => updateField(idx, { type: e.target.value as FieldType })}
                                  className="px-2 py-1 text-xs rounded-lg border border-border bg-surface text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
                                >
                                  {Object.entries(FIELD_TYPE_LABELS).map(([type, label]) => (
                                    <option key={type} value={type}>
                                      {label}
                                    </option>
                                  ))}
                                </select>
                              )}
                              {!field.fixed && dynamicFields.length > 1 && (
                                <button
                                  type="button"
                                  onClick={() => removeDynamicField(idx)}
                                  aria-label={`Hapus field ${field.key}`}
                                  className="text-muted hover:text-error p-1"
                                >
                                  &times;
                                </button>
                              )}
                            </div>

                            {field.type === "paragraf" ? (
                              <textarea
                                rows={3}
                                placeholder="Isi data teks panjang..."
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
                                    alt={`Foto ${field.key}`}
                                    className="w-14 h-14 rounded-xl object-cover border border-border bg-surface"
                                  />
                                ) : (
                                  <div className="w-14 h-14 rounded-xl border border-dashed border-border grid place-items-center text-muted bg-surface">
                                    <ImagePlus className="w-5 h-5" />
                                  </div>
                                )}
                                <div className="flex-1 space-y-1">
                                  <label className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-xl border border-border bg-surface text-foreground cursor-pointer hover:bg-surface-subtle transition">
                                    {uploadingField === idx ? <Loader2 className="w-3.5 h-3.5 animate-spin text-primary" /> : <ImagePlus className="w-3.5 h-3.5" />}
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
                                type={field.type === "angka" ? "number" : field.type === "tanggal" ? "date" : field.type === "link" ? "url" : "text"}
                                placeholder={field.type === "link" ? "https://..." : "Isi nilai data..."}
                                value={field.value}
                                onChange={(e) => updateField(idx, { value: e.target.value })}
                                className={inputClass}
                              />
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={submittingColItem}
                    className="w-full mt-2 py-2.5 bg-primary hover:bg-primary/90 text-on-primary rounded-xl text-xs font-semibold shadow-xs transition disabled:opacity-50"
                  >
                    {submittingColItem ? "Menyimpan..." : editingId ? "Simpan Perubahan" : "Simpan ke Modul"}
                  </button>
                  {editingId && (
                    <button
                      type="button"
                      onClick={cancelEdit}
                      className="w-full py-2 text-xs font-semibold text-muted hover:text-foreground rounded-xl border border-border bg-surface hover:bg-surface-subtle transition"
                    >
                      Batal Edit
                    </button>
                  )}
                </form>
              </div>

              {/* List Data Modul */}
              <div className="lg:col-span-2 space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-bold text-foreground">
                      Data Modul: <span className="font-mono text-primary font-bold">{selectedCol}</span>
                    </h3>
                    <p className="text-xs text-muted">
                      Disajikan ke template via <code className="font-mono text-foreground bg-surface-subtle border border-border px-1 py-0.5 rounded">data-ws-list=&quot;{selectedCol}&quot;</code>
                    </p>
                  </div>
                  <span className="text-xs text-muted font-mono">
                    {(collections[selectedCol] || []).length} Item
                  </span>
                </div>

                {loadingCollections ? (
                  <div className="py-12 text-center text-muted">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                    <p className="text-xs">Memuat data modul...</p>
                  </div>
                ) : !collections[selectedCol] || collections[selectedCol].length === 0 ? (
                  <div className="bg-surface p-8 text-center rounded-2xl border border-border text-muted text-xs shadow-xs">
                    Belum ada item dalam modul &ldquo;{selectedCol}&rdquo;.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {collections[selectedCol].map((item, idx, items) => (
                      <div
                        key={item.id || idx}
                        className={`bg-surface p-4 sm:p-5 rounded-2xl border shadow-xs flex items-start justify-between gap-4 transition hover:border-border-strong ${
                          editingId === item.id ? "border-primary ring-1 ring-primary" : "border-border"
                        }`}
                      >
                        <div className="flex flex-wrap gap-2.5 items-center flex-1 min-w-0">
                          {Object.entries(item).map(([k, v]) => {
                            if (k === "id") return null;
                            if (fieldTypeOf(selectedCol, k) === "gambar") {
                              return (
                                // eslint-disable-next-line @next/next/no-img-element
                                <img
                                  key={k}
                                  src={String(v)}
                                  alt={k}
                                  className="w-12 h-12 rounded-xl object-cover border border-border shrink-0"
                                />
                              );
                            }
                            return (
                              <div
                                key={k}
                                className="inline-flex items-center gap-1.5 bg-surface-subtle border border-border px-2.5 py-1 rounded-lg text-xs max-w-full"
                              >
                                <span className="font-mono text-muted text-[11px]">{k}:</span>
                                <span className="font-medium text-foreground truncate">{String(v)}</span>
                              </div>
                            );
                          })}
                        </div>

                        {item.id && (
                          <div className="flex items-center gap-1 shrink-0">
                            <button
                              onClick={() => handleMoveItem(idx, -1)}
                              disabled={idx === 0}
                              className="p-1.5 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition disabled:opacity-30"
                              title="Naikkan urutan"
                              aria-label="Naikkan urutan"
                            >
                              <ArrowUp className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleMoveItem(idx, 1)}
                              disabled={idx === items.length - 1}
                              className="p-1.5 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition disabled:opacity-30"
                              title="Turunkan urutan"
                              aria-label="Turunkan urutan"
                            >
                              <ArrowDown className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => startEditItem(item)}
                              className="p-1.5 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition"
                              title="Edit item"
                              aria-label="Edit item"
                            >
                              <Pencil className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteCollectionItem(selectedCol, item.id)}
                              className="p-1.5 text-muted hover:text-error hover:bg-error/10 rounded-lg transition"
                              title="Hapus item"
                              aria-label="Hapus item"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* TAB 5: PENGURUS & HAK AKSES (MEMBERS & ACCESS RIGHTS)      */}
        {/* ========================================================= */}
        {activeTab === "members" && (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-bold tracking-tight text-foreground">Pengurus & Hak Akses (RBAC)</h2>
              <p className="text-sm text-muted mt-0.5">
                Kelola pengurus resmi organisasi dan atur izin akses untuk mengelola data website.
              </p>
            </div>

            {/* Role Notice Card */}
            <div className="bg-surface p-5 rounded-2xl border border-border shadow-xs flex items-start gap-4">
              <div className="w-9 h-9 rounded-xl bg-primary-container text-on-primary-container flex items-center justify-center shrink-0 mt-0.5">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div className="space-y-1 text-xs leading-relaxed text-muted flex-1">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-foreground">Hierarki Hak Akses:</span>
                  <span className="font-mono text-[10px] bg-surface-subtle text-muted px-2 py-0.5 rounded-full border border-border">
                    Server Security Active
                  </span>
                </div>
                <p>
                  <strong className="text-foreground">Pemilik (Owner)</strong> memiliki kendali penuh atas situs. <strong className="text-foreground">Admin</strong> dapat mengelola konten dan menunjuk pengurus baru. <strong className="text-foreground">Editor</strong> berfokus pada penerbitan berita, pengisian modul kustom, dan melihat pesan formulir.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Form Tambah Pengurus */}
              <div className="lg:col-span-1">
                {user.role === "owner" || user.role === "admin" ? (
                  <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs sticky top-24">
                    <div className="flex items-center gap-2 mb-4">
                      <div className="w-8 h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                        <UserPlus className="w-4 h-4" />
                      </div>
                      <div>
                        <h3 className="text-sm font-bold text-foreground">Tambah Pengurus</h3>
                        <p className="text-xs text-muted">Beri hak akses ke akun pengurus baru</p>
                      </div>
                    </div>

                    <form onSubmit={handleAddMember} className="space-y-3.5">
                      <div>
                        <label className="block text-xs font-semibold text-foreground mb-1">
                          Email Pengurus <span className="text-error">*</span>
                        </label>
                        <input
                          type="email"
                          required
                          placeholder="pengurus@organisasi.id"
                          value={newMemberEmail}
                          onChange={(e) => setNewMemberEmail(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-foreground mb-1">
                          Nama Lengkap
                        </label>
                        <input
                          type="text"
                          placeholder="Contoh: Ahmad Rizki"
                          value={newMemberName}
                          onChange={(e) => setNewMemberName(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-foreground mb-1">
                          Jabatan / Posisi
                        </label>
                        <input
                          type="text"
                          placeholder="Contoh: Staff Humas / Divisi Acara"
                          value={newMemberPosition}
                          onChange={(e) => setNewMemberPosition(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-foreground mb-1">
                          Tingkat Hak Akses (Role) <span className="text-error">*</span>
                        </label>
                        <select
                          value={newMemberRole}
                          onChange={(e) => setNewMemberRole(e.target.value as any)}
                          className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                        >
                          <option value="editor">Editor (Kelola Berita, Modul & Formulir)</option>
                          <option value="admin">Admin (Akses Penuh + Kelola Hak Akses)</option>
                        </select>
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-foreground mb-1">
                          Password Masuk Sementara <span className="text-error">*</span>
                        </label>
                        <input
                          type="text"
                          required
                          value={newMemberPassword}
                          onChange={(e) => setNewMemberPassword(e.target.value)}
                          className="w-full px-3 py-2 text-xs rounded-xl border border-border bg-surface text-foreground font-mono focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
                        />
                        <p className="text-[11px] text-muted mt-1">
                          Password awal untuk pengurus masuk ke portal ini.
                        </p>
                      </div>

                      <button
                        type="submit"
                        disabled={submittingMember}
                        className="w-full mt-2 py-2.5 bg-primary hover:bg-primary/90 text-on-primary rounded-xl text-xs font-semibold shadow-xs transition disabled:opacity-50 flex items-center justify-center gap-1.5"
                      >
                        {submittingMember ? (
                          <>
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                            <span>Menyimpan...</span>
                          </>
                        ) : (
                          <>
                            <UserPlus className="w-3.5 h-3.5" />
                            <span>Beri Hak Akses</span>
                          </>
                        )}
                      </button>
                    </form>
                  </div>
                ) : (
                  <div className="bg-surface p-6 rounded-2xl border border-border shadow-xs text-muted space-y-3">
                    <div className="flex items-center gap-2 text-foreground font-bold text-sm">
                      <Shield className="w-4 h-4 text-primary" />
                      <span>Hak Akses Terbatas</span>
                    </div>
                    <p className="text-xs text-muted leading-relaxed">
                      Anda saat ini masuk sebagai <strong className="text-foreground">Editor Konten</strong>. Anda memiliki wewenang untuk menambah berita, item modul kustom, dan melihat pesan formulir.
                    </p>
                    <p className="text-xs text-muted leading-relaxed">
                      Penambahan pengurus baru serta pengubahan hak akses hanya dapat dilakukan oleh akun dengan peran <strong className="text-foreground">Admin</strong> atau <strong className="text-foreground">Pemilik</strong>.
                    </p>
                  </div>
                )}
              </div>

              {/* Daftar Pengurus Aktif */}
              <div className="lg:col-span-2 space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-bold text-foreground">
                      Daftar Pengurus Aktif ({members.length})
                    </h3>
                    <p className="text-xs text-muted">
                      Pengguna terdaftar dengan kredensial resmi pada website ini
                    </p>
                  </div>

                  <button
                    onClick={() => token && loadMembers(token)}
                    className="p-1.5 text-muted hover:text-foreground hover:bg-surface rounded-lg border border-transparent hover:border-border transition"
                    title="Perbarui data"
                  >
                    <RefreshCw className={`w-3.5 h-3.5 ${loadingMembers ? "animate-spin text-primary" : ""}`} />
                  </button>
                </div>

                {loadingMembers ? (
                  <div className="py-12 text-center text-muted">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                    <p className="text-xs">Memuat daftar pengurus...</p>
                  </div>
                ) : members.length === 0 ? (
                  <div className="bg-surface p-8 text-center rounded-2xl border border-border text-muted text-xs shadow-xs">
                    Belum ada data pengurus yang dimuat.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {members.map((m) => {
                      const isOwner = m.role === "owner";
                      const isAdmin = m.role === "admin";
                      const isCurrentUser = m.userId === user.id;
                      const canManage = (user.role === "owner" || user.role === "admin") && !isOwner && !isCurrentUser;

                      return (
                        <div
                          key={m.userId}
                          className="bg-surface p-4 sm:p-5 rounded-2xl border border-border shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition hover:border-border-strong"
                        >
                          <div className="flex items-start sm:items-center gap-3.5">
                            <div className="w-10 h-10 rounded-full bg-primary-container border border-border flex items-center justify-center font-bold text-xs text-on-primary-container shrink-0">
                              {(m.name || m.email).substring(0, 2).toUpperCase()}
                            </div>
                            <div>
                              <div className="flex items-center gap-2 flex-wrap">
                                <span className="text-xs font-bold text-foreground">
                                  {m.name || "Tanpa Nama"}
                                </span>
                                {isCurrentUser && (
                                  <span className="text-[10px] bg-surface-subtle text-muted px-1.5 py-0.2 rounded font-mono border border-border">
                                    (Anda)
                                  </span>
                                )}
                                <span className="text-[10px] px-2 py-0.5 rounded-full font-mono uppercase font-semibold border bg-surface-subtle text-foreground border-border">
                                  {isOwner ? "👑 Pemilik" : isAdmin ? "🛡️ Admin" : "✍️ Editor"}
                                </span>
                              </div>
                              <div className="text-xs text-muted font-mono mt-0.5">
                                {m.email}
                              </div>
                              {m.positionTitle && (
                                <div className="text-xs text-muted mt-0.5">
                                  Posisi: <span className="text-foreground font-medium">{m.positionTitle}</span>
                                </div>
                              )}
                            </div>
                          </div>

                          <div className="flex items-center gap-2 self-end sm:self-center border-t sm:border-t-0 pt-2 sm:pt-0 w-full sm:w-auto justify-end">
                            {canManage ? (
                              <>
                                <select
                                  value={m.role}
                                  onChange={(e) => handleUpdateRole(m.userId, e.target.value as any)}
                                  className="text-xs px-2.5 py-1.5 rounded-lg border border-border bg-surface-subtle text-foreground focus:outline-none focus:ring-1 focus:ring-primary transition"
                                  title="Ganti peran pengurus"
                                >
                                  <option value="editor">Role: Editor</option>
                                  <option value="admin">Role: Admin</option>
                                </select>

                                <button
                                  onClick={() => handleRemoveMember(m.userId, m.name || m.email)}
                                  className="p-1.5 text-muted hover:text-error hover:bg-error/10 rounded-lg transition"
                                  title="Cabut Hak Akses"
                                >
                                  <Trash2 className="w-4 h-4" />
                                </button>
                              </>
                            ) : isOwner ? (
                              <span className="text-[11px] text-muted font-mono px-2 py-1">
                                Pemilik Utama
                              </span>
                            ) : null}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
