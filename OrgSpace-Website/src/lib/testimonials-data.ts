export interface TestimonialItem {
  id: string;
  name: string;
  role: string;
  org: string;
  category: string;
  categoryKey: "bem" | "osis" | "ukm" | "himpunan" | "pramuka" | "komunitas";
  rating: number;
  content: string;
  avatarBg: string;
  avatarText: string;
  date: string;
  isUserAdded?: boolean;
}

export const ALL_TESTIMONIALS: TestimonialItem[] = [
  {
    id: "bem-fik",
    name: "Dimas Arya Pratama",
    role: "Ketua BEM Fakultas Ilmu Komputer",
    org: "Universitas Negeri",
    category: "Estafet Kepengurusan",
    categoryKey: "bem",
    rating: 5,
    date: "14 November 2026",
    content:
      "Kendala klasik tiap pergantian periode kepengurusan adalah website yang mati karena aksesnya terbawa pengurus lama yang sudah lulus. Dengan OrgSpace, estafet admin cukup lewat transfer email, dan seluruh arsip proker serta tampilan web tetap rapi berlanjut.",
    avatarBg: "bg-blue-600 text-white",
    avatarText: "DA",
  },
  {
    id: "osis-sma",
    name: "Nadia Safitri",
    role: "Kepala Divisi Humas & Media OSIS",
    org: "SMA Taruna Bangsa",
    category: "Praktis dari Smartphone",
    categoryKey: "osis",
    rating: 5,
    date: "10 November 2026",
    content:
      "Waktu kami butuh website kilat untuk publikasi Open Recruitment panitia pentas seni, kami cuma butuh waktu 1 malam mengaturnya lewat HP. Tampilannya otomatis rapi dan responsif saat dibuka teman-teman siswa, pendaftar pun langsung membludak!",
    avatarBg: "bg-purple-600 text-white",
    avatarText: "NS",
  },
  {
    id: "ukm-seni",
    name: "Fauzan Rabbani",
    role: "Koordinator Kominfo UKM Musik & Seni",
    org: "Institut Seni Nusantara",
    category: "Hemat Anggaran Kas",
    categoryKey: "ukm",
    rating: 5,
    date: "05 November 2026",
    content:
      "Sebelumnya kas organisasi kami terkuras jutaan rupiah setiap tahun hanya untuk sewa hosting dan bayar jasa maintenance yang jarang di-update. Pakai OrgSpace jauh lebih hemat kas, mandiri dikelola anggota, dan desainnya sangat kekinian.",
    avatarBg: "bg-amber-600 text-white",
    avatarText: "FR",
  },
  {
    id: "hima-manajemen",
    name: "Siti Rahmawati",
    role: "Sekretaris Umum Himpunan Mahasiswa",
    org: "Fakultas Ekonomi & Bisnis",
    category: "Profil Proker Rapi",
    categoryKey: "himpunan",
    rating: 5,
    date: "01 November 2026",
    content:
      "Dulu kami cuma pakai link tree sederhana saat pengajuan proposal sponsorship ke perusahaan. Sekarang dengan website resmi dari OrgSpace, profil divisi dan program kerja kami terlihat sangat profesional di mata calon sponsor.",
    avatarBg: "bg-indigo-600 text-white",
    avatarText: "SR",
  },
  {
    id: "pramuka-penegak",
    name: "Bayu Wicaksono",
    role: "Pradana Dewan Ambalan Pramuka",
    org: "Gugus Depan Kota",
    category: "Update Agenda Cepat",
    categoryKey: "pramuka",
    rating: 5,
    date: "28 Oktober 2026",
    content:
      "Setiap kali ada perkemahan atau lomba kepramukaan, jadwal dan dokumentasi kegiatan langsung kami publikasikan dari HP di lokasi. Pembina dan orang tua siswa bisa memantau jalannya kegiatan dengan tenang.",
    avatarBg: "bg-teal-600 text-white",
    avatarText: "BW",
  },
  {
    id: "relawan-mengajar",
    name: "Aulia Zahra",
    role: "Koordinator Relawan Pendidikan",
    org: "Komunitas Mengajar Anak Bangsa",
    category: "Mudah Tanpa Koding",
    categoryKey: "komunitas",
    rating: 5,
    date: "25 Oktober 2026",
    content:
      "Tidak ada anggota tim kami yang punya latar belakang IT, tapi kami berhasil meluncurkan website penerimaan relawan dalam hitungan jam. Informasi kegiatan dan formulirnya terpasang sangat rapi.",
    avatarBg: "bg-rose-600 text-white",
    avatarText: "AZ",
  },
  {
    id: "ukm-olahraga",
    name: "Rifqi Maulana",
    role: "Ketua Unit Kegiatan Olahraga",
    org: "Kampus Olahraga Prestasi",
    category: "Bebas Biaya Server",
    categoryKey: "ukm",
    rating: 5,
    date: "20 Oktober 2026",
    content:
      "Sangat membantu karena versi awalnya gratis dan tanpa ribet konfigurasi server. Semua jadwal turnamen dan galeri kejuaraan tersimpan rapi tanpa perlu keluar biaya sewa server tahunan dari kas kami.",
    avatarBg: "bg-blue-600 text-white",
    avatarText: "RM",
  },
  {
    id: "mpk-sekolah",
    name: "Clarissa Putri",
    role: "Divisi Publikasi & Aspirasi MPK",
    org: "SMA Negeri Unggulan",
    category: "Mobile Responsif",
    categoryKey: "osis",
    rating: 5,
    date: "18 Oktober 2026",
    content:
      "Tampilannya modern dan tidak kaku. Teman-teman satu sekolah senang karena saat dibuka lewat smartphone sangat ringan, cepat, dan tata letaknya tidak berantakan sama sekali.",
    avatarBg: "bg-violet-600 text-white",
    avatarText: "CP",
  },
  {
    id: "rohis-kampus",
    name: "Arif Hidayat",
    role: "Ketua Syiar & Kominfo LDK",
    org: "Lembaga Dakwah Kampus",
    category: "Agenda Terjadwal",
    categoryKey: "komunitas",
    rating: 5,
    date: "14 Oktober 2026",
    content:
      "Modul kalender kegiatan memudahkan seluruh mahasiswa mengikuti kajian mingguan dan agenda bakti sosial. Kami tidak perlu lagi mencetak poster fisik berlebihan karena semua info terpusat di website.",
    avatarBg: "bg-emerald-600 text-white",
    avatarText: "AH",
  },
  {
    id: "pers-mahasiswa",
    name: "Dina Marlina",
    role: "Pimpinan Redaksi Pers Kampus",
    org: "LPM Suara Mahasiswa",
    category: "Publikasi Berita Cepat",
    categoryKey: "himpunan",
    rating: 5,
    date: "10 Oktober 2026",
    content:
      "Sebagai media pers kampus, kami membutuhkan wadah berita online yang cepat dan mudah diakses. Template editorial di OrgSpace sangat pas untuk mempublikasikan liputan investigasi dan opini mahasiswa.",
    avatarBg: "bg-cyan-600 text-white",
    avatarText: "DM",
  },
  {
    id: "paskibra-satya",
    name: "Gibran Alfarizi",
    role: "Komandan Batalyon Paskibra",
    org: "SMK Negeri 2",
    category: "Dokumentasi Prestasi",
    categoryKey: "pramuka",
    rating: 5,
    date: "05 Oktober 2026",
    content:
      "Etalase galeri foto dan rekapitulasi trofi juara kami pajang di website dengan sangat rapi. Calon anggota baru saat demo ekskul langsung terkesan dengan profil digital satuan kami.",
    avatarBg: "bg-red-600 text-white",
    avatarText: "GA",
  },
  {
    id: "paguyuban-daerah",
    name: "Maya Anggraini",
    role: "Koordinator Hubungan Luar",
    org: "Forum Komunikasi Mahasiswa Daerah",
    category: "Pusat Aspirasi",
    categoryKey: "komunitas",
    rating: 5,
    date: "01 Oktober 2026",
    content:
      "Menghubungkan ratusan perantau dari kampung halaman jadi lebih mudah. Pengumuman beasiswa daerah dan kumpul silaturahmi bisa diakses lewat satu link resmi yang mudah diingat.",
    avatarBg: "bg-orange-600 text-white",
    avatarText: "MA",
  },
];
