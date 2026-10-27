import Image from "next/image";
import type { GalleryTemplate } from "@/lib/api/types";
import { templateThumbnailSrc } from "@/lib/format";

/**
 * CONTOH AWAL untuk tim web: section "website yang akan kamu dapatkan" dari template unggulan pilihan admin.
 * Bebas dirombak tampilannya. Data: getLandingTemplates() (maks 12). Tombol preview membuka previewUrl
 * (OrgSpace-Hosting, ter-sandbox) di tab baru.
 */
export function FeaturedTemplates({ templates }: { templates: GalleryTemplate[] }) {
  if (templates.length === 0) {
    return null;
  }
  return (
    <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8" aria-labelledby="template-unggulan">
      <h2 id="template-unggulan" className="text-2xl font-bold tracking-tight text-navy sm:text-3xl">
        Website seperti ini yang akan kamu dapatkan
      </h2>
      <p className="mt-2 text-sm text-muted">Pilih template, ganti isinya, lalu terbitkan.</p>
      <ul className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {templates.map((template) => {
          const thumbnail = templateThumbnailSrc(template);
          return (
            <li key={template.id} className="card overflow-hidden">
              <div className="aspect-[4/3] bg-surface-subtle">
                {thumbnail && (
                  <Image
                    src={thumbnail}
                    alt={`Tampilan template ${template.name}`}
                    width={640}
                    height={480}
                    unoptimized
                    className="h-full w-full object-cover object-top"
                  />
                )}
              </div>
              <div className="p-4">
                <h3 className="text-sm font-semibold text-foreground">{template.name}</h3>
                <p className="mt-0.5 text-xs text-muted">oleh {template.creatorName}</p>
                {/* Data landing di-cache 5 menit; data lama (sebelum ada previewUrl) jangan jadi tombol mati. */}
                {template.previewUrl && (
                  <a
                    href={template.previewUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn-outline mt-3 w-full text-sm"
                    aria-label={`Lihat preview template ${template.name} (tab baru)`}
                  >
                    Lihat preview
                  </a>
                )}
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
