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
  Clock,
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
  expiresAt?: string | null;
  expired?: boolean;
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
  "shield-check": ShieldCheck,
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
  // Demo memakai key tersendiri agar tidak menimpa sesi pengurus asli, tetapi tetap bertahan saat halaman di-refresh.
  const tokenKey = demo ? `orgspace_demo_token_${slug}` : `orgspace_site_token_${slug}`;

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
  const [mobileProfileOpen, setMobileProfileOpen] = useState(false);
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
  const [newImageUrl, setNewImageUrl] = useState("");
  const [uploadingNewsPhoto, setUploadingNewsPhoto] = useState(false);
  const [newExpiresAt, setNewExpiresAt] = useState("");
  const [newsFilter, setNewsFilter] = useState<"active" | "expired" | "all">("active");
  const [editingNewsExpiryId, setEditingNewsExpiryId] = useState<string | null>(null);
  const [editExpiryDate, setEditExpiryDate] = useState("");
  const [submittingNews, setSubmittingNews] = useState(false);

  // Edit News Modal state
  const [editingNewsItem, setEditingNewsItem] = useState<NewsItem | null>(null);
  const [editNewsTitle, setEditNewsTitle] = useState("");
  const [editNewsSummary, setEditNewsSummary] = useState("");
  const [editNewsContent, setEditNewsContent] = useState("");
  const [editNewsImageUrl, setEditNewsImageUrl] = useState("");
  const [editNewsExpiresAt, setEditNewsExpiresAt] = useState("");
  const [savingNewsEdit, setSavingNewsEdit] = useState(false);
  const [uploadingEditNewsPhoto, setUploadingEditNewsPhoto] = useState(false);

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

  // Access Management state
  interface AccessCred {
    role: "owner" | "admin" | "editor";
    username: string;
    updatedAt?: string;
    canEdit?: boolean;
  }
  const [accessList, setAccessList] = useState<AccessCred[]>([]);
  const [loadingAccess, setLoadingAccess] = useState(false);
  const [editingCredRole, setEditingCredRole] = useState<"owner" | "admin" | "editor" | null>(null);
  const [newCredUsername, setNewCredUsername] = useState("");
  const [newCredPassword, setNewCredPassword] = useState("");
  const [showCredPassword, setShowCredPassword] = useState(false);
  const [savingCred, setSavingCred] = useState(false);

  // Section Manager state (Option 1)
  interface SectionConfigItem {
    id: string;
    name: string;
    enabled: boolean;
    startAt?: string;
    endAt?: string;
  }
  const DEFAULT_SECTIONS: SectionConfigItem[] = [
    { id: "about", name: "Tentang Kami (Filosofi & Profil)", enabled: true },
    { id: "anggota", name: "Personel Musisi & Talenta", enabled: true },
    { id: "program", name: "Program Kerja Studio", enabled: true },
    { id: "galeri", name: "Dokumentasi Aksi Panggung", enabled: true },
    { id: "berita", name: "Kabar Rilis & Warta", enabled: true },
    { id: "agenda", name: "Jadwal Manggung & Tur", enabled: true },
    { id: "pendaftaran", name: "Booking Penampilan & Audisi", enabled: true },
  ];
  const [sectionList, setSectionList] = useState<SectionConfigItem[]>(DEFAULT_SECTIONS);
  const [loadingSections, setLoadingSections] = useState(false);
  const [savingSections, setSavingSections] = useState(false);

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
    loadAccess(jwt);
    loadSections(jwt);
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
        const raw: Record<string, any[]> = await res.json();
        const filtered: Record<string, any[]> = {};
        for (const [k, v] of Object.entries(raw || {})) {
          if (!k.startsWith("_")) {
            filtered[k] = v;
          }
        }
        setCollections(filtered);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingCollections(false);
    }
  };

  const loadAccess = async (jwt: string) => {
    setLoadingAccess(true);
    try {
      const res = await api(`${slug}/admin/access`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setAccessList(Array.isArray(data) ? data : []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingAccess(false);
    }
  };

  const handleSaveCred = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !editingCredRole || !newCredUsername.trim()) return;
    setSavingCred(true);
    try {
      const res = await api(`${slug}/admin/access/${editingCredRole}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: newCredUsername.trim(),
          password: newCredPassword.trim() || undefined,
        }),
      });
      if (res.ok) {
        showToast(`Kredensial ${editingCredRole.toUpperCase()} berhasil diperbarui!`, "success");
        setEditingCredRole(null);
        setNewCredUsername("");
        setNewCredPassword("");
        loadAccess(token);
      } else {
        const err = await res.json();
        showToast(err.message || "Gagal memperbarui kredensial.", "error");
      }
    } catch {
      showToast("Terjadi kesalahan saat menyimpan kredensial.", "error");
    } finally {
      setSavingCred(false);
    }
  };

  const loadSections = async (jwt: string) => {
    setLoadingSections(true);
    try {
      const res = await api(`${slug}/admin/sections`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        const list = Array.isArray(data) ? data : Array.isArray(data?.sections) ? data.sections : [];
        if (list.length > 0) {
          setSectionList(list);
        } else {
          setSectionList(DEFAULT_SECTIONS);
        }
      } else {
        setSectionList(DEFAULT_SECTIONS);
      }
    } catch (e) {
      console.error(e);
      setSectionList(DEFAULT_SECTIONS);
    } finally {
      setLoadingSections(false);
    }
  };

  const moveSection = (index: number, direction: "up" | "down") => {
    const target = direction === "up" ? index - 1 : index + 1;
    if (target < 0 || target >= sectionList.length) return;
    const updated = [...sectionList];
    const [moved] = updated.splice(index, 1);
    updated.splice(target, 0, moved);
    setSectionList(updated);
  };

  const toggleSection = (id: string) => {
    setSectionList((prev) =>
      prev.map((s) => (s.id === id ? { ...s, enabled: !s.enabled } : s))
    );
  };

  const handleSaveSections = async () => {
    if (!token) return;
    setSavingSections(true);
    try {
      const res = await api(`${slug}/admin/sections`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ sections: sectionList }),
      });
      if (res.ok) {
        showToast("Urutan dan visibilitas section berhasil disimpan!", "success");
      } else {
        showToast("Gagal menyimpan pengaturan section.", "error");
      }
    } catch {
      showToast("Terjadi kesalahan koneksi.", "error");
    } finally {
      setSavingSections(false);
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

  const handleUploadNewsPhoto = async (file: File | undefined, isEdit = false) => {
    if (!token || !file) return;
    if (isEdit) setUploadingEditNewsPhoto(true);
    else setUploadingNewsPhoto(true);

    try {
      const body = new FormData();
      body.append("file", await shrinkPhoto(file), "berita.jpg");
      const res = await api(`${slug}/admin/media`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body,
      });
      const data = await res.json();
      if (res.ok && data.url) {
        if (isEdit) {
          setEditNewsImageUrl(data.url);
        } else {
          setNewImageUrl(data.url);
        }
        showToast("Foto berita berhasil diunggah!");
      } else {
        showToast(data.message || "Gagal mengunggah foto", "error");
      }
    } catch {
      showToast("Gagal mengunggah foto", "error");
    } finally {
      if (isEdit) setUploadingEditNewsPhoto(false);
      else setUploadingNewsPhoto(false);
    }
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
          imageUrl: newImageUrl.trim() || null,
          expiresAt: newExpiresAt || null,
        }),
      });

      if (res.ok) {
        setNewTitle("");
        setNewSummary("");
        setNewContent("");
        setNewImageUrl("");
        setNewExpiresAt("");
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

  const handleSaveNewsEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !editingNewsItem || !editNewsTitle.trim()) return;
    setSavingNewsEdit(true);

    try {
      const res = await api(`${slug}/admin/news/${editingNewsItem.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: editNewsTitle.trim(),
          summary: editNewsSummary.trim(),
          content: editNewsContent.trim(),
          imageUrl: editNewsImageUrl.trim() || null,
          expiresAt: editNewsExpiresAt || null,
        }),
      });

      if (res.ok) {
        showToast("Berita berhasil diperbarui!");
        setEditingNewsItem(null);
        loadNews(token);
      } else {
        const err = await res.json();
        showToast(err.message || "Gagal memperbarui berita", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
    } finally {
      setSavingNewsEdit(false);
    }
  };

  const handleUpdateNewsExpiry = async (newsId: string, expiresAt: string | null) => {
    if (!token) return;
    try {
      const res = await api(`${slug}/admin/news/${newsId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ expiresAt }),
      });

      if (res.ok) {
        showToast("Waktu auto hilang berhasil diperbarui!");
        setEditingNewsExpiryId(null);
        setEditExpiryDate("");
        loadNews(token);
      } else {
        const err = await res.json();
        showToast(err.message || "Gagal memperbarui batas waktu", "error");
      }
    } catch {
      showToast("Terjadi kesalahan jaringan", "error");
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

  const handleUploadAudio = async (index: number, file: File | undefined) => {
    if (!token || !file) return;
    if (file.size > 15 * 1024 * 1024) {
      showToast("Ukuran file audio maksimal 15 MB", "error");
      return;
    }
    setUploadingField(index);
    try {
      const body = new FormData();
      body.append("file", file, file.name || "audio.mp3");
      const res = await api(`${slug}/admin/media`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body,
      });
      const data = await res.json();
      if (res.ok) {
        updateField(index, { value: data.url });
        showToast("Audio berhasil diunggah", "success");
      } else {
        showToast(data.message || "Gagal mengunggah audio", "error");
      }
    } catch {
      showToast("Audio gagal diunggah. Pastikan file audio valid lalu coba lagi.", "error");
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
    if (id === "access" && token) loadAccess(token);
    if (id === "sections" && token) loadSections(token);
    setMobileSidebarOpen(false);
    setMobileProfileOpen(false);
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
  const canManageAccess = user.role === "owner" || user.role === "admin";
  const navGroups = buildNav(portal, Object.keys(templateModules), Object.keys(collections), canViewSubmissions, canManageAccess);
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
    setMobileProfileOpen(false);
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
        <label htmlFor={field.type === "gambar" || field.type === "audio" ? undefined : id} className="block text-xs font-semibold text-foreground">
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
        ) : field.type === "audio" ? (
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <label className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium rounded-xl border border-border bg-surface text-foreground cursor-pointer hover:bg-surface-subtle transition">
                {uploadingField === idx ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin text-primary" />
                ) : (
                  <Music className="w-3.5 h-3.5 text-primary" />
                )}
                <span>{uploadingField === idx ? "Mengunggah..." : field.value ? "Ganti file MP3" : "Pilih file MP3 / Audio"}</span>
                <input
                  type="file"
                  accept="audio/*,.mp3,.wav,.ogg,.m4a"
                  className="sr-only"
                  disabled={uploadingField !== null}
                  onChange={(e) => {
                    handleUploadAudio(idx, e.target.files?.[0]);
                    e.target.value = "";
                  }}
                />
              </label>
              {field.value && (
                <button
                  type="button"
                  onClick={() => updateField(idx, { value: "" })}
                  className="text-[11px] text-muted hover:text-error transition"
                >
                  Hapus audio
                </button>
              )}
            </div>
            {field.value && (
              <div className="flex items-center gap-2 p-2 rounded-xl bg-surface-subtle border border-border">
                <audio controls src={field.value} className="h-8 w-full max-w-md" preload="metadata" />
              </div>
            )}
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
                <button
                  type="button"
                  onClick={() => {
                    setMobileProfileOpen(false);
                    setMobileSidebarOpen(true);
                  }}
                  className="p-2 -ml-2 rounded-xl text-foreground hover:bg-surface-subtle transition"
                  aria-label="Buka menu navigasi"
                >
                  <Menu className="w-5 h-5" />
                </button>
                <h1 className="text-sm font-bold text-foreground truncate">{current?.label || "Portal Pengurus"}</h1>
              </div>
              <div className="flex items-center gap-1 relative">
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
                  onClick={() => setMobileProfileOpen((prev) => !prev)}
                  className="w-9 h-9 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-semibold text-on-primary-container transition active:scale-95"
                  aria-label="Buka menu profil pengurus"
                  aria-expanded={mobileProfileOpen}
                >
                  {initials}
                </button>

                {/* Dropdown Menu Khusus Profil Pengurus (Tidak memunculkan sidebar) */}
                {mobileProfileOpen && (
                  <>
                    <div
                      className="fixed inset-0 z-40"
                      onClick={() => setMobileProfileOpen(false)}
                      aria-hidden="true"
                    />
                    <div className="absolute right-0 top-12 mt-1 w-60 rounded-2xl bg-surface border border-border shadow-xl z-50 p-2 divide-y divide-border/60">
                      <div className="flex items-center gap-2.5 px-3 py-2.5">
                        <div className="w-8 h-8 rounded-full bg-primary-container border border-border flex items-center justify-center text-xs font-semibold text-on-primary-container shrink-0">
                          {initials}
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="text-xs font-semibold text-foreground truncate">{user.username}</div>
                          <div className="text-[10px] uppercase font-mono font-semibold tracking-wider text-muted">{user.role}</div>
                        </div>
                      </div>
                      <div className="pt-1.5 space-y-1">
                        <Link
                          href={siteHref}
                          target="_blank"
                          onClick={() => setMobileProfileOpen(false)}
                          className="flex items-center gap-2 px-3 py-2 text-xs font-medium text-muted hover:text-foreground hover:bg-surface-subtle rounded-xl transition"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                          <span>Lihat Website</span>
                        </Link>
                        <button
                          type="button"
                          onClick={() => {
                            setMobileProfileOpen(false);
                            handleLogout();
                          }}
                          className="w-full flex items-center gap-2 px-3 py-2 text-xs font-semibold text-error hover:bg-error/10 rounded-xl transition"
                        >
                          <LogOut className="w-3.5 h-3.5" />
                          <span>Keluar dari Portal</span>
                        </button>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </div>

            {/* Menu Navigasi Scroll Samping di Mobile */}
            <div className="flex items-center gap-1.5 overflow-x-auto px-4 py-2 border-t border-border/60 bg-surface-subtle/30 scrollbar-none [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden">
              {navLinks.map((link) => {
                const Icon = iconOf(link.icon);
                const active = current?.id === link.id;
                const badge = badgeOf(link.id);
                return (
                  <button
                    key={link.id}
                    type="button"
                    onClick={() => openNav(link.id)}
                    className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap shrink-0 transition ${
                      active
                        ? "bg-primary text-on-primary shadow-xs"
                        : "bg-surface border border-border text-muted hover:text-foreground hover:bg-surface-subtle"
                    }`}
                  >
                    <Icon className="w-3.5 h-3.5" />
                    <span>{link.label}</span>
                    {typeof badge === "number" && badge > 0 && (
                      <span
                        className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
                          active ? "bg-white/20 text-white" : "bg-primary-container text-on-primary-container"
                        }`}
                      >
                        {badge}
                      </span>
                    )}
                  </button>
                );
              })}
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

                      {/* Foto Sampul Berita (Opsional) */}
                      <div className="space-y-1.5">
                        <div className="flex items-center justify-between">
                          <label className="block text-xs font-semibold text-foreground flex items-center gap-1.5">
                            <ImageIcon className="w-3.5 h-3.5 text-primary" />
                            <span>Foto Sampul (Opsional)</span>
                          </label>
                          {newImageUrl && (
                            <button
                              type="button"
                              onClick={() => setNewImageUrl("")}
                              className="text-[10px] text-error hover:underline flex items-center gap-0.5"
                            >
                              <X className="w-3 h-3" /> Hapus Foto
                            </button>
                          )}
                        </div>

                        {newImageUrl ? (
                          <div className="relative rounded-xl overflow-hidden border border-border group bg-surface-subtle">
                            <img
                              src={newImageUrl}
                              alt="Pratinjau foto berita"
                              className="w-full h-36 object-cover"
                            />
                            <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-2">
                              <label className="cursor-pointer px-2.5 py-1.5 rounded-lg bg-surface/90 text-foreground text-xs font-semibold hover:bg-surface transition flex items-center gap-1">
                                <ImagePlus className="w-3.5 h-3.5" /> Ganti Foto
                                <input
                                  type="file"
                                  accept="image/*"
                                  className="hidden"
                                  onChange={(e) => handleUploadNewsPhoto(e.target.files?.[0])}
                                  disabled={uploadingNewsPhoto}
                                />
                              </label>
                              <button
                                type="button"
                                onClick={() => setNewImageUrl("")}
                                className="p-1.5 rounded-lg bg-error text-white hover:bg-error/90 transition"
                                title="Hapus foto"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </div>
                        ) : (
                          <label className="flex flex-col items-center justify-center gap-2 p-3.5 border-2 border-dashed border-border hover:border-primary/50 hover:bg-surface-subtle/50 rounded-xl cursor-pointer transition text-center group">
                            {uploadingNewsPhoto ? (
                              <Loader2 className="w-5 h-5 animate-spin text-primary" />
                            ) : (
                              <ImagePlus className="w-5 h-5 text-muted group-hover:text-primary transition" />
                            )}
                            <div className="text-xs">
                              <span className="font-semibold text-foreground group-hover:text-primary transition">
                                {uploadingNewsPhoto ? "Mengunggah foto..." : "Pilih atau Unggah Foto"}
                              </span>
                              <p className="text-[10px] text-muted mt-0.5">JPG, PNG, WebP • Opsional (bisa tanpa foto)</p>
                            </div>
                            <input
                              type="file"
                              accept="image/*"
                              className="hidden"
                              onChange={(e) => handleUploadNewsPhoto(e.target.files?.[0])}
                              disabled={uploadingNewsPhoto}
                            />
                          </label>
                        )}
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
                          rows={4}
                          value={newContent}
                          onChange={(e) => setNewContent(e.target.value)}
                          className={inputClass}
                        />
                      </div>

                      <div className="space-y-1.5 pt-1 border-t border-border/60">
                        <div className="flex items-center justify-between">
                          <label htmlFor="news-expires" className="block text-xs font-semibold text-foreground flex items-center gap-1.5">
                            <Clock className="w-3.5 h-3.5 text-primary" />
                            <span>Auto Hilang (Kedaluwarsa)</span>
                          </label>
                          {newExpiresAt && (
                            <button
                              type="button"
                              onClick={() => setNewExpiresAt("")}
                              className="text-[10px] text-muted hover:text-error transition"
                            >
                              Hapus batas
                            </button>
                          )}
                        </div>
                        <input
                          id="news-expires"
                          type="datetime-local"
                          value={newExpiresAt}
                          onChange={(e) => setNewExpiresAt(e.target.value)}
                          className={inputClass}
                        />
                      </div>

                      <button
                        type="submit"
                        disabled={submittingNews}
                        className="w-full py-2.5 bg-primary hover:opacity-90 text-on-primary rounded-xl text-sm font-semibold shadow-xs transition disabled:opacity-50"
                      >
                        {submittingNews ? "Menerbitkan..." : "Terbitkan Berita"}
                      </button>
                    </form>
                  </div>

                  <div className="xl:col-span-2 space-y-3">
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div className="flex items-center gap-1 p-1 bg-surface-subtle rounded-xl border border-border">
                        <button
                          type="button"
                          onClick={() => setNewsFilter("active")}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                            newsFilter === "active" ? "bg-primary text-on-primary shadow-xs" : "text-muted hover:text-foreground"
                          }`}
                        >
                          Berita Aktif ({newsList.filter((n) => !n.expired && (!n.expiresAt || new Date(n.expiresAt) > new Date())).length})
                        </button>
                        <button
                          type="button"
                          onClick={() => setNewsFilter("expired")}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                            newsFilter === "expired" ? "bg-primary text-on-primary shadow-xs" : "text-muted hover:text-foreground"
                          }`}
                        >
                          Sudah Auto Hilang ({newsList.filter((n) => n.expired || (n.expiresAt && new Date(n.expiresAt) <= new Date())).length})
                        </button>
                        <button
                          type="button"
                          onClick={() => setNewsFilter("all")}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                            newsFilter === "all" ? "bg-primary text-on-primary shadow-xs" : "text-muted hover:text-foreground"
                          }`}
                        >
                          Semua ({newsList.length})
                        </button>
                      </div>
                      <span className="text-xs text-muted font-mono">{newsList.length} total berita</span>
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
                      (() => {
                        const now = new Date();
                        const displayed = newsList.filter((item) => {
                          const isExp = item.expired || (item.expiresAt ? new Date(item.expiresAt) <= now : false);
                          if (newsFilter === "active") return !isExp;
                          if (newsFilter === "expired") return isExp;
                          return true;
                        });

                        if (displayed.length === 0) {
                          return (
                            <div className="bg-surface p-8 text-center rounded-2xl border border-border text-muted text-xs shadow-xs">
                              {newsFilter === "active"
                                ? "Tidak ada berita aktif. Semua berita telah selesai tayang / kedaluwarsa."
                                : newsFilter === "expired"
                                ? "Belum ada berita yang kedaluwarsa."
                                : "Tidak ada berita ditemukan."}
                            </div>
                          );
                        }

                        return displayed.map((item) => {
                          const isExp = item.expired || (item.expiresAt ? new Date(item.expiresAt) <= now : false);
                          const isEditingThis = editingNewsExpiryId === item.id;

                          return (
                            <div
                              key={item.id}
                              className={`bg-surface p-5 rounded-2xl border shadow-xs transition space-y-3 ${
                                isExp ? "border-error/30 bg-error/5" : "border-border hover:border-border-strong"
                              }`}
                            >
                              <div className="flex items-start justify-between gap-4">
                                <div className="flex items-start gap-3.5 flex-1 min-w-0">
                                  {item.imageUrl ? (
                                    <div className="w-16 h-16 rounded-xl overflow-hidden shrink-0 border border-border bg-surface-subtle">
                                      <img src={item.imageUrl} alt={item.title} className="w-full h-full object-cover" />
                                    </div>
                                  ) : (
                                    <div className="w-16 h-16 rounded-xl border border-dashed border-border bg-surface-subtle/50 flex flex-col items-center justify-center shrink-0 text-muted/60">
                                      <ImageIcon className="w-5 h-5" />
                                      <span className="text-[9px] mt-0.5">Tanpa foto</span>
                                    </div>
                                  )}
                                  <div className="space-y-1.5 flex-1 min-w-0">
                                    <h4 className="text-sm font-bold text-foreground leading-snug">{item.title}</h4>
                                    {item.summary && <p className="text-xs text-muted leading-relaxed line-clamp-2">{item.summary}</p>}
                                    <div className="flex flex-wrap items-center gap-2 pt-1 text-[11px]">
                                      {item.publishedAt && (
                                        <span className="text-muted/80 font-mono">{item.publishedAt}</span>
                                      )}
                                      {item.expiresAt ? (
                                        isExp ? (
                                          <span className="px-2 py-0.5 rounded-md font-semibold bg-error/15 text-error border border-error/20 flex items-center gap-1">
                                            <Clock className="w-3 h-3" /> Auto hilang (kedaluwarsa)
                                          </span>
                                        ) : (
                                          <span className="px-2 py-0.5 rounded-md font-medium bg-amber-500/10 text-amber-500 border border-amber-500/20 flex items-center gap-1">
                                            <Clock className="w-3 h-3" /> Auto hilang:{" "}
                                            {new Date(item.expiresAt).toLocaleDateString("id-ID", {
                                              day: "numeric",
                                              month: "short",
                                              year: "numeric",
                                              hour: "2-digit",
                                              minute: "2-digit",
                                            })}
                                          </span>
                                        )
                                      ) : (
                                        <span className="text-muted/70 text-[10px] font-medium">Tayang permanen</span>
                                      )}
                                    </div>
                                  </div>
                                </div>
                                <div className="flex items-center gap-1 shrink-0">
                                  <button
                                    type="button"
                                    onClick={() => {
                                      setEditingNewsItem(item);
                                      setEditNewsTitle(item.title);
                                      setEditNewsSummary(item.summary || "");
                                      setEditNewsContent(item.content || "");
                                      setEditNewsImageUrl(item.imageUrl || "");
                                      setEditNewsExpiresAt(
                                        item.expiresAt ? new Date(item.expiresAt).toISOString().slice(0, 16) : ""
                                      );
                                    }}
                                    className="p-2 text-muted hover:text-primary hover:bg-surface-subtle rounded-lg transition"
                                    title="Edit berita & foto"
                                    aria-label="Edit berita & foto"
                                  >
                                    <Pencil className="w-4 h-4" />
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => {
                                      if (isEditingThis) {
                                        setEditingNewsExpiryId(null);
                                      } else {
                                        setEditingNewsExpiryId(item.id);
                                        setEditExpiryDate(
                                          item.expiresAt ? new Date(item.expiresAt).toISOString().slice(0, 16) : ""
                                        );
                                      }
                                    }}
                                    className="p-2 text-muted hover:text-primary hover:bg-surface-subtle rounded-lg transition"
                                    title="Atur waktu auto hilang"
                                    aria-label="Atur waktu auto hilang"
                                  >
                                    <Clock className="w-4 h-4" />
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => handleDeleteNews(item.id)}
                                    className="p-2 text-muted hover:text-error hover:bg-error/10 rounded-lg transition"
                                    title="Hapus berita"
                                    aria-label="Hapus berita"
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </div>
                              </div>

                              {isEditingThis && (
                                <div className="p-3 bg-surface-subtle rounded-xl border border-border space-y-2">
                                  <div className="flex items-center justify-between">
                                    <span className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                                      <Clock className="w-3.5 h-3.5 text-primary" />
                                      Atur Waktu Auto Hilang Berita Ini
                                    </span>
                                    <button
                                      type="button"
                                      onClick={() => setEditingNewsExpiryId(null)}
                                      className="text-xs text-muted hover:text-foreground"
                                    >
                                      Batal
                                    </button>
                                  </div>
                                  <div className="flex items-center gap-2">
                                    <input
                                      type="datetime-local"
                                      value={editExpiryDate}
                                      onChange={(e) => setEditExpiryDate(e.target.value)}
                                      className={inputClass}
                                    />
                                    <button
                                      type="button"
                                      onClick={() => handleUpdateNewsExpiry(item.id, editExpiryDate || null)}
                                      className="px-3 py-2 bg-primary text-on-primary rounded-xl text-xs font-semibold shrink-0"
                                    >
                                      Simpan
                                    </button>
                                    {item.expiresAt && (
                                      <button
                                        type="button"
                                        onClick={() => handleUpdateNewsExpiry(item.id, null)}
                                        className="px-3 py-2 bg-surface hover:bg-error/10 hover:text-error text-muted border border-border rounded-xl text-xs font-semibold shrink-0"
                                      >
                                        Hapus Batas
                                      </button>
                                    )}
                                  </div>
                                </div>
                              )}
                            </div>
                          );
                        });
                      })()
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
                                          ) : modFields[key] === "audio" ? (
                                            item[key] ? (
                                              <audio controls src={String(item[key])} className="h-7 w-48" preload="none" />
                                            ) : (
                                              <span className="text-xs text-muted italic">(belum ada audio)</span>
                                            )
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
                                        .map(([k, v]) => {
                                          if (modFields && modFields[k] === "audio") {
                                            return (
                                              <div key={k} className="w-full mt-1">
                                                <span className="text-xs text-muted block mb-1">{humanize(k)}:</span>
                                                {v ? (
                                                  <audio controls src={String(v)} className="h-7 w-full" preload="none" />
                                                ) : (
                                                  <span className="text-xs text-muted italic">(belum ada audio)</span>
                                                )}
                                              </div>
                                            );
                                          }
                                          return (
                                            <span key={k} className="text-xs bg-surface-subtle border border-border px-2 py-0.5 rounded-lg">
                                              <span className="text-muted">{humanize(k)}:</span> {String(v)}
                                            </span>
                                          );
                                        })}
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

            {/* AKSES PENGELOLA */}
            {current?.id === "access" && canManageAccess && (
              <div className="space-y-6">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h2 className="text-xl font-bold tracking-tight text-foreground">{current.label}</h2>
                    <p className="text-sm text-muted mt-0.5">
                      Kelola kredensial (username & password) untuk masuk ke Portal Pengurus. Tingkat peran menentukan batasan wewenang.
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => token && loadAccess(token)}
                    className="inline-flex items-center gap-1.5 text-xs font-medium px-3 py-2 rounded-lg border border-border bg-surface hover:bg-surface-subtle text-foreground transition shrink-0"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                    <span>Segarkan</span>
                  </button>
                </div>

                {loadingAccess ? (
                  <div className="py-16 text-center text-muted">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                    <p className="text-xs">Memuat data akses...</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {/* Pemilik (Owner) */}
                    {(() => {
                      const ownerCred = accessList.find((c) => c.role === "owner");
                      const canEditOwner = user.role === "owner";
                      return (
                        <div className="bg-surface p-5 rounded-2xl border border-border shadow-xs flex flex-col justify-between gap-4">
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-[11px] font-bold tracking-wider uppercase px-2.5 py-1 rounded-full bg-primary-container text-on-primary-container border border-primary/20">
                                Kendali Penuh
                              </span>
                              <div className="w-8 h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                                <ShieldCheck className="w-4 h-4" />
                              </div>
                            </div>
                            <div>
                              <h3 className="text-base font-bold text-foreground">Pemilik (Owner)</h3>
                              <p className="text-xs text-muted mt-1 leading-relaxed">
                                Memiliki kendali mutlak atas seluruh konten, data formulir pendaftar, dan pengaturan akun pengurus lain.
                              </p>
                            </div>
                            <div className="pt-2 border-t border-border">
                              <div className="text-[11px] text-muted">Username Aktif:</div>
                              <div className="text-sm font-mono font-bold text-foreground mt-0.5">
                                @{ownerCred?.username || user.username}
                              </div>
                            </div>
                          </div>
                          <button
                            type="button"
                            disabled={!canEditOwner}
                            onClick={() => {
                              setEditingCredRole("owner");
                              setNewCredUsername(ownerCred?.username || user.username);
                              setNewCredPassword("");
                            }}
                            className={`w-full py-2.5 px-3 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition ${
                              canEditOwner
                                ? "bg-primary text-on-primary hover:opacity-90"
                                : "bg-surface-subtle text-muted cursor-not-allowed border border-border"
                            }`}
                          >
                            <Pencil className="w-3.5 h-3.5" />
                            <span>{canEditOwner ? "Atur Kredensial" : "Hanya untuk Pemilik"}</span>
                          </button>
                        </div>
                      );
                    })()}

                    {/* Admin */}
                    {(() => {
                      const adminCred = accessList.find((c) => c.role === "admin");
                      const canEditAdmin = user.role === "owner";
                      return (
                        <div className="bg-surface p-5 rounded-2xl border border-border shadow-xs flex flex-col justify-between gap-4">
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-[11px] font-bold tracking-wider uppercase px-2.5 py-1 rounded-full bg-accent/15 text-accent border border-accent/20">
                                Operasional & Tim
                              </span>
                              <div className="w-8 h-8 rounded-lg bg-accent/15 text-accent flex items-center justify-center">
                                <Users className="w-4 h-4" />
                              </div>
                            </div>
                            <div>
                              <h3 className="text-base font-bold text-foreground">Admin</h3>
                              <p className="text-xs text-muted mt-1 leading-relaxed">
                                Dapat mengelola seluruh konten, melihat isian formulir pendaftaran, dan mengatur akun Editor.
                              </p>
                            </div>
                            <div className="pt-2 border-t border-border">
                              <div className="text-[11px] text-muted">Username Aktif:</div>
                              <div className="text-sm font-mono font-bold text-foreground mt-0.5">
                                {adminCred?.username ? `@${adminCred.username}` : <span className="text-muted font-sans text-xs">Belum diatur</span>}
                              </div>
                            </div>
                          </div>
                          <button
                            type="button"
                            disabled={!canEditAdmin}
                            onClick={() => {
                              setEditingCredRole("admin");
                              setNewCredUsername(adminCred?.username || "admin." + slug);
                              setNewCredPassword("");
                            }}
                            className={`w-full py-2.5 px-3 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition ${
                              canEditAdmin
                                ? "bg-primary text-on-primary hover:opacity-90"
                                : "bg-surface-subtle text-muted cursor-not-allowed border border-border"
                            }`}
                          >
                            <Pencil className="w-3.5 h-3.5" />
                            <span>{canEditAdmin ? "Atur Kredensial" : "Hanya Pemilik yang Boleh Ubah"}</span>
                          </button>
                        </div>
                      );
                    })()}

                    {/* Editor */}
                    {(() => {
                      const editorCred = accessList.find((c) => c.role === "editor");
                      const canEditEditor = user.role === "owner" || user.role === "admin";
                      return (
                        <div className="bg-surface p-5 rounded-2xl border border-border shadow-xs flex flex-col justify-between gap-4">
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="text-[11px] font-bold tracking-wider uppercase px-2.5 py-1 rounded-full bg-surface-subtle text-muted border border-border">
                                Konten Saja
                              </span>
                              <div className="w-8 h-8 rounded-lg bg-surface-subtle text-muted flex items-center justify-center">
                                <Pencil className="w-4 h-4" />
                              </div>
                            </div>
                            <div>
                              <h3 className="text-base font-bold text-foreground">Editor</h3>
                              <p className="text-xs text-muted mt-1 leading-relaxed">
                                Hanya dapat mengedit berita dan modul. Tidak bisa melihat formulir pendaftaran dan tidak bisa mengakses menu Akses ini.
                              </p>
                            </div>
                            <div className="pt-2 border-t border-border">
                              <div className="text-[11px] text-muted">Username Aktif:</div>
                              <div className="text-sm font-mono font-bold text-foreground mt-0.5">
                                {editorCred?.username ? `@${editorCred.username}` : <span className="text-muted font-sans text-xs">Belum diatur</span>}
                              </div>
                            </div>
                          </div>
                          <button
                            type="button"
                            disabled={!canEditEditor}
                            onClick={() => {
                              setEditingCredRole("editor");
                              setNewCredUsername(editorCred?.username || "editor." + slug);
                              setNewCredPassword("");
                            }}
                            className={`w-full py-2.5 px-3 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition ${
                              canEditEditor
                                ? "bg-primary text-on-primary hover:opacity-90"
                                : "bg-surface-subtle text-muted cursor-not-allowed border border-border"
                            }`}
                          >
                            <Pencil className="w-3.5 h-3.5" />
                            <span>{canEditEditor ? "Atur Kredensial" : "Akses Terbatas"}</span>
                          </button>
                        </div>
                      );
                    })()}
                  </div>
                )}
              </div>
            )}

            {/* KELOLA SECTION (SECTION MANAGER OPSI 1) */}
            {current?.id === "sections" && canManageAccess && (
              <div className="space-y-5">
                {/* Header Bagian Atas */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3.5 pb-1 border-b border-border/50">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-xl bg-primary-container text-on-primary-container flex items-center justify-center shrink-0 shadow-xs">
                        <Boxes className="w-4 h-4" />
                      </div>
                      <div>
                        <h2 className="text-lg sm:text-xl font-bold tracking-tight text-foreground leading-tight">{current.label}</h2>
                        <span className="text-[11px] font-medium text-muted">Tata Letak & Urutan Halaman Beranda</span>
                      </div>
                    </div>
                    <p className="text-xs sm:text-sm text-muted leading-relaxed">
                      Atur urutan tampilan section, sembunyikan section yang belum siap, atau kembalikan ke susunan bawaan.
                    </p>
                  </div>
                  <div className="grid grid-cols-2 sm:flex sm:items-center gap-2 w-full sm:w-auto shrink-0">
                    <button
                      type="button"
                      onClick={() => setSectionList(DEFAULT_SECTIONS)}
                      className="inline-flex items-center justify-center gap-1.5 text-xs font-semibold px-3 py-2.5 rounded-xl border border-border bg-surface hover:bg-surface-subtle text-muted hover:text-foreground transition shadow-xs"
                      title="Kembalikan susunan section ke urutan standar"
                    >
                      <RefreshCw className="w-3.5 h-3.5" />
                      <span>Reset Bawaan</span>
                    </button>
                    <button
                      type="button"
                      disabled={savingSections}
                      onClick={handleSaveSections}
                      className="inline-flex items-center justify-center gap-1.5 text-xs font-semibold px-4 py-2.5 rounded-xl bg-primary text-on-primary hover:opacity-90 transition shadow-sm disabled:opacity-50"
                    >
                      {savingSections ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                      <span className="whitespace-nowrap">Simpan Urutan</span>
                    </button>
                  </div>
                </div>

                {loadingSections ? (
                  <div className="py-16 text-center text-muted">
                    <Loader2 className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
                    <p className="text-xs">Memuat section halaman...</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {/* Struktur Tetap Atas (Hero & Sticky Navbar) */}
                    <div className="rounded-2xl border border-dashed border-border/80 bg-surface-subtle/40 p-3 sm:p-4 space-y-2.5">
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-[11px] font-bold uppercase tracking-wider text-muted flex items-center gap-1.5">
                          <span>🔒</span>
                          <span>Struktur Tetap di Bagian Atas</span>
                        </span>
                        <span className="text-[10px] font-semibold text-muted bg-surface px-2 py-0.5 rounded-md border border-border/70">
                          Posisi Kunci
                        </span>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                        <div className="bg-surface/90 p-2.5 sm:p-3 rounded-xl border border-border/60 flex items-center gap-2.5 shadow-2xs">
                          <span className="text-[11px] font-mono font-bold text-muted bg-surface-subtle px-1.5 py-0.5 rounded border border-border/70 shrink-0">
                            #hero
                          </span>
                          <div className="min-w-0 flex-1">
                            <div className="text-xs font-bold text-foreground truncate">Hero Section (100vh)</div>
                            <div className="text-[10px] text-muted truncate">Headline, deskripsi, CTA, foto ketua</div>
                          </div>
                        </div>

                        <div className="bg-surface/90 p-2.5 sm:p-3 rounded-xl border border-border/60 flex items-center gap-2.5 shadow-2xs">
                          <span className="text-[11px] font-mono font-bold text-muted bg-surface-subtle px-1.5 py-0.5 rounded border border-border/70 shrink-0">
                            #navbar
                          </span>
                          <div className="min-w-0 flex-1">
                            <div className="text-xs font-bold text-foreground truncate">Sticky Navbar on Scroll</div>
                            <div className="text-[10px] text-muted truncate">Menempel otomatis setelah melewati Hero</div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Header Bagian Section Dinamis */}
                    <div className="flex items-center justify-between pt-1 px-1">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-foreground">Urutan Section Dinamis</span>
                        <span className="text-[10px] font-semibold text-primary bg-primary-container px-2 py-0.5 rounded-full">
                          {sectionList.length} Section
                        </span>
                      </div>
                      <span className="text-[11px] text-muted hidden sm:inline">
                        Gunakan panah naik/turun untuk mengubah urutan
                      </span>
                    </div>

                    {/* Daftar Section yang Bisa Di-reorder */}
                    <div className="space-y-2.5">
                      {sectionList.map((sec, idx) => (
                        <div
                          key={sec.id}
                          className={`bg-surface p-3 sm:p-3.5 rounded-xl border transition flex items-center justify-between gap-3 ${
                            sec.enabled ? "border-border shadow-xs" : "border-border/50 opacity-60 bg-surface-subtle"
                          }`}
                        >
                          <div className="flex items-center gap-2.5 sm:gap-3 min-w-0 flex-1">
                            <span className="w-6 h-6 sm:w-7 sm:h-7 rounded-lg bg-surface-subtle border border-border flex items-center justify-center text-xs font-bold font-mono text-muted shrink-0">
                              {idx + 1}
                            </span>
                            <div className="min-w-0 flex-1">
                              <div className="flex items-center gap-1.5 flex-wrap">
                                <span className="text-xs sm:text-sm font-bold text-foreground truncate">{sec.name}</span>
                                <span className="text-[10px] font-mono text-muted bg-surface-subtle px-1.5 py-0.2 rounded border border-border/60">
                                  #{sec.id}
                                </span>
                              </div>
                              <div className="text-[11px] text-muted mt-0.5 flex flex-wrap items-center gap-1.5">
                                <span className={`inline-flex items-center gap-1 font-semibold ${sec.enabled ? "text-primary" : "text-muted"}`}>
                                  <span className={`w-1.5 h-1.5 rounded-full ${sec.enabled ? "bg-primary" : "bg-muted"}`} />
                                  <span>{sec.enabled ? "Tampil" : "Disembunyikan"}</span>
                                </span>
                                {sec.startAt && <span>• Mulai: {sec.startAt}</span>}
                                {sec.endAt && <span>• Selesai: {sec.endAt}</span>}
                              </div>
                            </div>
                          </div>

                          <div className="flex items-center gap-1.5 shrink-0">
                            {/* Tombol Reorder Naik & Turun (Segmented Control) */}
                            <div className="flex items-center bg-surface-subtle rounded-lg border border-border p-0.5">
                              <button
                                type="button"
                                title="Pindahkan ke atas"
                                disabled={idx === 0}
                                onClick={() => moveSection(idx, "up")}
                                className="w-7 h-7 sm:w-8 sm:h-8 rounded-md text-foreground hover:bg-surface flex items-center justify-center disabled:opacity-25 disabled:cursor-not-allowed transition"
                              >
                                <ArrowUp className="w-3.5 h-3.5" />
                              </button>
                              <button
                                type="button"
                                title="Pindahkan ke bawah"
                                disabled={idx === sectionList.length - 1}
                                onClick={() => moveSection(idx, "down")}
                                className="w-7 h-7 sm:w-8 sm:h-8 rounded-md text-foreground hover:bg-surface flex items-center justify-center disabled:opacity-25 disabled:cursor-not-allowed transition"
                              >
                                <ArrowDown className="w-3.5 h-3.5" />
                              </button>
                            </div>

                            {/* Tombol Toggle Visibilitas */}
                            <button
                              type="button"
                              onClick={() => toggleSection(sec.id)}
                              className={`px-2.5 sm:px-3 py-1.5 rounded-lg text-xs font-semibold border transition ${
                                sec.enabled
                                  ? "bg-primary-container text-on-primary-container border-primary/30"
                                  : "bg-surface-subtle text-muted border-border"
                              }`}
                            >
                              {sec.enabled ? "Aktif" : "Mati"}
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Footer Terkunci */}
                    <div className="bg-surface-subtle/40 p-3 sm:p-3.5 rounded-xl border border-dashed border-border/80 flex items-center justify-between opacity-80 gap-2">
                      <div className="flex items-center gap-2.5 min-w-0">
                        <span className="text-[11px] font-mono font-bold text-muted bg-surface px-1.5 py-0.5 rounded border border-border shrink-0">#footer</span>
                        <div className="min-w-0">
                          <div className="text-xs font-bold text-foreground truncate">Footer Website</div>
                          <div className="text-[10px] text-muted truncate">Hak cipta, navigasi sekunder, dan sosial media</div>
                        </div>
                      </div>
                      <span className="text-[10px] font-semibold text-muted bg-surface px-2 py-0.5 rounded-full border border-border shrink-0">
                        🔒 Terkunci di Paling Bawah
                      </span>
                    </div>
                  </div>
                )}
              </div>
            )}
          </main>
        </div>
      </div>

      {/* Modal Edit Berita & Foto */}
      {editingNewsItem && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs transition-opacity overflow-y-auto"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-edit-news-title"
        >
          <div className="bg-surface border border-border w-full max-w-lg rounded-2xl shadow-2xl p-6 space-y-4 my-8 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between border-b border-border pb-3">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                  <Pencil className="w-4 h-4" />
                </div>
                <div>
                  <h3 id="modal-edit-news-title" className="text-base font-bold text-foreground">
                    Edit Berita
                  </h3>
                  <p className="text-xs text-muted">Perbarui judul, foto sampul, ringkasan, atau batas waktu tayang.</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setEditingNewsItem(null)}
                className="p-1.5 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition"
                aria-label="Tutup modal"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSaveNewsEdit} className="space-y-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-foreground">
                  Judul Berita <span className="text-error">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={editNewsTitle}
                  onChange={(e) => setEditNewsTitle(e.target.value)}
                  className={inputClass}
                />
              </div>

              {/* Edit Foto Sampul Berita */}
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <label className="block text-xs font-semibold text-foreground flex items-center gap-1.5">
                    <ImageIcon className="w-3.5 h-3.5 text-primary" />
                    <span>Foto Sampul (Opsional)</span>
                  </label>
                  {editNewsImageUrl && (
                    <button
                      type="button"
                      onClick={() => setEditNewsImageUrl("")}
                      className="text-[10px] text-error hover:underline flex items-center gap-0.5"
                    >
                      <X className="w-3 h-3" /> Hapus Foto (Jadikan Tanpa Foto)
                    </button>
                  )}
                </div>

                {editNewsImageUrl ? (
                  <div className="relative rounded-xl overflow-hidden border border-border group bg-surface-subtle">
                    <img
                      src={editNewsImageUrl}
                      alt="Pratinjau foto berita"
                      className="w-full h-40 object-cover"
                    />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-2">
                      <label className="cursor-pointer px-3 py-1.5 rounded-lg bg-surface/90 text-foreground text-xs font-semibold hover:bg-surface transition flex items-center gap-1">
                        <ImagePlus className="w-3.5 h-3.5" /> Ganti Foto
                        <input
                          type="file"
                          accept="image/*"
                          className="hidden"
                          onChange={(e) => handleUploadNewsPhoto(e.target.files?.[0], true)}
                          disabled={uploadingEditNewsPhoto}
                        />
                      </label>
                      <button
                        type="button"
                        onClick={() => setEditNewsImageUrl("")}
                        className="p-1.5 rounded-lg bg-error text-white hover:bg-error/90 transition"
                        title="Hapus foto"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                ) : (
                  <label className="flex flex-col items-center justify-center gap-2 p-4 border-2 border-dashed border-border hover:border-primary/50 hover:bg-surface-subtle/50 rounded-xl cursor-pointer transition text-center group">
                    {uploadingEditNewsPhoto ? (
                      <Loader2 className="w-5 h-5 animate-spin text-primary" />
                    ) : (
                      <ImagePlus className="w-5 h-5 text-muted group-hover:text-primary transition" />
                    )}
                    <div className="text-xs">
                      <span className="font-semibold text-foreground group-hover:text-primary transition">
                        {uploadingEditNewsPhoto ? "Mengunggah foto..." : "Pilih atau Unggah Foto Baru"}
                      </span>
                      <p className="text-[10px] text-muted mt-0.5">JPG, PNG, WebP • Opsional (bisa tanpa foto)</p>
                    </div>
                    <input
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={(e) => handleUploadNewsPhoto(e.target.files?.[0], true)}
                      disabled={uploadingEditNewsPhoto}
                    />
                  </label>
                )}
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-foreground">
                  Ringkasan Berita
                </label>
                <input
                  type="text"
                  value={editNewsSummary}
                  onChange={(e) => setEditNewsSummary(e.target.value)}
                  className={inputClass}
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-foreground">
                  Isi Lengkap Berita
                </label>
                <textarea
                  rows={3}
                  value={editNewsContent}
                  onChange={(e) => setEditNewsContent(e.target.value)}
                  className={inputClass}
                />
              </div>

              <div className="space-y-1.5 pt-1 border-t border-border">
                <div className="flex items-center justify-between">
                  <label className="block text-xs font-semibold text-foreground flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-primary" />
                    <span>Auto Hilang (Kedaluwarsa)</span>
                  </label>
                  {editNewsExpiresAt && (
                    <button
                      type="button"
                      onClick={() => setEditNewsExpiresAt("")}
                      className="text-[10px] text-muted hover:text-error transition"
                    >
                      Hapus batas
                    </button>
                  )}
                </div>
                <input
                  type="datetime-local"
                  value={editNewsExpiresAt}
                  onChange={(e) => setEditNewsExpiresAt(e.target.value)}
                  className={inputClass}
                />
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-border">
                <button
                  type="button"
                  onClick={() => setEditingNewsItem(null)}
                  className="px-4 py-2 text-xs font-medium text-foreground hover:bg-surface-subtle rounded-xl border border-border transition"
                >
                  Batal
                </button>
                <button
                  type="submit"
                  disabled={savingNewsEdit || !editNewsTitle.trim()}
                  className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-primary text-on-primary hover:opacity-90 rounded-xl transition disabled:opacity-50"
                >
                  {savingNewsEdit ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                  <span>Simpan Perubahan</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Edit Kredensial Pengurus */}
      {editingCredRole && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs transition-opacity"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-cred-title"
        >
          <div className="bg-surface border border-border w-full max-w-md rounded-2xl shadow-2xl p-6 space-y-5 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-primary-container text-on-primary-container flex items-center justify-center">
                  <ShieldCheck className="w-4 h-4" />
                </div>
                <div>
                  <h3 id="modal-cred-title" className="text-base font-bold text-foreground">
                    Atur Kredensial {editingCredRole.toUpperCase()}
                  </h3>
                  <p className="text-xs text-muted">Ubah username atau password login portal.</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setEditingCredRole(null)}
                className="p-1.5 text-muted hover:text-foreground hover:bg-surface-subtle rounded-lg transition"
                aria-label="Tutup modal"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSaveCred} className="space-y-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-foreground">
                  Username <span className="text-error">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={newCredUsername}
                  onChange={(e) => setNewCredUsername(e.target.value)}
                  placeholder={`Contoh: ${editingCredRole}.${slug}`}
                  className="w-full px-3.5 py-2.5 text-sm bg-surface-subtle border border-border rounded-xl text-foreground focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition"
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-foreground">
                  Password Baru
                </label>
                <div className="relative">
                  <input
                    type={showCredPassword ? "text" : "password"}
                    value={newCredPassword}
                    onChange={(e) => setNewCredPassword(e.target.value)}
                    placeholder="Kosongkan jika tidak ingin mengubah password"
                    className="w-full pl-3.5 pr-10 py-2.5 text-sm bg-surface-subtle border border-border rounded-xl text-foreground focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition"
                  />
                  <button
                    type="button"
                    onClick={() => setShowCredPassword(!showCredPassword)}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-muted hover:text-foreground transition"
                    aria-label={showCredPassword ? "Sembunyikan password" : "Lihat password"}
                    title={showCredPassword ? "Sembunyikan password" : "Lihat password"}
                  >
                    {showCredPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
                <p className="text-[11px] text-muted">
                  Minimal 6 karakter jika ingin mengganti password baru.
                </p>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-border">
                <button
                  type="button"
                  onClick={() => setEditingCredRole(null)}
                  className="px-4 py-2 text-xs font-medium text-foreground hover:bg-surface-subtle rounded-xl border border-border transition"
                >
                  Batal
                </button>
                <button
                  type="submit"
                  disabled={savingCred || !newCredUsername.trim()}
                  className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-primary text-on-primary hover:opacity-90 rounded-xl transition disabled:opacity-50"
                >
                  {savingCred ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                  <span>Simpan Kredensial</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

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
