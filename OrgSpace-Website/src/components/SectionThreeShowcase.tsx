"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import Link from "next/link";

/**
 * 3 Keunggulan Utama di Bagian Bawah Kolom Kiri
 * Masing-masing dilengkapi palet warna yang saling mendukung & selaras dengan brand
 */
const BOTTOM_FEATURES = [
  {
    title: "Mudah Digunakan",
    desc: "Cukup dengan klik dan atur, website langsung bisa kamu miliki.",
    tag: "Praktis",
    tagStyle: "bg-orange-50 text-orange-700 border-orange-200/80",
    iconContainerStyle: "bg-gradient-to-br from-orange-500/15 via-amber-500/10 to-orange-500/20 border-orange-200/80 text-orange-600 shadow-sm shadow-orange-500/15 group-hover:border-orange-300 group-hover:shadow-orange-500/30 group-hover:scale-105",
    cardBorderHover: "hover:border-orange-200 hover:shadow-orange-500/5",
    icon: (
      <svg className="h-5 w-5 sm:h-5.5 sm:w-5.5 text-orange-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M15 15l5 5m-5-5l-3.5-3.5a2 2 0 0 0-2.8 0l-5 5a2 2 0 0 0 0 2.8l3.5 3.5" />
        <path d="M9 3v4" />
        <path d="M3 9h4" />
        <path d="M14 6l-2 2" />
        <circle cx="12" cy="12" r="3" fill="#FF7700" fillOpacity="0.25" />
      </svg>
    ),
  },
  {
    title: "Bisa di Semua Perangkat",
    desc: "Kelola website kapan saja dan di mana saja, lewat HP atau laptop.",
    tag: "Responsif",
    tagStyle: "bg-blue-50 text-[#0070E0] border-blue-200/80",
    iconContainerStyle: "bg-gradient-to-br from-blue-500/15 via-sky-500/10 to-blue-500/20 border-blue-200/80 text-[#0084FF] shadow-sm shadow-blue-500/15 group-hover:border-blue-300 group-hover:shadow-blue-500/30 group-hover:scale-105",
    cardBorderHover: "hover:border-blue-200 hover:shadow-blue-500/5",
    icon: (
      <svg className="h-5 w-5 sm:h-5.5 sm:w-5.5 text-[#0084FF]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="4" y="2" width="16" height="20" rx="3" />
        <circle cx="12" cy="18" r="1.2" fill="#0084FF" />
        <line x1="9" y1="5" x2="15" y2="5" />
      </svg>
    ),
  },
  {
    title: "Aman & Terpercaya",
    desc: "Dilengkapi dengan sistem keamanan untuk melindungi website kamu.",
    tag: "Proteksi SSL",
    tagStyle: "bg-emerald-50 text-emerald-700 border-emerald-200/80",
    iconContainerStyle: "bg-gradient-to-br from-emerald-500/15 via-teal-500/10 to-emerald-500/20 border-emerald-200/80 text-emerald-600 shadow-sm shadow-emerald-500/15 group-hover:border-emerald-300 group-hover:shadow-emerald-500/30 group-hover:scale-105",
    cardBorderHover: "hover:border-emerald-200 hover:shadow-emerald-500/5",
    icon: (
      <svg className="h-5 w-5 sm:h-5.5 sm:w-5.5 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <polyline points="9 12 11 14 15 10" stroke="#059669" strokeWidth="2.4" />
      </svg>
    ),
  },
];

/**
 * Tipe Tahap Layar di Dalam HP:
 * 1. "splash-ring": Logo pembuka awal (Gambar Pertama: Cincin O biru + orbit kuning emas)
 * 2. "splash-orgspace": Gambar Kedua (Kartu OrgSpace + "Ruang Kerja & Website Organisasi" + "Menyiapkan ruang kerjamu...")
 * 3. "login": Form login interaktif dengan simulasi pengetikan email & kata sandi otomatis
 * 4. "video": Setelah login, LANGSUNG masuk ke menu-menu aplikasi (tanpa logo lagi) & memutar segmen penting tanpa ketikan keyboard
 */
type ScreenStep = "splash-ring" | "splash-orgspace" | "login" | "video";

