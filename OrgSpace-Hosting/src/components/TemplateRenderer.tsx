"use client";

import { useMemo } from "react";
import { SiteRenderData } from "@/lib/types";
import DynamicForm from "./DynamicForm";

interface TemplateRendererProps {
  site: SiteRenderData;
}

export default function TemplateRenderer({ site }: TemplateRendererProps) {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";
  const assetBaseUrl = `${backendUrl}/api/public/sites/${site.slug}/assets/`;

  // Parse HTML terkompilasi agar menjadi elemen native React (tanpa keterbatasan iframe)
  const { headerAndHeroHtml, footerHtml } = useMemo(() => {
    if (!site.compiledHtml) {
      return { headerAndHeroHtml: null, footerHtml: null };
    }

    let html = site.compiledHtml;
    // Rewrite asset paths
    html = html.replace(/src=["']img\//g, `src="${assetBaseUrl}img/`);
    html = html.replace(/href=["']css\//g, `href="${assetBaseUrl}css/`);

    // Ambil isi body
    const bodyMatch = html.match(/<body[^>]*>([\s\S]*?)<\/body>/i);
    const bodyContent = bodyMatch ? bodyMatch[1] : html;

    // Pisahkan sebelum form pendaftaran dan sesudahnya
    const formSectionIndex = bodyContent.indexOf('<section id="pendaftaran"');
    if (formSectionIndex !== -1) {
      const headerAndHeroHtml = bodyContent.substring(0, formSectionIndex);
      const footerIndex = bodyContent.indexOf("<footer");
      const footerHtml = footerIndex !== -1 ? bodyContent.substring(footerIndex) : "";
      return { headerAndHeroHtml, footerHtml };
    }

    const footerIndex = bodyContent.indexOf("<footer");
    if (footerIndex !== -1) {
      return {
        headerAndHeroHtml: bodyContent.substring(0, footerIndex),
        footerHtml: bodyContent.substring(footerIndex),
      };
    }

    return { headerAndHeroHtml: bodyContent, footerHtml: "" };
  }, [site.compiledHtml, assetBaseUrl]);

  return (
    <div className="w-full min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Banner indikator OrgSpace Hosting */}
      <div className="w-full bg-slate-900 text-white text-xs py-1.5 px-4 flex items-center justify-between sticky top-0 z-50 shadow-sm">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-400 inline-block animate-pulse"></span>
          <span>OrgSpace Hosting • <strong>{site.organizationName}</strong></span>
        </div>
        <div className="flex items-center gap-3 text-slate-300">
          <span className="bg-emerald-600/80 px-2 py-0.5 rounded text-[11px] font-semibold">Mode Template</span>
          <span>v{site.version}</span>
        </div>
      </div>

      {/* Load stylesheet template dari backend */}
      <link rel="stylesheet" href={`${assetBaseUrl}css/style.css`} />

      {/* Konten template (Header, Hero, Kegiatan / Berita) */}
      {headerAndHeroHtml ? (
        <div
          dangerouslySetInnerHTML={{ __html: headerAndHeroHtml }}
          className="template-body-wrapper"
        />
      ) : (
        <main className="flex-1 max-w-4xl mx-auto w-full px-6 py-12 space-y-12">
          <section className="text-center py-8">
            <span className="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-700 mb-3">
              Website Resmi
            </span>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-slate-900">
              {site.name}
            </h1>
            <p className="mt-3 text-lg text-slate-600 max-w-2xl mx-auto">
              Selamat datang di website resmi {site.organizationName}.
            </p>
          </section>
        </main>
      )}

      {/* Formulir Pendaftaran Interaktif Native React */}
      {site.forms && site.forms.length > 0 && (
        <div id="pendaftaran" className="sec my-8 px-4 max-w-3xl mx-auto w-full">
          <div className="text-center mb-6">
            <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
              Pendaftaran Anggota Baru
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Daftarkan diri Anda melalui formulir resmi di bawah ini.
            </p>
          </div>
          {site.forms.map((f) => (
            <DynamicForm key={f.name} slug={site.slug} config={f} />
          ))}
        </div>
      )}

      {/* Footer template */}
      {footerHtml ? (
        <div dangerouslySetInnerHTML={{ __html: footerHtml }} />
      ) : (
        <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-400">
          Diterbitkan melalui OrgSpace • &copy; {new Date().getFullYear()} {site.organizationName}
        </footer>
      )}
    </div>
  );
}
