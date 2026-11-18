"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { ALL_TESTIMONIALS, type TestimonialItem } from "@/lib/testimonials-data";

interface HighlightPoint {
  value: string;
  label: string;
  desc: string;
  badge: string;
  iconBg: string;
  iconColor: string;
  borderColor: string;
  icon: React.ReactNode;
}

const HIGHLIGHT_POINTS: HighlightPoint[] = [
  {
    value: "100% Gratis",
    label: "Versi Awal Bebas Biaya",
    desc: "Seluruh fitur utama bisa dinikmati tanpa sewa server atau komitmen kartu kredit.",
    badge: "Versi Awal",
    iconBg: "bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200/80 dark:border-emerald-800/60",
    iconColor: "text-emerald-600 dark:text-emerald-400",
    borderColor: "hover:border-emerald-300 dark:hover:border-emerald-500/50 hover:shadow-emerald-500/10",
    icon: (
      <svg className="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 12v10H4V12" />
        <path d="M2 7h20v5H2z" />
        <path d="M12 22V7" />
        <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
        <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
      </svg>
    ),
  },
  {
    value: "< 5 Menit",
    label: "Waktu Setup Kilat",
    desc: "Pilih template yang cocok, isi nama organisasi, dan website langsung online.",
    badge: "Super Cepat",
    iconBg: "bg-blue-50 dark:bg-blue-950/40 border-blue-200/80 dark:border-blue-800/60",
    iconColor: "text-[#0084FF] dark:text-[#00C9F2]",
    borderColor: "hover:border-blue-300 dark:hover:border-blue-500/50 hover:shadow-blue-500/10",
    icon: (
      <svg className="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
      </svg>
    ),
  },
  {
    value: "Lewat HP",
    label: "Bisa Diedit Fleksibel",
    desc: "Update program kerja, berita, dan jadwal kegiatan langsung dari browser ponselmu.",
    badge: "Mobile-First",
    iconBg: "bg-purple-50 dark:bg-purple-950/40 border-purple-200/80 dark:border-purple-800/60",
    iconColor: "text-purple-600 dark:text-purple-400",
    borderColor: "hover:border-purple-300 dark:hover:border-purple-500/50 hover:shadow-purple-500/10",
    icon: (
      <svg className="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="5" y="2" width="14" height="20" rx="3" />
        <line x1="12" y1="18" x2="12.01" y2="18" strokeWidth="2.5" />
      </svg>
    ),
  },
  {
    value: "Tanpa Koding",
    label: "Siapapun Bisa Pakai",
    desc: "Tampilan visual yang mudah dipahami, tidak perlu keahlian IT atau koding rumit.",
    badge: "No-Code",
    iconBg: "bg-amber-50 dark:bg-amber-950/40 border-amber-200/80 dark:border-amber-800/60",
    iconColor: "text-amber-600 dark:text-amber-400",
    borderColor: "hover:border-amber-300 dark:hover:border-amber-500/50 hover:shadow-amber-500/10",
    icon: (
      <svg className="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="16 18 22 12 16 6" />
        <polyline points="8 6 2 12 8 18" />
        <line x1="14" y1="4" x2="10" y2="20" />
      </svg>
    ),
  },
];