/**
 * Segmen-Segmen Penting Terkurasi dari Video:
 * BAGIAN MENGHAPUS TEKS (detik 28s - 38s) & MENGETIK KEYBOARD (detik 39s - 89s) TELAH DIHILANGKAN TOTAL!
 * 1. Dashboard (8s - 20s): Antarmuka menu utama "My Organisasi", status web aktif & navigasi
 * 2. Template (20s - 26s): Menu pemilihan template website (Creative Crew, Sinergi, dll)
 * 3. Preview Desain (90s - 96s): Tampilan live preview website yang sudah rapi & selesai (bebas hapus/ketik)
 * 4. Website Live (120s - 140s): Tampilan website publik yang sudah live di hosting & drawer menu terbuka
 */
const HIGHLIGHT_SEGMENTS = [
  { id: "dashboard", label: "Dashboard", start: 8, end: 20 },
  { id: "templates", label: "Template", start: 20, end: 26 },
  { id: "preview", label: "Preview Desain", start: 90, end: 96 },
  { id: "live", label: "Website Live", start: 120, end: 140 },
];

export function SectionThreeShowcase() {
  const sectionRef = useRef<HTMLElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const rightColRef = useRef<HTMLDivElement>(null);
  const videoRef = useRef<HTMLVideoElement>(null);

  // Tahap pergerakan fisik HP: "idle" | "center" | "right"
  const [stage, setStage] = useState<"idle" | "center" | "right">("idle");
  const [centerOffset, setCenterOffset] = useState<number>(0);
  const [isTextVisible, setIsTextVisible] = useState(false);
  const [isMuted, setIsMuted] = useState(true);

  // Tahap tampilan layar di dalam smartphone (dimulai dari Gambar Pertama: Cincin O)
  const [screenStep, setScreenStep] = useState<ScreenStep>("splash-ring");

  // State simulasi form login
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [activeField, setActiveField] = useState<"none" | "email" | "password">("none");
  const [loginStatus, setLoginStatus] = useState<"idle" | "typing-email" | "typing-password" | "submitting" | "success">("idle");
  const [loadingProgress, setLoadingProgress] = useState(20);

  // State segmen video terkurasi
  const [activeSegmentIndex, setActiveSegmentIndex] = useState(0);

  // Array untuk menyimpan referensi timer agar pembersihan memori aman
  const timersRef = useRef<NodeJS.Timeout[]>([]);

  const clearAllTimers = useCallback(() => {
    timersRef.current.forEach((t) => clearTimeout(t));
    timersRef.current = [];
  }, []);

  // Hitung offset horizontal presisi agar HP berada persis di tengah section saat fase intro
  useEffect(() => {
    function calculateCenterOffset() {
      if (containerRef.current && rightColRef.current) {
        const containerRect = containerRef.current.getBoundingClientRect();
        const rightColRect = rightColRef.current.getBoundingClientRect();
        const cCenter = containerRect.left + containerRect.width / 2;
        const rCenter = rightColRect.left + rightColRect.width / 2;
        setCenterOffset(Math.round(cCenter - rCenter));
      }
    }

    calculateCenterOffset();
    window.addEventListener("resize", calculateCenterOffset);
    return () => window.removeEventListener("resize", calculateCenterOffset);
  }, []);

  /**
   * Menjalankan seluruh koreografi pembuka secara berurutan:
   * 1. Gambar Pertama (Cincin O Biru + Orbit Kuning)
   * 2. Gambar Kedua (Kartu OrgSpace + "Menyiapkan ruang kerjamu...")
   * 3. Form Login Otomatis Mengisi
   * 4. Setelah Login: HP Meluncur ke Kanan & LANGSUNG Masuk ke Menu Aplikasi pada Detik ke-8
   */
  const playInteractiveSequence = useCallback(() => {
    clearAllTimers();

    // 1. Awal: HP muncul di tengah, menampilkan Gambar Pertama (Cincin O Biru)
    setStage("center");
    setScreenStep("splash-ring");
    setLoginEmail("");
    setLoginPassword("");
    setActiveField("none");
    setLoginStatus("idle");
    setLoadingProgress(20);
    setIsTextVisible(false);
    setActiveSegmentIndex(0);

    // 2. T = 2200ms: Jeda selesai, lalu muncul Gambar Kedua (Kartu OrgSpace + Loading bar)
    const tSecond = setTimeout(() => {
      setScreenStep("splash-orgspace");
    }, 2200);
    timersRef.current.push(tSecond);

    // Animasi progress bar pada Gambar Kedua
    const p1 = setTimeout(() => setLoadingProgress(65), 2600);
    const p2 = setTimeout(() => setLoadingProgress(100), 3400);
    timersRef.current.push(p1, p2);

    // 3. T = 4200ms: Muncul Form Login OrgSpace
    const tLogin = setTimeout(() => {
      setScreenStep("login");
      setActiveField("email");
      setLoginStatus("typing-email");
    }, 4200);
    timersRef.current.push(tLogin);

    // 4. T = 4600ms: Ketik email secara otomatis
    const emailTarget = "admin@orgspace.com";
    for (let i = 1; i <= emailTarget.length; i++) {
      const tChar = setTimeout(() => {
        setLoginEmail(emailTarget.slice(0, i));
      }, 4600 + i * 50);
      timersRef.current.push(tChar);
    }

    // 5. T = 5700ms: Pindah ke field Kata Sandi
    const tSwitchPass = setTimeout(() => {
      setActiveField("password");
      setLoginStatus("typing-password");
    }, 5700);
    timersRef.current.push(tSwitchPass);

    const passTarget = "••••••••••••";
    for (let i = 1; i <= passTarget.length; i++) {
      const tPass = setTimeout(() => {
        setLoginPassword(passTarget.slice(0, i));
      }, 5900 + i * 60);
      timersRef.current.push(tPass);
    }

    // 6. T = 6900ms: Tombol Masuk tertekan (Submitting / memverifikasi)
    const tSubmit = setTimeout(() => {
      setActiveField("none");
      setLoginStatus("submitting");
    }, 6900);
    timersRef.current.push(tSubmit);

    // 7. T = 7500ms: Login Sukses (Berhasil Masuk ✓)
    const tSuccess = setTimeout(() => {
      setLoginStatus("success");
    }, 7500);
    timersRef.current.push(tSuccess);

    // 8. T = 8100ms: HP berpindah posisi meluncur mulus ke sebelah kanan
    const tMoveRight = setTimeout(() => {
      setStage("right");
    }, 8100);
    timersRef.current.push(tMoveRight);

    // 9. T = 8500ms: Teks di sebelah kiri mulai bermunculan satu per satu
    const tText = setTimeout(() => {
      setIsTextVisible(true);
    }, 8500);
    timersRef.current.push(tText);

    // 10. T = 8800ms: LANGSUNG MASUK KE MENU-MENU PADA DETIK KE-8 (TANPA LOGO & TANPA MENGETIK)
    const tVideo = setTimeout(() => {
      setScreenStep("video");
      setActiveSegmentIndex(0);
      if (videoRef.current) {
        // Langsung lompat ke detik ke-8 (Dashboard Menu "My Organisasi")
        videoRef.current.currentTime = HIGHLIGHT_SEGMENTS[0].start;
        videoRef.current.play().catch(() => {});
      }
    }, 8800);
    timersRef.current.push(tVideo);
  }, [clearAllTimers]);

  // Melompat ke segmen video tertentu saat tab diklik
  const jumpToSegment = (index: number) => {
    setActiveSegmentIndex(index);
    if (videoRef.current) {
      videoRef.current.currentTime = HIGHLIGHT_SEGMENTS[index].start;
      videoRef.current.play().catch(() => {});
    }
  };

  /**
   * Filter Pemutaran Video:
   * HANYA memutar segmen penting. Menghilangkan bagian mengetik keyboard secara total!
   * Begitu mencapai detik 38s (sebelum keyboard muncul), langsung lompat ke detik 126s (hasil jadi website).
   */
  const handleTimeUpdate = () => {
    if (!videoRef.current || screenStep !== "video") return;
    const cur = videoRef.current.currentTime;
    const currentSegment = HIGHLIGHT_SEGMENTS[activeSegmentIndex];

    // Jika video berada di luar batas segmen aktif (atau sebelum detik 8):
    if (cur < currentSegment.start || cur >= currentSegment.end) {
      // Jika telah mencapai batas akhir segmen saat ini, otomatis lanjut ke segmen berikutnya
      if (cur >= currentSegment.end) {
        const nextIndex = (activeSegmentIndex + 1) % HIGHLIGHT_SEGMENTS.length;
        setActiveSegmentIndex(nextIndex);
        videoRef.current.currentTime = HIGHLIGHT_SEGMENTS[nextIndex].start;
      } else {
        videoRef.current.currentTime = currentSegment.start;
      }
    }
  };

  // IntersectionObserver untuk memicu rangkaian animasi saat pengguna menggulir ke Section 3
  useEffect(() => {
    if (!("IntersectionObserver" in window)) {
      requestAnimationFrame(() => {
        setStage("right");
        setIsTextVisible(true);
        setScreenStep("video");
      });
      return;
    }

    const currentEl = sectionRef.current;
    if (!currentEl) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          observer.disconnect();
          playInteractiveSequence();
        }
      },
      {
        threshold: 0.15,
        rootMargin: "0px 0px -40px 0px",
      }
    );

    observer.observe(currentEl);

    return () => {
      observer.disconnect();
      clearAllTimers();
    };
  }, [clearAllTimers, playInteractiveSequence]);

  return (
    <section
      ref={sectionRef}
      id="fitur-showcase"
      className="relative w-full overflow-hidden bg-gradient-to-b from-white via-[#FAFBFD] to-[#F3F7FD] pt-12 sm:pt-16 md:pt-20 pb-20 sm:pb-24 lg:pb-32 transition-colors duration-500"
    >
      {/* Background Glow Halus */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -top-32 right-1/4 h-96 w-96 rounded-full bg-blue-100/40 blur-3xl" />
        <div className="absolute top-1/2 -left-20 h-96 w-96 rounded-full bg-sky-100/30 blur-3xl" />
      </div>

      <div ref={containerRef} className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 sm:gap-12 lg:gap-16 items-center">
          
          {/* =====================================================================
              KOLOM KIRI: TEKS PENJELASAN, KEUNGGULAN & CTA
              (Muncul SATU PERSATU dengan jeda teratur & perlahan)
             ===================================================================== */}
          <div className="lg:col-span-6 xl:col-span-7 text-left">
            {/* 1. Pill Badge */}
            <div
              style={{
                transitionDelay: isTextVisible ? "100ms" : "0ms",
              }}
              className={`inline-flex items-center gap-2 rounded-full border border-[#0084FF]/25 bg-[#EBF5FF] px-4 py-1.5 text-xs sm:text-sm font-bold text-[#0070E0] shadow-xs transition-all duration-800 ease-[cubic-bezier(0.16,1,0.3,1)] ${
                isTextVisible ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-10 pointer-events-none"
              }`}
            >
              <svg className="h-4 w-4 text-[#0084FF]" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L14.5 9.5L22 12L14.5 14.5L12 22L9.5 14.5L2 12L9.5 9.5L12 2Z" />
              </svg>
              <span>SAATNYA MULAI BERKREASI</span>
            </div>

            {/* 2. Headline Utama (Jeda 600ms) */}
            <h2
              style={{
                transitionDelay: isTextVisible ? "600ms" : "0ms",
              }}
              className={`mt-4 sm:mt-5 text-3xl sm:text-4xl md:text-5xl lg:text-[48px] font-black tracking-tight text-[#0B1E36] leading-[1.14] transition-all duration-900 ease-[cubic-bezier(0.16,1,0.3,1)] ${
                isTextVisible ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-12 pointer-events-none"
              }`}
            >
              Ide Website-mu Layak Jadi Nyata.<br />
              <span className="text-[#0084FF]">Yuk, Wujudkan Bersama OrgSpace!</span>
            </h2>

            {/* 3. Deskripsi Pendukung (Jeda 1100ms) */}
            <p
              style={{
                transitionDelay: isTextVisible ? "1100ms" : "0ms",
              }}
              className={`mt-4 sm:mt-5 text-sm sm:text-base md:text-lg text-[#475569] leading-relaxed max-w-xl transition-all duration-900 ease-[cubic-bezier(0.16,1,0.3,1)] ${
                isTextVisible ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-12 pointer-events-none"
              }`}
            >
              Dengan Rakit, kamu bisa membuat website profesional tanpa perlu coding. Pilih desain, atur konten, dan bangun kehadiran online-mu dengan lebih mudah dan cepat.
            </p>

            {/* 4. Tombol Call to Action (Jeda 1600ms) */}
            <div
              style={{
                transitionDelay: isTextVisible ? "1600ms" : "0ms",
              }}
              className={`mt-7 sm:mt-8 flex flex-wrap items-center gap-4 transition-all duration-900 ease-[cubic-bezier(0.16,1,0.3,1)] ${
                isTextVisible ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-10 pointer-events-none"
              }`}
            >
              <a
                href="#akses-awal"
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-[#FF5A00] to-[#FF7700] px-6 sm:px-7 py-3 sm:py-3.5 text-sm sm:text-base font-bold text-white shadow-lg shadow-orange-500/25 hover:shadow-orange-500/40 hover:-translate-y-0.5 hover:from-[#FF6A15] hover:to-[#FF8515] transition-all duration-200"
              >
                <span>Mulai Gratis</span>
                <span className="text-lg">→</span>
              </a>

              <Link
                href="/docs"
                className="inline-flex items-center justify-center rounded-xl border-2 border-[#0B1E36] bg-white px-6 sm:px-7 py-3 sm:py-3.5 text-sm sm:text-base font-bold text-[#0B1E36] hover:bg-[#F8FAFC] hover:border-[#0084FF] hover:text-[#0084FF] transition-all duration-200"
              >
                Lihat Demo
              </Link>
            </div>

            {/* 5. Tiga Poin Keunggulan Utama (Muncul satu persatu dengan palet warna selaras) */}
            <div className="mt-10 pt-7 border-t border-slate-200/80 grid grid-cols-1 sm:grid-cols-3 gap-3.5 sm:gap-4">
              {BOTTOM_FEATURES.map((item, index) => (
                <div
                  key={item.title}
                  style={{
                    transitionDelay: isTextVisible ? `${2100 + index * 450}ms` : "0ms",
                  }}
                  className={`group flex flex-col p-3.5 sm:p-4 rounded-2xl bg-white/70 hover:bg-white border border-slate-200/70 ${item.cardBorderHover} shadow-2xs hover:shadow-md transition-all duration-300 hover:-translate-y-0.5 ${
                    isTextVisible ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-8 pointer-events-none"
                  }`}
                >
                  <div className="flex items-center justify-between mb-3">
                    <div className={`flex h-10 w-10 sm:h-11 sm:w-11 shrink-0 items-center justify-center rounded-xl border transition-all duration-300 ${item.iconContainerStyle}`}>
                      {item.icon}
                    </div>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${item.tagStyle}`}>
                      {item.tag}
                    </span>
                  </div>
                  <div>
                    <h3 className="text-xs sm:text-sm font-bold text-[#0B1E36] group-hover:text-[#0084FF] transition-colors">
                      {item.title}
                    </h3>
                    <p className="mt-1 text-[11px] sm:text-xs text-[#475569] leading-relaxed">
                      {item.desc}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* =====================================================================
              KOLOM KANAN: MOCKUP SMARTPHONE BERISI RANGKAIAN ALUR INTERAKTIF
              (Layar Bersih Bebas Tumpang Tindih + Filter Video Tanpa Keyboard Mengetik)
             ===================================================================== */}
          <div ref={rightColRef} className="lg:col-span-6 xl:col-span-5 flex flex-col items-center relative perspective-[1400px]">
            
            {/* Bayangan Lantai Halus di Bawah HP */}
            <div
              style={{
                transform:
                  stage === "center"
                    ? `translateX(${centerOffset}px) scale(0.95)`
                    : stage === "right"
                    ? "translateX(0px) scale(1)"
                    : `translateX(${centerOffset}px) scale(0.4)`,
                transition:
                  stage === "center"
                    ? "transform 3.2s cubic-bezier(0.16, 1, 0.3, 1), opacity 1.5s ease"
                    : stage === "right"
                    ? "transform 1.5s cubic-bezier(0.16, 1, 0.3, 1), opacity 1s ease"
                    : "none",
              }}
              className={`absolute -bottom-6 w-52 sm:w-68 h-7 bg-black/25 blur-xl rounded-full ${
                stage !== "idle" ? "opacity-100" : "opacity-0"
              }`}
            />

            {/* Badan Mockup Smartphone 3D (Rasio Presisi 9:20 sesuai Video Layar Android) */}
            <div
              style={{
                transform:
                  stage === "idle"
                    ? `translateX(${centerOffset}px) translateY(220px) perspective(1400px) rotateY(-360deg) scale(0.8)`
                    : stage === "center"
                    ? `translateX(${centerOffset}px) translateY(0px) perspective(1400px) rotateY(0deg) scale(1.02)`
                    : "translateX(0px) translateY(0px) perspective(1400px) rotateY(0deg) scale(1)",
                transition:
                  stage === "center"
                    ? "transform 3.2s cubic-bezier(0.16, 1, 0.3, 1), opacity 1.4s ease-out"
                    : stage === "right"
                    ? "transform 1.5s cubic-bezier(0.16, 1, 0.3, 1), opacity 1s ease"
                    : "none",
              }}
              className={`relative w-[255px] sm:w-[275px] md:w-[290px] aspect-[9/20] rounded-[42px] border-[8px] sm:border-[9px] border-[#1E293B] bg-[#07111F] shadow-2xl shadow-blue-950/40 ring-1 ring-slate-700/60 overflow-hidden select-none ${
                stage !== "idle" ? "opacity-100 filter-none" : "opacity-0 blur-xs"
              } ${stage === "right" ? "hover:-translate-y-1 transition-transform" : ""}`}
            >
              {/* Tombol Fisik Smartphone di Samping Luar */}
              <div className="absolute -left-[11px] top-20 w-[3px] h-8 bg-slate-700 rounded-l-sm" />
              <div className="absolute -left-[11px] top-30 w-[3px] h-8 bg-slate-700 rounded-l-sm" />
              <div className="absolute -right-[11px] top-24 w-[3px] h-12 bg-slate-700 rounded-r-sm" />

              {/* Earpiece Ramping di Bezel Atas (Tidak Menutupi Konten/Tab Layar) */}
              <div className="absolute top-1 left-1/2 -translate-x-1/2 z-50 w-10 h-1 bg-slate-700/80 rounded-full pointer-events-none" />

              {/* Lapisan Refleksi Kaca Layar (Glossy Specular Sheen) */}
              <div className="pointer-events-none absolute inset-0 z-40 bg-gradient-to-tr from-transparent via-white/5 to-white/10 rounded-[34px]" />

              {/* ===============================================================
                  1. LAYAR GAMBAR PERTAMA (CINCIN O BIRU BESAR DENGAN ORBIT KUNING)
                  Tampil terpusat secara bersih tanpa tumpang tindih
                 =============================================================== */}
              <div
                className={`absolute inset-0 z-20 flex flex-col items-center justify-center bg-[#07111F] p-6 text-center text-white transition-opacity duration-700 ${
                  screenStep === "splash-ring" ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
                }`}
              >
                {/* Glow Radial Biru di Belakang Cincin */}
                <div className="absolute h-56 w-56 rounded-full bg-[#3B82F6]/25 blur-3xl animate-pulse" />

                {/* Cincin Biru Besar dengan Titik Kuning Mengorbit */}
                <div className="relative flex items-center justify-center">
                  <div className="h-28 w-28 sm:h-32 sm:w-32 rounded-full border-[10px] sm:border-[12px] border-[#3B82F6] shadow-[0_0_40px_rgba(59,130,246,0.5)] flex items-center justify-center relative">
                    <div className="absolute -top-1.5 -right-1.5 h-6 w-6 sm:h-7 sm:w-7 rounded-full bg-[#FFC531] border-2 border-white/60 shadow-lg shadow-yellow-500/50 animate-pulse" />
                  </div>
                </div>
              </div>

              {/* ===============================================================
                  2. LAYAR GAMBAR KEDUA (SPLASH ORGSPACE DENGAN LOGO KARTU BIRU)
                  Posisinya proporsional dan tidak menimpa atas maupun bawah
                 =============================================================== */}
              <div
                className={`absolute inset-0 z-20 flex flex-col items-center justify-center bg-[#07111F] px-6 text-center text-white transition-opacity duration-700 ${
                  screenStep === "splash-orgspace" ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
                }`}
              >
                {/* Aura Cahaya Berpendar Lembut di Belakang Logo */}
                <div className="absolute h-48 w-48 rounded-full bg-[#0B63F6]/30 blur-3xl animate-pulse" />

                {/* Kartu Logo Utama OrgSpace */}
                <div className="relative mb-4 flex h-20 w-20 sm:h-22 sm:w-22 items-center justify-center rounded-[24px] bg-[#0B63F6] shadow-2xl shadow-blue-500/40 border border-white/25">
                  <div className="h-11 w-11 sm:h-12 sm:w-12 rounded-full border-[5px] border-white flex items-center justify-center relative">
                    <span className="absolute -top-1 -right-1 h-3.5 w-3.5 rounded-full bg-[#FFC531] border-2 border-white shadow-xs" />
                  </div>
                </div>

                {/* Teks Judul Brand */}
                <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white drop-shadow-md">
                  OrgSpace
                </h2>

                {/* Tagline Aplikasi */}
                <p className="mt-1 text-xs sm:text-[13px] text-[#9AB0CC] font-normal tracking-wide">
                  Ruang Kerja &amp; Website Organisasi
                </p>

                {/* Bar Loading Ramping Bersih */}
                <div className="mt-8 flex flex-col items-center">
                  <div className="w-24 h-1 bg-[#15243B] rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[#4C9AFF] transition-all duration-700 ease-out rounded-full"
                      style={{ width: `${loadingProgress}%` }}
                    />
                  </div>
                  <span className="mt-2.5 text-[11px] text-[#9AB0CC] font-normal tracking-tight">
                    Menyiapkan ruang kerjamu...
                  </span>
                </div>
              </div>

              {/* ===============================================================
                  3. LAYAR FORM LOGIN (SIMULASI PENGETIKAN OTOMATIS)
                  Memiliki jarak aman atas & bawah agar tidak tertutup punch hole
                 =============================================================== */}
              <div
                className={`absolute inset-0 z-20 flex flex-col justify-center bg-[#07111F] pt-10 pb-6 px-6 text-white transition-opacity duration-700 overflow-hidden ${
                  screenStep === "login" ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
                }`}
              >
                {/* Header Atas Bersih */}
                <div className="flex items-center gap-2 mb-3 text-slate-300">
                  <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="15 18 9 12 15 6" />
                  </svg>
                  <span className="text-xs font-bold text-slate-200">Masuk ke OrgSpace</span>
                </div>

                {/* Judul & Subjudul Form */}
                <div className="text-left mb-2.5">
                  <h3 className="text-base sm:text-lg font-black tracking-tight text-white">
                    Masuk
                  </h3>
                  <p className="text-[10px] text-[#9AB0CC] mt-0.5 leading-snug">
                    Masuk untuk mengelola ruang kerja &amp; websitemu.
                  </p>
                </div>

                {/* Tombol Masuk dengan Google */}
                <div className="w-full py-1.5 px-3 rounded-xl bg-[#0E1B2E] border border-[#22344F] flex items-center justify-center gap-2 shadow-xs cursor-pointer">
                  <svg className="w-3.5 h-3.5 shrink-0" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                  </svg>
                  <span className="text-[11px] font-semibold text-slate-200">
                    Lanjutkan dengan Google
                  </span>
                </div>

                {/* Garis Pembatas */}
                <div className="flex items-center gap-2 my-2 text-[9px] text-[#9AB0CC]">
                  <div className="h-px flex-1 bg-[#22344F]" />
                  <span className="uppercase tracking-wider">atau</span>
                  <div className="h-px flex-1 bg-[#22344F]" />
                </div>

                {/* Field Email */}
                <div className="text-left mb-2">
                  <label className="text-[10px] font-semibold text-[#9AB0CC] mb-0.5 block">
                    Email
                  </label>
                  <div
                    className={`w-full h-8 px-3 rounded-xl bg-[#0E1B2E] border flex items-center text-xs text-white transition-colors ${
                      activeField === "email"
                        ? "border-[#4C9AFF] ring-1 ring-[#4C9AFF]/40"
                        : "border-[#22344F]"
                    }`}
                  >
                    {loginEmail ? (
                      <span className="font-mono text-[11px] text-white">
                        {loginEmail}
                      </span>
                    ) : (
                      <span className="text-slate-500 text-[11px]">nama@email.com</span>
                    )}
                    {activeField === "email" && (
                      <span className="inline-block w-0.5 h-3.5 bg-[#4C9AFF] ml-0.5 animate-pulse" />
                    )}
                  </div>
                </div>

                {/* Field Kata Sandi */}
                <div className="text-left mb-3">
                  <label className="text-[10px] font-semibold text-[#9AB0CC] mb-0.5 block">
                    Kata Sandi
                  </label>
                  <div
                    className={`w-full h-8 px-3 rounded-xl bg-[#0E1B2E] border flex items-center justify-between text-xs text-white transition-colors ${
                      activeField === "password"
                        ? "border-[#4C9AFF] ring-1 ring-[#4C9AFF]/40"
                        : "border-[#22344F]"
                    }`}
                  >
                    <div className="flex items-center">
                      {loginPassword ? (
                        <span className="font-mono text-sm tracking-widest text-white">
                          {loginPassword}
                        </span>
                      ) : (
                        <span className="text-slate-500 text-[11px]">••••••••</span>
                      )}
                      {activeField === "password" && (
                        <span className="inline-block w-0.5 h-3.5 bg-[#4C9AFF] ml-0.5 animate-pulse" />
                      )}
                    </div>
                    <svg className="w-3.5 h-3.5 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </div>
                </div>

                {/* Tombol Aksi Masuk */}
                <div>
                  {loginStatus === "submitting" ? (
                    <div className="w-full h-8.5 rounded-xl bg-[#0B63F6] text-white text-xs font-bold flex items-center justify-center gap-2 shadow-lg shadow-blue-500/30 scale-98 transition-all">
                      <svg className="w-3.5 h-3.5 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                      </svg>
                      <span>Memverifikasi...</span>
                    </div>
                  ) : loginStatus === "success" ? (
                    <div className="w-full h-8.5 rounded-xl bg-emerald-600 text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-lg shadow-emerald-600/30 transition-all">
                      <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                      <span>Berhasil Masuk ✓</span>
                    </div>
                  ) : (
                    <div className="w-full h-8.5 rounded-xl bg-[#0B63F6] text-white text-xs font-bold flex items-center justify-center shadow-md shadow-blue-500/20">
                      <span>Masuk</span>
                    </div>
                  )}
                </div>
              </div>

              {/* ===============================================================
                  4. LAYAR UTAMA: VIDEO DEMO REKAMAN APLIKASI
                  Layar murni video 100% bersih tanpa tombol tumpang tindih di dalam
                 =============================================================== */}
              <div
                className={`relative z-10 w-full h-full overflow-hidden bg-black flex items-center justify-center transition-opacity duration-700 ${
                  screenStep === "video" ? "opacity-100" : "opacity-0 pointer-events-none"
                }`}
              >
                <video
                  ref={videoRef}
                  src="/videos/rakit-app-demo.mp4"
                  autoPlay
                  loop
                  muted={isMuted}
                  playsInline
                  preload="auto"
                  onTimeUpdate={handleTimeUpdate}
                  className="w-full h-full object-fill object-center"
                />
              </div>
            </div>

            {/* ===================================================================
                KONTROL DI LUAR HP: BEBAS TUMPANG TINDIH
                (Tab Pilihan Menu Penting, Kontrol Suara & Putar Ulang)
               =================================================================== */}
            <div className="mt-4 flex flex-col items-center gap-2.5 w-full max-w-sm px-2">
              
              {/* Pilihan 4 Segmen Fitur Penting (Filter Tanpa Mengetik) */}
              {stage === "right" && screenStep === "video" && (
                <div className="flex flex-wrap items-center justify-center gap-1.5">
                  {HIGHLIGHT_SEGMENTS.map((seg, idx) => (
                    <button
                      key={seg.id}
                      type="button"
                      onClick={() => jumpToSegment(idx)}
                      className={`px-3 py-1 rounded-full text-xs font-bold transition-all shadow-xs cursor-pointer ${
                        activeSegmentIndex === idx
                          ? "bg-[#0084FF] text-white shadow-md shadow-blue-500/30 ring-2 ring-blue-300"
                          : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-100 hover:text-[#0B1E36]"
                      }`}
                    >
                      {seg.label}
                    </button>
                  ))}
                </div>
              )}

              {/* Baris Tombol Utilitas (Suara & Putar Ulang) */}
              {stage === "right" && (
                <div className="flex items-center gap-2">
                  {/* Tombol Suara (Mute / Unmute) */}
                  <button
                    type="button"
                    onClick={() => setIsMuted((prev) => !prev)}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-100 transition-all shadow-xs active:scale-95 cursor-pointer"
                    title={isMuted ? "Nyalakan Suara" : "Bisukan Suara"}
                  >
                    {isMuted ? (
                      <>
                        <svg className="h-3.5 w-3.5 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                          <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                          <line x1="23" y1="9" x2="17" y2="15" />
                          <line x1="17" y1="9" x2="23" y2="15" />
                        </svg>
                        <span>Suara Mati</span>
                      </>
                    ) : (
                      <>
                        <svg className="h-3.5 w-3.5 text-blue-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                          <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                          <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07" />
                        </svg>
                        <span className="text-blue-600">Suara Aktif</span>
                      </>
                    )}
                  </button>

                  {/* Tombol Putar Ulang Animasi */}
                  <button
                    type="button"
                    onClick={playInteractiveSequence}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-100 transition-all shadow-xs active:scale-95 cursor-pointer"
                    title="Putar ulang animasi intro & interaksi login"
                  >
                    <svg className="w-3.5 h-3.5 text-blue-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
                      <path d="M3 3v5h5" />
                    </svg>
                    <span>Putar Ulang</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
