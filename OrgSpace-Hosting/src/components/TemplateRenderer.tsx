"use client";

import Link from "next/link";
import { useEffect, useMemo, useRef, useState } from "react";
import { SiteRenderData } from "@/lib/types";
import { CheckCircle2, AlertCircle } from "lucide-react";

interface TemplateRendererProps {
  site: SiteRenderData;
}

/**
 * TemplateRenderer - Engine Render Website Template Provider (OrgSpace-Hosting).
 * 
 * PRINSIP UTAMA: "Zero Layout Interference" (Kebebasan Penuh Desain Provider).
 * 1. Seluruh HTML template dari provider dirender 100% utuh tanpa dipotong/dislice.
 * 2. Tidak memaksa class CSS atau tema bawaan ke elemen template milik provider.
 * 3. Modul dinamis (data-ws-list, data-ws-collection, data-ws-form, data-ws-auth)
 *    bekerja secara HEADLESS: hanya menyuplai data dan event handler tanpa merusak
 *    struktur kartu, form, tata letak grid/flex, maupun font/warna asli buatan provider.
 */
export default function TemplateRenderer({ site }: TemplateRendererProps) {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";
  const assetBaseUrl = `${backendUrl}/api/public/sites/${site.slug}/assets/`;
  const containerRef = useRef<HTMLDivElement>(null);

  // Toast notifikasi non-intrusif untuk interaksi formulir
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error" = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // 1. Ekstrak tag stylesheet (<link rel="stylesheet">) dan <style> dari <head> template
  const headElements = useMemo(() => {
    if (!site.compiledHtml) return [];
    const headMatch = site.compiledHtml.match(/<head[^>]*>([\s\S]*?)<\/head>/i);
    if (!headMatch) return [];

    const headContent = headMatch[1];
    const elements: Array<{ type: "link" | "style"; content: string; key: string }> = [];

    // Ambil semua stylesheet
    const linkRegex = /<link[^>]*rel=["']stylesheet["'][^>]*>/gi;
    let match;
    let idx = 0;
    while ((match = linkRegex.exec(headContent)) !== null) {
      let tag = match[0];
      // Rewrite path lokal (css/, assets/) ke assetBaseUrl
      tag = tag.replace(/href=["'](\.?\/)?(css\/[^"']+)["']/gi, `href="${assetBaseUrl}$2"`);
      tag = tag.replace(/href=["'](\.?\/)?(assets\/[^"']+)["']/gi, `href="${assetBaseUrl}$2"`);
      elements.push({ type: "link", content: tag, key: `link-${idx++}` });
    }

    // Ambil semua blok <style>
    const styleRegex = /<style[^>]*>([\s\S]*?)<\/style>/gi;
    while ((match = styleRegex.exec(headContent)) !== null) {
      let styleContent = match[1];
      styleContent = styleContent.replace(/url\(["']?(\.?\/)?(img\/[^"')]+)["']?\)/gi, `url("${assetBaseUrl}$2")`);
      styleContent = styleContent.replace(/url\(["']?(\.?\/)?(assets\/[^"')]+)["']?\)/gi, `url("${assetBaseUrl}$2")`);
      elements.push({ type: "style", content: styleContent, key: `style-${idx++}` });
    }

    return elements;
  }, [site.compiledHtml, assetBaseUrl]);

  // 2. Ekstrak seluruh isi <body> template tanpa memotong satu section pun
  const cleanBodyHtml = useMemo(() => {
    if (!site.compiledHtml) return null;

    let html = site.compiledHtml;
    // Rewrite path aset lokal (img/, assets/, css/)
    html = html.replace(/src=["'](\.?\/)?(img\/[^"']+)["']/gi, `src="${assetBaseUrl}$2"`);
    html = html.replace(/src=["'](\.?\/)?(assets\/[^"']+)["']/gi, `src="${assetBaseUrl}$2"`);
    html = html.replace(/href=["'](\.?\/)?(css\/[^"']+)["']/gi, `href="${assetBaseUrl}$2"`);
    html = html.replace(/href=["'](\.?\/)?(assets\/[^"']+)["']/gi, `href="${assetBaseUrl}$2"`);

    // Rewrite auth login link ke portal pengurus
    html = html.replace(
      /(<a[^>]*data-ws-auth=["']login["'][^>]*href=["'])[^"']*["']/gi,
      `$1/${site.slug}/admin"`
    );
    html = html.replace(
      /(<a[^>]*href=["'])[^"']*(["'][^>]*data-ws-auth=["']login["'])/gi,
      `$1/${site.slug}/admin$2`
    );

    // Ambil isi <body> asli penyedia template
    const bodyMatch = html.match(/<body[^>]*>([\s\S]*?)<\/body>/i);
    return bodyMatch ? bodyMatch[1] : html;
  }, [site.compiledHtml, assetBaseUrl, site.slug]);

  // 3. Client-side Headless Hydration
  useEffect(() => {
    if (!containerRef.current) return;
    const root = containerRef.current;

    // A. Hydrate data-ws-auth (Bilah Status Pengurus)
    const loginLinks = root.querySelectorAll('a[data-ws-auth="login"]');
    loginLinks.forEach((el) => {
      el.setAttribute("href", `/${site.slug}/admin`);
    });

    const logoutBtns = root.querySelectorAll<HTMLElement>('[data-ws-auth="logout"]');
    logoutBtns.forEach((el) => {
      el.addEventListener("click", () => {
        localStorage.removeItem(`orgspace_site_token_${site.slug}`);
        window.location.reload();
      });
    });

    // Tampilan tamu dulu: tombol keluar & elemen khusus peran disembunyikan sampai sesi pengurus terbukti.
    const roleEls = root.querySelectorAll<HTMLElement>("[data-ws-role]");
    const setVisible = (el: HTMLElement, visible: boolean) => {
      el.style.display = visible ? "" : "none";
    };
    logoutBtns.forEach((el) => setVisible(el, false));
    roleEls.forEach((el) => setVisible(el, false));

    let savedToken: string | null = null;
    try {
      savedToken = localStorage.getItem(`orgspace_site_token_${site.slug}`);
    } catch {
      savedToken = null;
    }
    if (savedToken) {
      fetch(`${backendUrl}/api/public/sites/${site.slug}/auth/me`, {
        headers: { Authorization: `Bearer ${savedToken}` },
      })
        .then((res) => (res.ok ? res.json() : null))
        .then((data) => {
          if (!data) return;
          const u = data.user || data;
          root.querySelectorAll('[data-ws-auth="status"]').forEach((el) => {
            el.textContent = u.name || u.email;
          });
          logoutBtns.forEach((el) => setVisible(el, true));
          loginLinks.forEach((el) => setVisible(el as HTMLElement, false));
          // Pemilik boleh melihat semua yang boleh dilihat admin.
          const myRoles = u.role === "owner" ? ["owner", "admin"] : [u.role];
          roleEls.forEach((el) => {
            const allowed = (el.getAttribute("data-ws-role") || "")
              .split(",")
              .map((r) => r.trim().toLowerCase());
            setVisible(el, allowed.some((r) => myRoles.includes(r)));
          });
        })
        .catch(() => {});
    }

    // B. Hydrate data-ws-list & data-ws-collection (Koleksi Dinamis Tanpa Ubah Tata Letak)
    const listContainers = root.querySelectorAll("[data-ws-list], [data-ws-collection]");
    listContainers.forEach((container) => {
      const listName = (
        container.getAttribute("data-ws-list") ||
        container.getAttribute("data-ws-collection") ||
        ""
      ).toLowerCase();

      let items: Array<Record<string, any>> = [];
      if (listName === "berita" || listName === "kegiatan" || listName === "pengumuman") {
        items = (site.news || []).map((n) => ({
          judul: n.title,
          ringkasan: n.summary,
          konten: n.content,
          tanggal: n.publishedAt,
          foto: n.imageUrl || undefined,
          detail: `/${site.slug}#berita-${n.id}`,
        }));
      } else if (site.collections && site.collections[listName]) {
        items = site.collections[listName];
      }

      const limit = parseInt(container.getAttribute("data-ws-limit") || "", 10);
      if (limit > 0) items = items.slice(0, limit);

      const itemTemplate = container.querySelector("[data-ws-item]") as HTMLElement | null;
      const emptyElement = container.querySelector("[data-ws-empty]") as HTMLElement | null;

      if (!itemTemplate) return;

      if (items.length === 0) {
        itemTemplate.style.display = "none";
        if (emptyElement) emptyElement.style.display = "";
      } else {
        if (emptyElement) emptyElement.style.display = "none";
        itemTemplate.style.display = "";

        const templateHtml = itemTemplate.outerHTML;
        const parent = itemTemplate.parentElement;
        if (parent) {
          // Hapus klon sebelumnya
          parent.querySelectorAll("[data-hydrated='true']").forEach((node) => node.remove());

          // Isi template pertama
          populateElement(itemTemplate, items[0]);

          // Klon untuk item berikutnya (mempertahankan 100% struktur kartu & class CSS provider)
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
      const fields = el.querySelectorAll("[data-ws-field]");
      fields.forEach((fieldEl) => {
        const fieldName = fieldEl.getAttribute("data-ws-field");
        if (fieldName && data[fieldName] !== undefined) {
          const val = String(data[fieldName]);
          if (fieldEl.tagName === "IMG") {
            if (/^(https?:|\/)/i.test(val)) fieldEl.setAttribute("src", val);
          } else {
            fieldEl.textContent = val;
          }
        }
      });

      const links = el.querySelectorAll("[data-ws-link]");
      links.forEach((linkEl) => {
        const linkName = linkEl.getAttribute("data-ws-link");
        // Data diisi pengurus: hanya link biasa, bukan javascript: (bisa mencuri sesi pengurus lain).
        const href = linkName && data[linkName] ? String(data[linkName]).trim() : "";
        if (href && /^(https?:|mailto:|tel:|\/|#)/i.test(href)) {
          linkEl.setAttribute("href", href);
        }
      });
    }

    // C. Hydrate data-ws-form (Headless Form Submission: 100% Layout Asli Provider)
    const forms = root.querySelectorAll<HTMLFormElement>(
      "form[data-ws-form], [data-ws-form] form"
    );

    forms.forEach((form) => {
      // Pasang handler submit otomatis tanpa mengubah gaya tampilan form sedikit pun
      const handleSubmit = async (e: Event) => {
        e.preventDefault();
        const formEl = e.currentTarget as HTMLFormElement;
        const submitBtn = formEl.querySelector<HTMLButtonElement | HTMLInputElement>(
          'button[type="submit"], input[type="submit"]'
        );
        const originalText = submitBtn
          ? submitBtn.innerText || (submitBtn as HTMLInputElement).value
          : "";

        if (submitBtn) {
          submitBtn.setAttribute("disabled", "true");
          if (submitBtn.tagName === "INPUT") {
            (submitBtn as HTMLInputElement).value = "Mengirim...";
          } else {
            submitBtn.innerText = "Mengirim...";
          }
        }

        const formData = new FormData(formEl);
        const payload: Record<string, any> = {};
        formData.forEach((val, key) => {
          payload[key] = val;
        });

        const formName =
          formEl.getAttribute("data-ws-form") ||
          formEl.closest("[data-ws-form]")?.getAttribute("data-ws-form") ||
          "";

        try {
          const res = await fetch(`${backendUrl}/api/public/sites/${site.slug}/forms/${formName}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });
          const data = await res.json();

          const alertBox =
            formEl.querySelector<HTMLElement>("[data-ws-form-alert]") ||
            document.getElementById(`orgspace-form-alert-${formName}`);

          if (alertBox) {
            alertBox.style.display = "block";
            alertBox.style.background = "#dcfce7";
            alertBox.style.color = "#166534";
            alertBox.textContent = data.message || "Pesan/pendaftaran berhasil dikirim!";
          } else {
            showToast(data.message || "Formulir berhasil dikirim!");
          }
          formEl.reset();
        } catch {
          const alertBox =
            formEl.querySelector<HTMLElement>("[data-ws-form-alert]") ||
            document.getElementById(`orgspace-form-alert-${formName}`);

          if (alertBox) {
            alertBox.style.display = "block";
            alertBox.style.background = "#fee2e2";
            alertBox.style.color = "#991b1b";
            alertBox.textContent = "Gagal mengirim data. Silakan coba lagi.";
          } else {
            showToast("Gagal mengirim data. Silakan periksa koneksi internet.", "error");
          }
        } finally {
          if (submitBtn) {
            submitBtn.removeAttribute("disabled");
            if (submitBtn.tagName === "INPUT") {
              (submitBtn as HTMLInputElement).value = originalText;
            } else {
              submitBtn.innerText = originalText;
            }
          }
        }
      };

      form.addEventListener("submit", handleSubmit);
    });
  }, [site, assetBaseUrl, backendUrl]);

  return (
    <div className="w-full min-h-screen flex flex-col font-sans">
      {/* Toast Notifikasi Non-Intrusif */}
      {toast && (
        <div
          className={`fixed bottom-5 right-5 z-50 flex items-center gap-2 px-4 py-3 rounded-xl shadow-xl text-sm font-medium transition animate-slide-up ${
            toast.type === "success" ? "bg-emerald-600 text-white" : "bg-red-600 text-white"
          }`}
        >
          {toast.type === "success" ? (
            <CheckCircle2 className="w-4 h-4 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 shrink-0" />
          )}
          <span>{toast.message}</span>
        </div>
      )}

      {/* Top Bar Status Hosting OrgSpace */}
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

      {/* Load seluruh stylesheet dan style dari template provider */}
      {headElements.length > 0 ? (
        headElements.map((item) =>
          item.type === "link" ? (
            <div key={item.key} dangerouslySetInnerHTML={{ __html: item.content }} />
          ) : (
            <style key={item.key} dangerouslySetInnerHTML={{ __html: item.content }} />
          )
        )
      ) : (
        <link rel="stylesheet" href={`${assetBaseUrl}css/style.css`} />
      )}

      {/* RENDER SELURUH KONTEN TEMPLATE PROVIDER 100% UTUH */}
      {cleanBodyHtml ? (
        <div
          ref={containerRef}
          dangerouslySetInnerHTML={{ __html: cleanBodyHtml }}
          className="template-body-wrapper w-full"
        />
      ) : (
        <main className="flex-1 max-w-4xl mx-auto w-full px-6 py-12 text-center text-slate-500">
          <p>Konten website belum tersedia.</p>
        </main>
      )}
    </div>
  );
}
