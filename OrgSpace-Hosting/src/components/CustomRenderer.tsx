"use client";

import { useMemo } from "react";
import { SiteRenderData, CustomSection } from "@/lib/types";
import DynamicForm from "./DynamicForm";
import {
  Users,
  Calendar,
  Sparkles,
  MapPin,
  Mail,
  Phone,
  Globe,
  ArrowRight,
  CheckCircle,
  ExternalLink,
} from "lucide-react";

interface CustomRendererProps {
  site: SiteRenderData;
}

export default function CustomRenderer({ site }: CustomRendererProps) {
  const fields = site.content?.fields || {};
  const styles = site.content?.styles || {};
  const theme = site.content?.theme || {};

  // Susunan section default jika user merakit sendiri
  const sections: CustomSection[] = useMemo(() => {
    if (site.content?.sections && site.content.sections.length > 0) {
      return site.content.sections.filter((s) => !s.hidden);
    }
    // Default 10 section standar Rakit Sendiri (alur-buat-website-via-template & penyempurnaan-system)
    return [
      { id: "s-header", type: "header", name: "Header & Navigasi" },
      { id: "s-hero", type: "hero", name: "Hero Utama" },
      { id: "s-profile", type: "profile", name: "Profil Organisasi" },
      { id: "s-structure", type: "structure", name: "Struktur Kepengurusan" },
      { id: "s-programs", type: "programs", name: "Program Kerja" },
      { id: "s-news", type: "news", name: "Berita & Pengumuman" },
      { id: "s-gallery", type: "gallery", name: "Galeri Kegiatan" },
      { id: "s-form", type: "form", name: "Formulir Pendaftaran" },
      { id: "s-contact", type: "contact", name: "Kontak & Media Sosial" },
      { id: "s-footer", type: "footer", name: "Footer" },
    ];
  }, [site.content?.sections]);

  // Ekstrak helper gaya khusus untuk suatu field
  const getFieldStyle = (key: string): React.CSSProperties => {
    const s = styles[key];
    if (!s) return {};
    const res: React.CSSProperties = {};
    if (s.color) res.color = s.color;
    if (s["background-color"]) res.backgroundColor = s["background-color"];
    if (s["font-size"]) res.fontSize = typeof s["font-size"] === "number" ? `${s["font-size"]}px` : s["font-size"];
    if (s["border-radius"]) res.borderRadius = typeof s["border-radius"] === "number" ? `${s["border-radius"]}px` : s["border-radius"];
    return res;
  };

  return (
    <div
      className="min-h-screen bg-slate-50 text-slate-900 font-sans selection:bg-blue-500 selection:text-white"
      style={theme as React.CSSProperties}
    >
      {/* Banner indikator OrgSpace Hosting */}
      <div className="w-full bg-slate-900 text-white text-xs py-1.5 px-4 flex items-center justify-between sticky top-0 z-50 shadow-sm">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-blue-400 inline-block animate-pulse"></span>
          <span>OrgSpace Hosting • <strong>{site.organizationName}</strong></span>
        </div>
        <div className="flex items-center gap-3 text-slate-300">
          <span className="bg-blue-600/60 px-2 py-0.5 rounded text-[11px] font-semibold">Mode Rakit / Custom</span>
          <span>v{site.version}</span>
        </div>
      </div>

      {sections.map((section) => {
        switch (section.type) {
          case "header":
            return (
              <header
                key={section.id}
                className="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-7 z-40 px-6 py-4"
              >
                <div className="max-w-6xl mx-auto flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-blue-600 text-white font-bold flex items-center justify-center text-lg shadow-sm">
                      {site.organizationName.charAt(0)}
                    </div>
                    <div>
                      <div className="font-bold text-base text-slate-900 leading-tight">
                        {fields["header-brand"]?.text || site.organizationName}
                      </div>
                      <div className="text-xs text-slate-500">Website Resmi</div>
                    </div>
                  </div>
                  <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-600">
                    <a href="#hero" className="hover:text-blue-600 transition">Beranda</a>
                    <a href="#profil" className="hover:text-blue-600 transition">Profil</a>
                    <a href="#struktur" className="hover:text-blue-600 transition">Pengurus</a>
                    <a href="#program" className="hover:text-blue-600 transition">Program</a>
                    <a href="#berita" className="hover:text-blue-600 transition">Berita</a>
                    <a href="#daftar" className="hover:text-blue-600 transition">Pendaftaran</a>
                  </nav>
                  <a
                    href="#daftar"
                    className="px-4 py-2 rounded-full bg-blue-600 text-white text-xs font-semibold hover:bg-blue-700 transition shadow-sm"
                  >
                    Daftar Sekarang
                  </a>
                </div>
              </header>
            );

          case "hero":
            return (
              <section
                key={section.id}
                id="hero"
                className="relative overflow-hidden py-20 px-6 bg-gradient-to-b from-blue-50/60 to-white"
              >
                <div className="max-w-5xl mx-auto text-center space-y-6">
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-700">
                    <Sparkles className="w-3.5 h-3.5" />
                    {fields["hero-badge"]?.text || "Organisasi Pelajar & Pemuda Indonesia"}
                  </span>
                  <h1
                    className="text-4xl sm:text-5xl lg:text-6xl font-black text-slate-900 tracking-tight leading-tight"
                    style={getFieldStyle("hero-title")}
                  >
                    {fields["hero-title"]?.text || site.name}
                  </h1>
                  <p
                    className="text-lg sm:text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed"
                    style={getFieldStyle("hero-desc")}
                  >
                    {fields["hero-desc"]?.text ||
                      "Wadah aspirasi, pengembangan bakat, dan kolaborasi positif untuk mencetak generasi pemimpin masa depan."}
                  </p>
                  <div className="pt-4 flex flex-wrap items-center justify-center gap-4">
                    <a
                      href="#daftar"
                      className="px-6 py-3.5 rounded-full bg-blue-600 text-white font-semibold text-sm hover:bg-blue-700 active:scale-95 transition shadow-md shadow-blue-500/20 inline-flex items-center gap-2"
                      style={getFieldStyle("hero-cta")}
                    >
                      <span>{fields["hero-cta"]?.text || "Bergabung Bersama Kami"}</span>
                      <ArrowRight className="w-4 h-4" />
                    </a>
                    <a
                      href="#profil"
                      className="px-6 py-3.5 rounded-full bg-white text-slate-700 border border-slate-200 font-semibold text-sm hover:bg-slate-50 transition"
                    >
                      Pelajari Lebih Lanjut
                    </a>
                  </div>
                </div>
              </section>
            );

          case "profile":
            return (
              <section key={section.id} id="profil" className="py-16 px-6 bg-white border-t border-slate-100">
                <div className="max-w-5xl mx-auto">
                  <div className="text-center mb-12">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Tentang Kami</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Visi &amp; Misi Organisasi</h2>
                  </div>
                  <div className="grid md:grid-cols-2 gap-8">
                    <div className="p-8 rounded-2xl bg-blue-50/50 border border-blue-100">
                      <h3 className="text-xl font-bold text-blue-900 mb-3">Visi</h3>
                      <p className="text-slate-700 leading-relaxed text-sm">
                        {fields["visi"]?.text ||
                          "Menjadi wadah kepemimpinan yang berintegritas, kreatif, dan berdampak nyata bagi seluruh anggota dan masyarakat."}
                      </p>
                    </div>
                    <div className="p-8 rounded-2xl bg-emerald-50/50 border border-emerald-100">
                      <h3 className="text-xl font-bold text-emerald-900 mb-3">Misi Utama</h3>
                      <ul className="space-y-2.5 text-sm text-slate-700">
                        <li className="flex items-start gap-2">
                          <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                          <span>Mendorong partisipasi aktif anggota dalam program pengabdian dan inovasi.</span>
                        </li>
                        <li className="flex items-start gap-2">
                          <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                          <span>Menyelenggarakan pelatihan keterampilan kepemimpinan dan manajemen organisasi.</span>
                        </li>
                        <li className="flex items-start gap-2">
                          <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                          <span>Membangun jejaring kolaborasi lintas divisi dan institusi.</span>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </section>
            );

          case "structure":
            return (
              <section key={section.id} id="struktur" className="py-16 px-6 bg-slate-50">
                <div className="max-w-5xl mx-auto">
                  <div className="text-center mb-12">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Manajemen</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Struktur Kepengurusan</h2>
                    <p className="text-sm text-slate-600 mt-2">Jajaran pimpinan dan pengurus periode aktif</p>
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
                    {[
                      { role: "Ketua Umum", name: "Ahmad Rizki", div: "BPH" },
                      { role: "Wakil Ketua", name: "Siti Rahma", div: "BPH" },
                      { role: "Sekretaris", name: "Dina Novita", div: "Administrasi" },
                      { role: "Bendahara", name: "Budi Santoso", div: "Keuangan" },
                    ].map((person, idx) => (
                      <div key={idx} className="bg-white p-5 rounded-2xl border border-slate-200 text-center shadow-sm">
                        <div className="w-16 h-16 rounded-full bg-slate-200 mx-auto mb-3 flex items-center justify-center text-slate-500 font-bold">
                          {person.name.charAt(0)}
                        </div>
                        <div className="font-bold text-sm text-slate-900">{person.name}</div>
                        <div className="text-xs font-semibold text-blue-600 mt-0.5">{person.role}</div>
                        <div className="text-[11px] text-slate-400 mt-1">{person.div}</div>
                      </div>
                    ))}
                  </div>
                </div>
              </section>
            );

          case "programs":
            return (
              <section key={section.id} id="program" className="py-16 px-6 bg-white">
                <div className="max-w-5xl mx-auto">
                  <div className="text-center mb-12">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Agenda Kerja</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Program Unggulan</h2>
                  </div>
                  <div className="grid sm:grid-cols-3 gap-6">
                    {[
                      { title: "Latihan Dasar Kepemimpinan", tag: "Terjadwal", desc: "Pembekalan intensif wawasan organisasi dan kepemimpinan bagi seluruh kader baru." },
                      { title: "Pekan Kreasi & Bakat Siswa", tag: "Berjalan", desc: "Ajang unjuk karya, olahraga, dan seni antar anggota untuk menggali potensi terbaik." },
                      { title: "Bakti Sosial & Peduli Lingkungan", tag: "Selesai", desc: "Aksi nyata kepedulian lingkungan dan pembagian sembako ke panti asuhan sekitar." },
                    ].map((p, idx) => (
                      <div key={idx} className="p-6 rounded-2xl border border-slate-200 bg-slate-50 flex flex-col justify-between">
                        <div>
                          <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800 mb-3">
                            {p.tag}
                          </span>
                          <h3 className="font-bold text-base text-slate-900 mb-2">{p.title}</h3>
                          <p className="text-xs text-slate-600 leading-relaxed">{p.desc}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </section>
            );

          case "news":
            return (
              <section key={section.id} id="berita" className="py-16 px-6 bg-slate-50 border-t border-slate-200">
                <div className="max-w-5xl mx-auto">
                  <div className="text-center mb-12">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Informasi Terkini</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Berita &amp; Artikel</h2>
                  </div>
                  <div className="grid sm:grid-cols-2 gap-6">
                    {site.news.map((item) => (
                      <article key={item.id} className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm">
                        <span className="text-xs text-slate-500 font-medium">{item.publishedAt}</span>
                        <h3 className="text-lg font-bold text-slate-900 mt-1">{item.title}</h3>
                        <p className="text-sm text-slate-600 mt-2 leading-relaxed line-clamp-3">
                          {item.summary || item.content}
                        </p>
                      </article>
                    ))}
                  </div>
                </div>
              </section>
            );

          case "gallery":
            return (
              <section key={section.id} id="galeri" className="py-16 px-6 bg-white">
                <div className="max-w-5xl mx-auto">
                  <div className="text-center mb-12">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Dokumentasi</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Galeri Foto</h2>
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                    {[1, 2, 3, 4, 5, 6].map((i) => (
                      <div
                        key={i}
                        className="aspect-video rounded-xl bg-slate-200 border border-slate-300 flex items-center justify-center text-xs text-slate-500 font-medium hover:opacity-90 transition"
                      >
                        Dokumentasi #{i}
                      </div>
                    ))}
                  </div>
                </div>
              </section>
            );

          case "form":
            return (
              <section key={section.id} id="daftar" className="py-16 px-6 bg-slate-100/60 border-t border-slate-200">
                <div className="max-w-4xl mx-auto">
                  <div className="text-center mb-8">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Formulir Interaktif</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Gabung atau Kirim Pesan</h2>
                  </div>
                  {site.forms.map((f) => (
                    <DynamicForm key={f.name} slug={site.slug} config={f} />
                  ))}
                </div>
              </section>
            );

          case "contact":
            return (
              <section key={section.id} id="kontak" className="py-16 px-6 bg-white border-t border-slate-200">
                <div className="max-w-5xl mx-auto">
                  <div className="text-center mb-12">
                    <span className="text-xs font-bold uppercase tracking-wider text-blue-600">Hubungi Kami</span>
                    <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Sekretariat &amp; Media</h2>
                  </div>
                  <div className="grid sm:grid-cols-3 gap-6">
                    <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 text-center">
                      <MapPin className="w-6 h-6 text-blue-600 mx-auto mb-2" />
                      <div className="font-bold text-sm text-slate-900">Alamat</div>
                      <div className="text-xs text-slate-600 mt-1">Gedung Olahraga &amp; Kegiatan Siswa</div>
                    </div>
                    <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 text-center">
                      <Mail className="w-6 h-6 text-blue-600 mx-auto mb-2" />
                      <div className="font-bold text-sm text-slate-900">Email</div>
                      <div className="text-xs text-slate-600 mt-1">info@{site.slug}.orgspace.app</div>
                    </div>
                    <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 text-center">
                      <Phone className="w-6 h-6 text-blue-600 mx-auto mb-2" />
                      <div className="font-bold text-sm text-slate-900">WhatsApp</div>
                      <div className="text-xs text-slate-600 mt-1">0812-3456-7890</div>
                    </div>
                  </div>
                </div>
              </section>
            );

          case "footer":
            return (
              <footer key={section.id} className="py-10 px-6 bg-slate-900 text-slate-400 text-xs text-center border-t border-slate-800">
                <div className="max-w-5xl mx-auto space-y-3">
                  <div className="font-bold text-sm text-white">{site.organizationName}</div>
                  <p>Website ini dibuat dan dikelola melalui platform pembangun website OrgSpace.</p>
                  <p>&copy; {new Date().getFullYear()} {site.organizationName}. Semua hak dilindungi.</p>
                </div>
              </footer>
            );

          default:
            return null;
        }
      })}
    </div>
  );
}
