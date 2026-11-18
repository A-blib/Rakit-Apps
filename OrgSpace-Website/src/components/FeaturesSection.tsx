"use client";

interface FeatureItem {
  id: string;
  badge: string;
  title: string;
  desc: string;
  iconBg: string;
  iconColor: string;
  borderColor: string;
  icon: React.ReactNode;
}

const FEATURES: FeatureItem[] = [
  {
    id: "mobile-first",
    badge: "Mobile-First",
    title: "Kelola Praktis dari Smartphone",
    desc: "Update agenda, unggah dokumentasi kegiatan, dan publikasikan artikel langsung dari browser HP Anda tanpa perlu membuka laptop.",
    iconBg: "bg-blue-50 dark:bg-blue-950/40 border-blue-200/80 dark:border-blue-800/60",
    iconColor: "text-[#0084FF] dark:text-[#00C9F2]",
    borderColor: "hover:border-[#0084FF]/50 dark:hover:border-[#00C9F2]/50 hover:shadow-blue-500/10",
    icon: (
      <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="5" y="2" width="14" height="20" rx="3" />
        <line x1="12" y1="18" x2="12.01" y2="18" strokeWidth="3" />
        <line x1="9" y1="5" x2="15" y2="5" />
      </svg>
    ),
  },
  {
    id: "regeneration",
    badge: "Regenerasi Pengurus",
    title: "Estafet Kepengurusan Anti-Ribet",
    desc: "Ganti pengurus setiap tahun tanpa takut website hilang. Cukup undang admin baru melalui email, seluruh arsip dan web tetap aman berlanjut.",
    iconBg: "bg-indigo-50 dark:bg-indigo-950/40 border-indigo-200/80 dark:border-indigo-800/60",
    iconColor: "text-indigo-600 dark:text-indigo-400",
    borderColor: "hover:border-indigo-400/50 dark:hover:border-indigo-400/50 hover:shadow-indigo-500/10",
    icon: (
      <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
    ),
  },
  {
    id: "domain",
    badge: "Domain Fleksibel",
    title: "Subdomain Instan & Custom Domain",
    desc: "Website langsung aktif di alamat orgspace.app/organisasimu secara gratis, atau hubungkan domain resmi seperti .sch.id, .or.id, maupun .com.",
    iconBg: "bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200/80 dark:border-emerald-800/60",
    iconColor: "text-emerald-600 dark:text-emerald-400",
    borderColor: "hover:border-emerald-400/50 dark:hover:border-emerald-400/50 hover:shadow-emerald-500/10",
    icon: (
      <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10" />
        <line x1="2" y1="12" x2="22" y2="12" />
        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
      </svg>
    ),
  },
  {
    id: "modules",
    badge: "Modul Lengkap",
    title: "Modul Spesifik Dunia Organisasi",
    desc: "Dilengkapi modul siap pakai: struktur bagan kepengurusan, timeline proker, etalase karya, hingga form pendaftaran open recruitment anggota.",
    iconBg: "bg-amber-50 dark:bg-amber-950/40 border-amber-200/80 dark:border-amber-800/60",
    iconColor: "text-amber-600 dark:text-amber-400",
    borderColor: "hover:border-amber-400/50 dark:hover:border-amber-400/50 hover:shadow-amber-500/10",
    icon: (
      <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="12 2 2 7 12 12 22 7 12 2" />
        <polyline points="2 17 12 22 22 17" />
        <polyline points="2 12 12 17 22 12" />
      </svg>
    ),
  },
  {
    id: "security",
    badge: "Aman & Cepat",
    title: "Keamanan Tingkat Tinggi & SSL Otomatis",
    desc: "Enkripsi HTTPS aktif otomatis tanpa biaya tahunan, perlindungan serangan DDoS, dan infrastruktur cloud modern yang selalu cepat diakses.",
    iconBg: "bg-rose-50 dark:bg-rose-950/40 border-rose-200/80 dark:border-rose-800/60",
    iconColor: "text-rose-600 dark:text-rose-400",
    borderColor: "hover:border-rose-400/50 dark:hover:border-rose-400/50 hover:shadow-rose-500/10",
    icon: (
      <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <polyline points="9 12 11 14 15 10" />
      </svg>
    ),
  },
  {
    id: "analytics",
    badge: "Data Transparan",
    title: "Statistik Pengunjung Real-Time",
    desc: "Pantau seberapa banyak anggota dan masyarakat umum yang membaca berita, melihat agenda, dan mengunjungi profil resmi organisasimu.",
    iconBg: "bg-purple-50 dark:bg-purple-950/40 border-purple-200/80 dark:border-purple-800/60",
    iconColor: "text-purple-600 dark:text-purple-400",
    borderColor: "hover:border-purple-400/50 dark:hover:border-purple-400/50 hover:shadow-purple-500/10",
    icon: (
      <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="18" y1="20" x2="18" y2="10" />
        <line x1="12" y1="20" x2="12" y2="4" />
        <line x1="6" y1="20" x2="6" y2="14" />
      </svg>
    ),
  },
];

