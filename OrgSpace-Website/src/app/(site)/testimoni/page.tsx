"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { ALL_TESTIMONIALS, type TestimonialItem } from "@/lib/testimonials-data";

type CategoryFilter = "all" | "bem" | "osis" | "ukm" | "himpunan" | "pramuka" | "komunitas";

const CATEGORY_TABS: { key: CategoryFilter; label: string }[] = [
  { key: "all", label: "Semua Kategori" },
  { key: "bem", label: "BEM & DEMA" },
  { key: "osis", label: "OSIS & MPK" },
  { key: "ukm", label: "UKM & Seni" },
  { key: "himpunan", label: "Himpunan Jurusan" },
  { key: "pramuka", label: "Pramuka & Paskibra" },
  { key: "komunitas", label: "Komunitas & Relawan" },
];

export default function TestimoniPage() {
  const [userTestimonials, setUserTestimonials] = useState<TestimonialItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Form states
  const [name, setName] = useState("");
  const [org, setOrg] = useState("");
  const [role, setRole] = useState("");
  const [content, setContent] = useState("");
  const [categoryKey, setCategoryKey] = useState<TestimonialItem["categoryKey"]>("komunitas");
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState<number | null>(null);
  const [submitted, setSubmitted] = useState(false);

  // Load testimonials from localStorage safely on client
  useEffect(() => {
    const timer = setTimeout(() => {
      try {
        const stored = localStorage.getItem("orgspace_user_testimonials");
        if (stored) {
          const parsed = JSON.parse(stored) as TestimonialItem[];
          if (Array.isArray(parsed) && parsed.length > 0) {
            setUserTestimonials(parsed);
          }
        }
      } catch {
        // Fallback silently if localStorage is not accessible
      }
    }, 0);

    return () => clearTimeout(timer);
  }, []);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!name.trim() || !org.trim() || !content.trim()) return;

    // Create initials
    const initials =
      name
        .split(" ")
        .map((n) => n[0])
        .filter(Boolean)
        .slice(0, 2)
        .join("")
        .toUpperCase() || "PL";

    const newTestimonial: TestimonialItem = {
      id: `user-${Date.now()}`,
      name: name.trim(),
      role: role.trim() || "Pengurus Organisasi",
      org: org.trim(),
      category: "Testimoni Pengguna",
      categoryKey,
      rating,
      date: "Baru saja",
      content: content.trim(),
      avatarBg: "bg-emerald-600 text-white",
      avatarText: initials,
      isUserAdded: true,
    };

    const updated = [newTestimonial, ...userTestimonials];
    setUserTestimonials(updated);

    try {
      localStorage.setItem("orgspace_user_testimonials", JSON.stringify(updated));
    } catch {
      // Ignore storage errors
    }

    setSubmitted(true);
    setTimeout(() => {
      setSubmitted(false);
      setIsModalOpen(false);
      setName("");
      setOrg("");
      setRole("");
      setContent("");
      setRating(5);
    }, 1600);
  }

  const allTestimonials = [...userTestimonials, ...ALL_TESTIMONIALS];

  // Filter based on search & category tab
  const filteredTestimonials = allTestimonials.filter((item) => {
    const matchesCategory = selectedCategory === "all" || item.categoryKey === selectedCategory;
    const q = searchQuery.toLowerCase().trim();
    const matchesSearch =
      !q ||
      item.name.toLowerCase().includes(q) ||
      item.org.toLowerCase().includes(q) ||
      item.role.toLowerCase().includes(q) ||
      item.content.toLowerCase().includes(q);

    return matchesCategory && matchesSearch;
  });

  return (
    <div className="relative min-h-screen bg-gradient-to-b from-[#F8FAFC] via-[#FFFFFF] to-[#F8FAFC] dark:from-[#07152B] dark:via-[#091D3A] dark:to-[#07152B] text-[#0B1E36] dark:text-white py-16 sm:py-24 transition-colors duration-300">
      <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Back Link to Landing */}
        <div className="mb-6">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-xs sm:text-sm font-bold text-[#64748B] dark:text-slate-400 hover:text-[#0099FF] dark:hover:text-[#00C9F2] transition-colors"
          >
            <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M17 10a.75.75 0 01-.75.75H5.612l4.158 3.96a.75.75 0 11-1.04 1.08l-5.5-5.25a.75.75 0 010-1.08l5.5-5.25a.75.75 0 111.04 1.08L5.612 9.25H16.25A.75.75 0 0117 10z"
                clipRule="evenodd"
              />
            </svg>
            <span>Kembali ke Beranda OrgSpace</span>
          </Link>
        </div>

        {/* Page Header */}
        <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-6 border-b border-slate-200/80 dark:border-[#1E3E6B] pb-10">
          <div className="max-w-3xl">
            <div className="inline-flex items-center gap-2 rounded-full border border-purple-200/90 dark:border-purple-800/80 bg-purple-50/80 dark:bg-purple-950/60 px-4 py-1.5 text-xs font-bold text-purple-700 dark:text-purple-300 shadow-xs mb-3.5">
              <span className="size-1.5 rounded-full bg-purple-500 animate-pulse" />
              Semua Ulasan & Pengalaman Pelanggan
            </div>

            <h1 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight text-[#0B1E36] dark:text-white leading-[1.15]">
              Kata Pelanggan Kami
            </h1>

            <p className="mt-3.5 text-base sm:text-lg text-[#64748B] dark:text-slate-300 leading-relaxed">
              Kumpulan cerita nyata dari rekan-rekan pengurus OSIS, BEM, UKM, dan komunitas yang telah berhasil mendigitalkan kegiatannya tanpa koding lewat OrgSpace.
            </p>
          </div>

          {/* Action: Tulis Testimoni */}
          <div className="shrink-0">
            <button
              type="button"
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center gap-2 rounded-2xl bg-[#0099FF] px-6 py-3.5 text-xs sm:text-sm font-bold text-white shadow-md shadow-blue-500/25 hover:bg-[#0084FF] hover:shadow-lg hover:shadow-blue-500/35 hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer"
            >
              <svg className="size-4 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
              <span>+ Tulis Testimoni Kamu</span>
            </button>
          </div>
        </div>

        {/* Filter Bar: Category Tabs & Search Box */}
        <div className="mt-8 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
          {/* Category Tabs */}
          <div className="flex flex-wrap items-center gap-1.5">
            {CATEGORY_TABS.map((tab) => {
              const isActive = selectedCategory === tab.key;
              return (
                <button
                  key={tab.key}
                  type="button"
                  onClick={() => setSelectedCategory(tab.key)}
                  className={`rounded-xl px-3.5 py-2 text-xs font-bold transition-all duration-150 cursor-pointer ${
                    isActive
                      ? "bg-[#0B1E36] dark:bg-[#0099FF] text-white shadow-xs"
                      : "bg-white dark:bg-[#0C2E5B] border border-slate-200/90 dark:border-[#1E3E6B] text-[#64748B] dark:text-slate-300 hover:text-[#0B1E36] dark:hover:text-white hover:bg-slate-50 dark:hover:bg-[#102C52]"
                  }`}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>

          {/* Search Input */}
          <div className="relative w-full lg:w-72 shrink-0">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Cari nama, organisasi, atau kata kunci..."
              className="w-full rounded-xl border border-slate-200/90 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] pl-9 pr-3.5 py-2 text-xs text-[#0B1E36] dark:text-white placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-[#0099FF] focus:outline-none focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/40 transition-all shadow-2xs"
            />
            <svg className="absolute left-3 top-2.5 size-4 text-slate-400" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z"
                clipRule="evenodd"
              />
            </svg>
          </div>
        </div>

        {/* Counter Results */}
        <div className="mt-4 flex items-center justify-between text-xs text-[#64748B] dark:text-slate-400">
          <span>
            Menampilkan <strong>{filteredTestimonials.length}</strong> dari <strong>{allTestimonials.length}</strong> ulasan
          </span>
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery("")}
              className="text-[#0099FF] dark:text-[#00C9F2] hover:underline cursor-pointer"
            >
              Reset pencarian
            </button>
          )}
        </div>

        {/* Full Testimonials Grid */}
        {filteredTestimonials.length > 0 ? (
          <div className="mt-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 items-stretch">
            {filteredTestimonials.map((item) => (
              <div
                key={item.id}
                className={`flex flex-col justify-between rounded-3xl border bg-white dark:bg-[#0C2E5B] p-7 sm:p-8 shadow-xs transition-all duration-200 hover:-translate-y-1 hover:shadow-lg ${
                  item.isUserAdded
                    ? "border-emerald-300 dark:border-emerald-500 ring-2 ring-emerald-400/20"
                    : "border-slate-200/90 dark:border-[#1E3E6B] hover:border-purple-200 dark:hover:border-purple-500/50"
                }`}
              >
                <div>
                  {/* Rating, Category & Date */}
                  <div className="flex items-center justify-between gap-2 mb-4">
                    <div className="flex items-center gap-1 text-amber-400">
                      {[...Array(item.rating || 5)].map((_, i) => (
                        <svg key={i} className="size-4.5 fill-current" viewBox="0 0 20 20">
                          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                        </svg>
                      ))}
                    </div>
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold ${
                        item.isUserAdded
                          ? "bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800/60"
                          : "bg-purple-50 dark:bg-purple-950/60 text-purple-700 dark:text-purple-300 border border-purple-200/60 dark:border-purple-800/60"
                      }`}
                    >
                      {item.isUserAdded ? "Ulasan Baru" : item.category}
                    </span>
                  </div>

                  {/* Content */}
                  <p className="text-sm sm:text-[15px] text-[#334155] dark:text-slate-200 leading-relaxed italic">
                    &ldquo;{item.content}&rdquo;
                  </p>
                </div>

                {/* Author Info & Date */}
                <div className="mt-7 pt-5 border-t border-slate-100 dark:border-slate-700/60 flex items-center justify-between">
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className={`flex size-10 shrink-0 items-center justify-center rounded-2xl font-bold text-xs shadow-xs ${item.avatarBg}`}
                    >
                      {item.avatarText}
                    </div>
                    <div className="min-w-0">
                      <h4 className="text-sm font-bold text-[#0B1E36] dark:text-white truncate">{item.name}</h4>
                      <p className="text-xs text-[#64748B] dark:text-slate-300 truncate">{item.role}</p>
                      <p className="text-[11px] font-semibold text-[#0099FF] dark:text-[#00C9F2] truncate">{item.org}</p>
                    </div>
                  </div>
                  {item.date && (
                    <span className="text-[10px] text-[#94A3B8] dark:text-slate-400 shrink-0 ml-2">{item.date}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="mt-12 rounded-3xl border border-slate-200 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] p-12 text-center max-w-lg mx-auto">
            <div className="mx-auto flex size-12 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-800 text-slate-400 mb-3">
              <svg className="size-6" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-5a.75.75 0 01.75.75v4.5a.75.75 0 01-1.5 0v-4.5A.75.75 0 0110 5zm0 10a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <h3 className="text-base font-bold text-[#0B1E36] dark:text-white">Tidak ada testimoni yang cocok</h3>
            <p className="mt-1 text-xs text-[#64748B] dark:text-slate-300">
              Coba gunakan kata kunci pencarian lain atau pilih kategori Semua.
            </p>
            <button
              type="button"
              onClick={() => {
                setSearchQuery("");
                setSelectedCategory("all");
              }}
              className="mt-4 text-xs font-bold text-[#0099FF] dark:text-[#00C9F2] hover:underline"
            >
              Reset Filter
            </button>
          </div>
        )}

        {/* Bottom CTA Card */}
        <div className="mt-16 rounded-3xl bg-gradient-to-br from-[#0B1E36] to-[#07152B] border border-slate-800/40 dark:border-[#1E3E6B] p-8 sm:p-12 text-white text-center shadow-xl">
          <h3 className="text-2xl sm:text-3xl font-black">
            Ingin Organisasimu Memiliki Website Mandiri Seperti Mereka?
          </h3>
          <p className="mt-3 text-sm sm:text-base text-slate-300 max-w-xl mx-auto">
            Mulai sekarang juga. Pilih template yang cocok, isi konten dari HP, dan website resmimu langsung aktif tanpa biaya!
          </p>
          <div className="mt-6 flex flex-wrap items-center justify-center gap-3">
            <a
              href="/downloads/orgspace.apk"
              download="orgspace.apk"
              className="inline-flex items-center justify-center rounded-2xl bg-[#0099FF] px-7 py-3 text-sm font-bold text-white shadow-md hover:bg-[#0084FF] hover:scale-105 active:scale-95 transition-all"
            >
              Mulai Buat Website Gratis (Unduh APK)
            </a>
            <Link
              href="/"
              className="inline-flex items-center justify-center rounded-2xl border border-white/20 bg-white/5 px-6 py-3 text-sm font-bold text-white hover:bg-white/10 transition-all"
            >
              Kembali ke Beranda
            </Link>
          </div>
        </div>
      </div>

      {/* Modal Popup: Form Tulis Testimoni */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
          <div
            className="relative w-full max-w-lg rounded-3xl bg-white dark:bg-[#0C2E5B] p-6 sm:p-8 shadow-2xl border border-slate-200 dark:border-[#1E3E6B]"
            role="dialog"
            aria-modal="true"
          >
            {/* Tombol Tutup */}
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="absolute top-5 right-5 flex size-8 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-600 dark:hover:text-slate-200 transition-colors cursor-pointer"
              aria-label="Tutup form"
            >
              <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            </button>

            {submitted ? (
              <div className="py-8 text-center">
                <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 mb-4">
                  <svg className="size-7" viewBox="0 0 20 20" fill="currentColor">
                    <path
                      fillRule="evenodd"
                      d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-[#0B1E36] dark:text-white">Terima Kasih Banyak! 🎉</h3>
                <p className="mt-2 text-sm text-[#64748B] dark:text-slate-300">
                  Testimonimu telah berhasil ditambahkan dan langsung tampil di halaman ini.
                </p>
              </div>
            ) : (
              <div>
                <div className="mb-6">
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-purple-50 dark:bg-purple-950/60 border border-purple-200 dark:border-purple-800/60 px-3 py-1 text-[11px] font-bold text-purple-700 dark:text-purple-300 mb-2">
                    💬 Bagikan Ceritamu
                  </span>
                  <h3 className="text-xl sm:text-2xl font-bold text-[#0B1E36] dark:text-white">
                    Tulis Testimoni Kamu
                  </h3>
                  <p className="mt-1 text-xs text-[#64748B] dark:text-slate-300">
                    Ceritakan pengalamanmu saat membuat atau mengelola website dengan OrgSpace.
                  </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                  {/* Rating Selector */}
                  <div>
                    <label className="block text-xs font-bold text-[#0B1E36] dark:text-white mb-1.5">
                      Rating Kepuasan
                    </label>
                    <div className="flex items-center gap-1.5">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onMouseEnter={() => setHoverRating(star)}
                          onMouseLeave={() => setHoverRating(null)}
                          onClick={() => setRating(star)}
                          className="p-1 focus:outline-none transition-transform hover:scale-125 cursor-pointer"
                        >
                          <svg
                            className={`size-6 ${
                              (hoverRating !== null ? star <= hoverRating : star <= rating)
                                ? "text-amber-400 fill-current"
                                : "text-slate-300 dark:text-slate-600"
                            }`}
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                          </svg>
                        </button>
                      ))}
                      <span className="ml-2 text-xs font-bold text-[#64748B] dark:text-slate-300">
                        {rating} dari 5 Bintang
                      </span>
                    </div>
                  </div>

                  {/* Kategori Organisasi */}
                  <div>
                    <label htmlFor="user-cat" className="block text-xs font-bold text-[#0B1E36] dark:text-white mb-1">
                      Kategori Organisasi
                    </label>
                    <select
                      id="user-cat"
                      value={categoryKey}
                      onChange={(e) => setCategoryKey(e.target.value as TestimonialItem["categoryKey"])}
                      className="w-full rounded-xl border border-slate-200 dark:border-[#1E3E6B] bg-[#F8FAFC] dark:bg-[#07152B] px-3.5 py-2 text-xs text-[#0B1E36] dark:text-white focus:bg-white dark:focus:bg-[#081B36] focus:border-[#0099FF] focus:outline-none focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/40 transition-all"
                    >
                      <option value="bem">BEM / DEMA / Senat Mahasiswa</option>
                      <option value="osis">OSIS / MPK Sekolah</option>
                      <option value="ukm">UKM / Unit Seni & Olahraga</option>
                      <option value="himpunan">Himpunan Jurusan / Program Studi</option>
                      <option value="pramuka">Pramuka / Paskibra / PMR</option>
                      <option value="komunitas">Komunitas / Relawan / Publik</option>
                    </select>
                  </div>

                  {/* Form Row: Nama & Organisasi */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                    <div>
                      <label htmlFor="user-name" className="block text-xs font-bold text-[#0B1E36] dark:text-white mb-1">
                        Nama Lengkap <span className="text-rose-500">*</span>
                      </label>
                      <input
                        id="user-name"
                        type="text"
                        required
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="Contoh: Rian Pratama"
                        className="w-full rounded-xl border border-slate-200 dark:border-[#1E3E6B] bg-[#F8FAFC] dark:bg-[#07152B] px-3.5 py-2 text-xs text-[#0B1E36] dark:text-white placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:bg-white dark:focus:bg-[#081B36] focus:border-[#0099FF] focus:outline-none focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/40 transition-all"
                      />
                    </div>
                    <div>
                      <label htmlFor="user-org" className="block text-xs font-bold text-[#0B1E36] dark:text-white mb-1">
                        Nama Organisasi <span className="text-rose-500">*</span>
                      </label>
                      <input
                        id="user-org"
                        type="text"
                        required
                        value={org}
                        onChange={(e) => setOrg(e.target.value)}
                        placeholder="Contoh: BEM FT / OSIS SMA 1"
                        className="w-full rounded-xl border border-slate-200 dark:border-[#1E3E6B] bg-[#F8FAFC] dark:bg-[#07152B] px-3.5 py-2 text-xs text-[#0B1E36] dark:text-white placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:bg-white dark:focus:bg-[#081B36] focus:border-[#0099FF] focus:outline-none focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/40 transition-all"
                      />
                    </div>
                  </div>

                  {/* Peran/Jabatan */}
                  <div>
                    <label htmlFor="user-role" className="block text-xs font-bold text-[#0B1E36] dark:text-white mb-1">
                      Jabatan / Peran di Organisasi (Opsional)
                    </label>
                    <input
                      id="user-role"
                      type="text"
                      value={role}
                      onChange={(e) => setRole(e.target.value)}
                      placeholder="Contoh: Ketua / Divisi Humas / Anggota"
                      className="w-full rounded-xl border border-slate-200 dark:border-[#1E3E6B] bg-[#F8FAFC] dark:bg-[#07152B] px-3.5 py-2 text-xs text-[#0B1E36] dark:text-white placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:bg-white dark:focus:bg-[#081B36] focus:border-[#0099FF] focus:outline-none focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/40 transition-all"
                    />
                  </div>

                  {/* Isi Testimoni */}
                  <div>
                    <label htmlFor="user-content" className="block text-xs font-bold text-[#0B1E36] dark:text-white mb-1">
                      Testimoni / Ulasan Kamu <span className="text-rose-500">*</span>
                    </label>
                    <textarea
                      id="user-content"
                      required
                      rows={3}
                      value={content}
                      onChange={(e) => setContent(e.target.value)}
                      placeholder="Ceritakan bagaimana OrgSpace membantu pembuatan atau pengelolaan website organisasimu..."
                      className="w-full rounded-xl border border-slate-200 dark:border-[#1E3E6B] bg-[#F8FAFC] dark:bg-[#07152B] p-3 text-xs text-[#0B1E36] dark:text-white placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:bg-white dark:focus:bg-[#081B36] focus:border-[#0099FF] focus:outline-none focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/40 transition-all resize-none"
                    />
                  </div>

                  {/* Action Buttons */}
                  <div className="mt-5 pt-3 flex items-center justify-end gap-2.5">
                    <button
                      type="button"
                      onClick={() => setIsModalOpen(false)}
                      className="rounded-xl px-4 py-2.5 text-xs font-semibold text-[#64748B] dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                    >
                      Batal
                    </button>
                    <button
                      type="submit"
                      className="inline-flex items-center gap-1.5 rounded-xl bg-[#0099FF] px-5 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-[#0084FF] hover:shadow-md transition-all cursor-pointer"
                    >
                      <span>Kirim Testimoni</span>
                      <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                        <path
                          fillRule="evenodd"
                          d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
                          clipRule="evenodd"
                        />
                      </svg>
                    </button>
                  </div>
                </form>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
