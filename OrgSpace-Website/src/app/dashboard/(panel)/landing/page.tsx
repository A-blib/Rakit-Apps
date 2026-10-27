import Form from "next/form";
import { Pagination } from "@/components/Pagination";
import { adminApi } from "@/lib/api/admin";
import { getTemplates } from "@/lib/api/public";
import { adminToken } from "@/lib/auth/session";
import { formatDateTime } from "@/lib/format";
import { FeaturedPicker } from "./FeaturedPicker";
import { HeroForm } from "./HeroForm";

/** Satu wadah galeri = 16 template; admin pindah wadah lewat Pagination. */
const WADAH_SIZE = 16;

/** CONTOH AWAL untuk tim web: pengatur hero & template unggulan landing page. Bebas dirombak tampilannya. */
export default async function LandingContentPage({ searchParams }: PageProps<"/dashboard/landing">) {
  const params = await searchParams;
  const q = typeof params.q === "string" ? params.q.trim() : "";
  const page = Math.max(0, Number(params.page) || 0);
  const token = await adminToken();
  const [hero, featured, gallery] = await Promise.all([
    adminApi.landingHero(token),
    adminApi.landingTemplates(token),
    getTemplates({ q: q || undefined, page, size: WADAH_SIZE }),
  ]);
  const hrefFor = (target: number) =>
    `/dashboard/landing?${new URLSearchParams({ ...(q && { q }), page: String(target) })}#template-unggulan`;

  return (
    <div className="max-w-4xl space-y-10">
      <header className="border-b border-border/80 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">Konten Landing</h1>
        <p className="mt-1 text-xs text-muted">Atur hero dan template unggulan di halaman depan orgspace.com.</p>
      </header>

      <section className="space-y-4">
        <div>
          <h2 className="text-base font-semibold text-foreground">Hero</h2>
          <p className="text-xs text-muted">Terakhir diubah {formatDateTime(hero.updatedAt)}.</p>
        </div>
        <HeroForm hero={hero} />
      </section>

      <section id="template-unggulan" className="space-y-4 scroll-mt-20">
        <div>
          <h2 className="text-base font-semibold text-foreground">Template unggulan</h2>
          <p className="text-xs text-muted">
            Pilih maks 12 template dari galeri. Belum ketemu yang pas? Pindah ke halaman galeri berikutnya.
          </p>
        </div>
        <Form action="/dashboard/landing" className="flex gap-2">
          <input className="input text-xs" name="q" defaultValue={q} placeholder="Cari nama template..." aria-label="Cari template" />
          <button type="submit" className="btn-outline h-9 px-3.5 text-xs">Cari</button>
        </Form>
        <FeaturedPicker initial={featured} gallery={gallery.items} />
        <Pagination page={gallery.page} totalPages={gallery.totalPages} hrefFor={hrefFor} />
      </section>
    </div>
  );
}
