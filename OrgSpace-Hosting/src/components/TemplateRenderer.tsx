"use client";

import Link from "next/link";
import { useEffect, useMemo, useRef } from "react";
import { SiteRenderData } from "@/lib/types";
import DynamicForm from "./DynamicForm";

interface TemplateRendererProps {
  site: SiteRenderData;
}

export default function TemplateRenderer({ site }: TemplateRendererProps) {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";
  const assetBaseUrl = `${backendUrl}/api/public/sites/${site.slug}/assets/`;
  const containerRef = useRef<HTMLDivElement>(null);

  // Parse HTML terkompilasi agar menjadi elemen native React (tanpa keterbatasan iframe)
  const { headerAndHeroHtml, footerHtml } = useMemo(() => {
    if (!site.compiledHtml) {
      return { headerAndHeroHtml: null, footerHtml: null };
    }

    let html = site.compiledHtml;
    // Rewrite asset paths
    html = html.replace(/src=["']img\//g, `src="${assetBaseUrl}img/`);
    html = html.replace(/href=["']css\//g, `href="${assetBaseUrl}css/`);

    // Rewrite auth login links to portal pengurus
    html = html.replace(
      /(<a[^>]*data-ws-auth=["']login["'][^>]*href=["'])[^"']*["']/gi,
      `$1/${site.slug}/admin"`
    );
    html = html.replace(
      /(<a[^>]*href=["'])[^"']*(["'][^>]*data-ws-auth=["']login["'])/gi,
      `$1/${site.slug}/admin$2`
    );

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
  }, [site.compiledHtml, assetBaseUrl, site.slug]);

  // Client-side Hydration untuk data-ws-auth dan data-ws-list
  useEffect(() => {
    if (!containerRef.current) return;
    const root = containerRef.current;

    // 1. Hydrate data-ws-auth links
    const loginLinks = root.querySelectorAll('a[data-ws-auth="login"]');
    loginLinks.forEach((el) => {
      el.setAttribute("href", `/${site.slug}/admin`);
    });

    const logoutLinks = root.querySelectorAll('a[data-ws-auth="logout"]');
    logoutLinks.forEach((el) => {
      el.setAttribute("href", `/${site.slug}/admin`);
    });

    // 2. Hydrate data-ws-list containers
    const listContainers = root.querySelectorAll("[data-ws-list], [data-ws-collection]");
    listContainers.forEach((container) => {
      const listName = (
        container.getAttribute("data-ws-list") ||
        container.getAttribute("data-ws-collection") ||
        ""
      ).toLowerCase();

      let items: Array<Record<string, any>> = [];
      if (listName === "berita" || listName === "kegiatan") {
        items = (site.news || []).map((n) => ({
          judul: n.title,
          ringkasan: n.summary,
          konten: n.content,
          tanggal: n.publishedAt,
          foto: `${assetBaseUrl}img/logo.png`,
          detail: `/${site.slug}#berita-${n.id}`,
        }));
      } else if (site.collections && site.collections[listName]) {
        items = site.collections[listName];
      }

      const itemTemplate = container.querySelector("[data-ws-item]") as HTMLElement | null;
      const emptyElement = container.querySelector("[data-ws-empty]") as HTMLElement | null;

      if (!itemTemplate) return;

      if (items.length === 0) {
        itemTemplate.style.display = "none";
        if (emptyElement) emptyElement.style.display = "block";
      } else {
        if (emptyElement) emptyElement.style.display = "none";
        itemTemplate.style.display = "";

        // Template HTML asli untuk di-clone
        const templateHtml = itemTemplate.outerHTML;

        // Bersihkan item lama jika sudah di-hydrate sebelumnya
        const parent = itemTemplate.parentElement;
        if (parent) {
          // Hapus semua elemen yang di-hydrate sebelumnya (kecuali template asli)
          parent.querySelectorAll("[data-hydrated='true']").forEach((node) => node.remove());

          // Ubah template asli menjadi item pertama
          populateElement(itemTemplate, items[0]);

          // Clone untuk sisa items
          for (let i = 1; i < items.length; i++) {
            const tempDiv = document.createElement("div");
            tempDiv.innerHTML = templateHtml;
            const clone = tempDiv.firstElementChild as HTMLElement;
            if (clone) {
              clone.setAttribute("data-hydrated", "true");
              populateElement(clone, items[i]);
              parent.insertBefore(clone, emptyElement || null);
            }
          }
        }
      }
    });

    function populateElement(el: HTMLElement, data: Record<string, any>) {
      // Isi semua field yang bertanda data-ws-field
      const fields = el.querySelectorAll("[data-ws-field]");
      fields.forEach((fieldEl) => {
        const fieldName = fieldEl.getAttribute("data-ws-field");
        if (fieldName && data[fieldName] !== undefined) {
          const val = String(data[fieldName]);
          if (fieldEl.tagName === "IMG") {
            fieldEl.setAttribute("src", val);
          } else {
            fieldEl.textContent = val;
          }
        }
      });

      // Isi semua link yang bertanda data-ws-link
      const links = el.querySelectorAll("[data-ws-link]");
      links.forEach((linkEl) => {
        const linkName = linkEl.getAttribute("data-ws-link");
        if (linkName && data[linkName]) {
          linkEl.setAttribute("href", String(data[linkName]));
        }
      });
    }
  }, [site, assetBaseUrl]);

  // Koleksi yang belum ada di template HTML (ditampilkan sebagai fallback card)
  const unhandledCollections = useMemo(() => {
    if (!site.collections) return [];
    return Object.entries(site.collections).filter(([colName]) => {
      if (!site.compiledHtml) return true;
      const lower = site.compiledHtml.toLowerCase();
      return (
        !lower.includes(`data-ws-list="${colName.toLowerCase()}"`) &&
        !lower.includes(`data-ws-collection="${colName.toLowerCase()}"`)
      );
    });
  }, [site.collections, site.compiledHtml]);

  return (
    <div className="w-full min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Banner indikator OrgSpace Hosting */}
      <div className="w-full bg-slate-900 text-white text-xs py-1.5 px-4 flex items-center justify-between sticky top-0 z-50 shadow-sm">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-400 inline-block animate-pulse"></span>
          <span>
            OrgSpace Hosting • <strong>{site.organizationName}</strong>
          </span>
        </div>
        <div className="flex items-center gap-3 text-slate-300">
          <Link
            href={`/${site.slug}/admin`}
            className="inline-flex items-center gap-1 bg-blue-600 hover:bg-blue-500 text-white px-2.5 py-0.5 rounded text-[11px] font-semibold transition shadow-sm"
          >
            <span>Portal Pengurus</span>
          </Link>
          <span className="bg-emerald-600/80 px-2 py-0.5 rounded text-[11px] font-semibold">
            Mode Template
          </span>
          <span>v{site.version}</span>
        </div>
      </div>

      {/* Load stylesheet template dari backend */}
      <link rel="stylesheet" href={`${assetBaseUrl}css/style.css`} />

      {/* Konten template (Header, Hero, Kegiatan / Berita) */}
      {headerAndHeroHtml ? (
        <div
          ref={containerRef}
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

      {/* Fallback Modul Koleksi Kustom (jika tidak ada di HTML template) */}
      {unhandledCollections.length > 0 && (
        <div className="max-w-4xl mx-auto w-full px-4 my-8 space-y-8">
          {unhandledCollections.map(([colName, items]) => (
            <section key={colName} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
              <div className="mb-4">
                <span className="text-[11px] font-semibold uppercase tracking-wider text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
                  Modul Organisasi
                </span>
                <h3 className="text-xl font-bold text-slate-900 capitalize mt-1">
                  {colName}
                </h3>
              </div>

              {items.length === 0 ? (
                <p className="text-xs text-slate-400">Belum ada data untuk {colName}.</p>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                  {items.map((item, idx) => (
                    <div
                      key={item.id || idx}
                      className="p-4 rounded-xl border border-slate-100 bg-slate-50/60 hover:bg-white hover:border-slate-200 transition shadow-xs"
                    >
                      {Object.entries(item).map(([k, v]) => {
                        if (k === "id") return null;
                        return (
                          <div key={k} className="mb-1.5 last:mb-0">
                            <span className="block text-[10px] uppercase font-mono text-slate-400">
                              {k}
                            </span>
                            <span className="text-xs font-medium text-slate-800">
                              {String(v)}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  ))}
                </div>
              )}
            </section>
          ))}
        </div>
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