export function FeaturesSection() {
  return (
    <section id="fitur" className="relative w-full overflow-hidden bg-white dark:bg-[#07152B] text-[#0B1E36] dark:text-white py-16 sm:py-24 scroll-mt-20 transition-colors duration-300">
      <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 rounded-full border border-blue-200/90 dark:border-blue-800/80 bg-blue-50/80 dark:bg-blue-950/60 px-4 py-1.5 text-xs font-bold text-[#0084FF] dark:text-[#00C9F2] shadow-xs mb-3.5">
            <span className="size-1.5 rounded-full bg-[#0084FF] dark:bg-[#00C9F2] animate-pulse" />
            Fitur & Keunggulan OrgSpace
          </div>

          <h2 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight text-[#0B1E36] dark:text-white leading-[1.15]">
            Dirancang Khusus untuk Kebutuhan Organisasi Modern
          </h2>

          <p className="mt-4 text-base sm:text-lg text-[#64748B] dark:text-slate-300 leading-relaxed">
            Semua instrumen digital yang Anda perlukan untuk menjaga profil OSIS, BEM, UKM, atau komunitas tetap aktif, kredibel, dan mudah dioperasikan.
          </p>
        </div>

        {/* Feature Grid */}
        <div className="mt-12 sm:mt-16 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 sm:gap-7">
          {FEATURES.map((feature) => (
            <div
              key={feature.id}
              className={`group relative flex flex-col justify-between rounded-3xl border border-slate-200/90 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] p-6 sm:p-7 shadow-xs transition-all duration-300 hover:-translate-y-1 hover:shadow-lg ${feature.borderColor}`}
            >
              <div>
                {/* Top Badge & Icon */}
                <div className="flex items-center justify-between mb-5">
                  <div
                    className={`flex h-12 w-12 items-center justify-center rounded-2xl border ${feature.iconBg} ${feature.iconColor} shadow-2xs group-hover:scale-110 transition-transform duration-200`}
                  >
                    {feature.icon}
                  </div>
                  <span className="rounded-full bg-slate-50 dark:bg-slate-800/80 border border-slate-200/80 dark:border-slate-700/80 px-3 py-1 text-[11px] font-bold text-[#475569] dark:text-slate-300">
                    {feature.badge}
                  </span>
                </div>

                {/* Title & Description */}
                <h3 className="text-lg sm:text-xl font-bold text-[#0B1E36] dark:text-white group-hover:text-[#0099FF] dark:group-hover:text-[#00C9F2] transition-colors leading-snug">
                  {feature.title}
                </h3>
                <p className="mt-2.5 text-sm text-[#64748B] dark:text-slate-300 leading-relaxed">
                  {feature.desc}
                </p>
              </div>

              {/* Bottom Subtle Indicator */}
              <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-700/60 flex items-center text-xs font-semibold text-[#0099FF] dark:text-[#00C9F2] opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                <span>Siap digunakan langsung</span>
                <svg className="ml-1.5 size-3.5 transition-transform group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clipRule="evenodd" />
                </svg>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