export function TestimonialsSection() {
  const [userTestimonials, setUserTestimonials] = useState<TestimonialItem[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Form states
  const [name, setName] = useState("");
  const [org, setOrg] = useState("");
  const [role, setRole] = useState("");
  const [content, setContent] = useState("");
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
      categoryKey: "komunitas",
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
  // Landing page HANYA menampilkan 6 kartu testimoni
  const visibleTestimonials = allTestimonials.slice(0, 6);

  return (
    <section id="testimoni" className="relative w-full overflow-hidden bg-white dark:bg-[#07152B] text-[#0B1E36] dark:text-white py-16 sm:py-24 scroll-mt-20 transition-colors duration-300">
      <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 rounded-full border border-purple-200/90 dark:border-purple-800/80 bg-purple-50/80 dark:bg-purple-950/60 px-4 py-1.5 text-xs font-bold text-purple-700 dark:text-purple-300 shadow-xs mb-3.5">
            <span className="size-1.5 rounded-full bg-purple-500 animate-pulse" />
            Ulasan & Pengalaman Nyata
          </div>

          <h2 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight text-[#0B1E36] dark:text-white leading-[1.15]">
            Kata Pelanggan Kami
          </h2>

          <p className="mt-4 text-base sm:text-lg text-[#64748B] dark:text-slate-300 leading-relaxed">
            Cerita dan pengalaman langsung dari rekan-rekan pengurus yang telah mencoba kemudahan membuat website organisasi dengan OrgSpace.
          </p>

          {/* Tombol Tulis Testimoni Utama */}
          <div className="mt-6 flex justify-center">
            <button
              type="button"
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center gap-2.5 rounded-full border border-[#0099FF] bg-blue-50/70 dark:bg-blue-950/40 px-6 py-2.5 text-xs sm:text-sm font-bold text-[#0070BA] dark:text-[#00C9F2] shadow-xs hover:bg-[#0099FF] hover:text-white hover:shadow-md hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer"
            >
              <svg className="size-4 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
              <span>Tulis Testimoni Kamu</span>
            </button>
          </div>
        </div>

        {/* Testimonials Cards Grid (Pasti 6 item di Landing Page) */}
        <div className="mt-12 sm:mt-16 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 sm:gap-8 items-stretch">
          {visibleTestimonials.map((item) => (
            <div
              key={item.id}
              className={`flex flex-col justify-between rounded-3xl border bg-white dark:bg-[#0C2E5B] p-7 sm:p-8 shadow-xs transition-all duration-300 hover:-translate-y-1 hover:shadow-lg ${
                item.isUserAdded
                  ? "border-emerald-300 dark:border-emerald-500 ring-2 ring-emerald-400/20"
                  : "border-slate-200/90 dark:border-[#1E3E6B] hover:border-purple-200 dark:hover:border-purple-500/50"
              }`}
            >
              <div>
                {/* 5 Stars Rating & Tag */}
                <div className="flex items-center justify-between gap-2 mb-5">
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

                {/* Quote Content */}
                <p className="text-sm sm:text-[15px] text-[#334155] dark:text-slate-200 leading-relaxed italic">
                  &ldquo;{item.content}&rdquo;
                </p>
              </div>

              {/* Author Info */}
              <div className="mt-7 pt-5 border-t border-slate-100 dark:border-slate-700/60 flex items-center gap-3.5">
                <div
                  className={`flex size-11 shrink-0 items-center justify-center rounded-2xl font-bold text-sm shadow-xs ${item.avatarBg}`}
                >
                  {item.avatarText}
                </div>
                <div className="min-w-0">
                  <h4 className="text-sm font-bold text-[#0B1E36] dark:text-white truncate">{item.name}</h4>
                  <p className="text-xs text-[#64748B] dark:text-slate-300 truncate">{item.role}</p>
                  <p className="text-[11px] font-semibold text-[#0099FF] dark:text-[#00C9F2] truncate">{item.org}</p>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Tombol Show More: Buka di Tab Baru ke Halaman /testimoni */}
        <div className="mt-10 sm:mt-12 flex flex-col items-center justify-center gap-3">
          <Link
            href="/testimoni"
            target="_blank"
            rel="noopener noreferrer"
            className="group inline-flex items-center gap-2.5 rounded-full border border-slate-300 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] px-8 py-3.5 text-xs sm:text-sm font-bold text-[#0B1E36] dark:text-white shadow-xs hover:border-[#0099FF] dark:hover:border-[#00C9F2] hover:bg-slate-50 dark:hover:bg-[#102C52] hover:text-[#0099FF] dark:hover:text-[#00C9F2] hover:shadow-md hover:scale-105 active:scale-95 transition-all duration-200"
            aria-label="Buka semua testimoni pelanggan di tab baru"
          >
            <span>Lihat Semua Testimoni ({allTestimonials.length} Ulasan)</span>
            <svg
              className="size-4 text-[#0099FF] dark:text-[#00C9F2] transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M5.22 14.78a.75.75 0 001.06 0l7.22-7.22v5.69a.75.75 0 001.5 0v-7.5a.75.75 0 00-.75-.75h-7.5a.75.75 0 000 1.5h5.69l-7.22 7.22a.75.75 0 000 1.06z"
                clipRule="evenodd"
              />
            </svg>
          </Link>
          <span className="text-[11px] text-[#94A3B8] dark:text-slate-400 font-medium flex items-center gap-1">
            <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4.25 2A2.25 2.25 0 002 4.25v11.5A2.25 2.25 0 004.25 18h11.5A2.25 2.25 0 0018 15.75V4.25A2.25 2.25 0 0015.75 2H4.25zm4.03 6.28a.75.75 0 00-1.06-1.06L4.97 9.47a.75.75 0 000 1.06l2.25 2.25a.75.75 0 001.06-1.06L6.56 10l1.72-1.72zm3.44-1.06a.75.75 0 111.06-1.06l2.25 2.25a.75.75 0 010 1.06l-2.25 2.25a.75.75 0 11-1.06-1.06L13.44 10l-1.72-1.72z" clipRule="evenodd" />
            </svg>
            Membuka halaman lengkap di tab baru
          </span>
        </div>

        {/* Highlight Points Cards Strip */}
        <div className="mt-16 sm:mt-20">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
            {HIGHLIGHT_POINTS.map((stat) => (
              <div
                key={stat.label}
                className={`group flex flex-col justify-between rounded-3xl border border-slate-200/90 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] p-6 shadow-xs transition-all duration-300 hover:-translate-y-1.5 hover:shadow-lg ${stat.borderColor}`}
              >
                <div>
                  {/* Top Bar with Icon & Badge */}
                  <div className="flex items-center justify-between gap-2 mb-4">
                    <div
                      className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border ${stat.iconBg} ${stat.iconColor} shadow-2xs group-hover:scale-105 transition-transform duration-200`}
                    >
                      {stat.icon}
                    </div>
                    <span className="rounded-full bg-slate-50 dark:bg-slate-800/80 border border-slate-200/80 dark:border-slate-700/80 px-2.5 py-0.5 text-[10px] font-bold text-[#475569] dark:text-slate-300">
                      {stat.badge}
                    </span>
                  </div>

                  {/* Big Bold Stat Value */}
                  <p className="text-2xl sm:text-[28px] font-black tracking-tight text-[#0B1E36] dark:text-white leading-tight">
                    {stat.value}
                  </p>

                  {/* Label / Feature Title */}
                  <h4 className="mt-1.5 text-sm font-bold text-[#0B1E36] dark:text-white">
                    {stat.label}
                  </h4>

                  {/* Supporting Description */}
                  <p className="mt-2 text-xs text-[#64748B] dark:text-slate-300 leading-relaxed">
                    {stat.desc}
                  </p>
                </div>
              </div>
            ))}
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
                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </button>

            {submitted ? (
              <div className="py-8 text-center">
                <div className="mx-auto flex size-14 items-center justify-center rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 mb-4">
                  <svg className="size-7" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
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
                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
                      </svg>
                    </button>
                  </div>
                </form>
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
}
