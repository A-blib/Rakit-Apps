import Image from "next/image";
import type { PortalUpdate } from "@/lib/api/types";
import { UPDATE_CATEGORY_LABEL, formatDate, updateImageSrc } from "@/lib/format";

/**
 * CONTOH AWAL untuk tim web: section "Info terbaru OrgSpace" di landing. Bebas dirombak tampilannya.
 * Data: getLatestUpdates() (maks 3, terbaru dulu, tanpa isi lengkap). Tombol detail membuka /info/{id} di tab baru.
 */
export function LatestUpdates({ updates }: { updates: PortalUpdate[] }) {
  return (
    <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8" aria-labelledby="info-terbaru">
      <h2 id="info-terbaru" className="text-2xl font-bold tracking-tight text-navy sm:text-3xl">
        Info terbaru OrgSpace
      </h2>
      <p className="mt-2 text-sm text-muted">Fitur baru, optimasi, dan perubahan tampilan.</p>
      {updates.length === 0 ? (
        <p className="card mt-8 p-8 text-center text-sm text-muted">Belum ada informasi terbaru.</p>
      ) : (
        <ul className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {updates.map((update) => {
            const image = updateImageSrc(update);
            return (
              <li key={update.id} className="card flex flex-col overflow-hidden">
                {image && (
                  <Image
                    src={image}
                    alt={update.imageAlt ?? ""}
                    width={640}
                    height={360}
                    unoptimized
                    className="aspect-video w-full object-cover"
                  />
                )}
                <div className="flex flex-1 flex-col p-4">
                  <p className="text-xs text-muted">
                    <span className="badge-neutral text-[10px]">{UPDATE_CATEGORY_LABEL[update.category]}</span>
                    {update.publishedAt && <span className="ml-2">{formatDate(update.publishedAt)}</span>}
                  </p>
                  <h3 className="mt-2 text-sm font-semibold text-foreground">{update.title}</h3>
                  <p className="mt-1 flex-1 text-sm text-muted">{update.summary}</p>
                  <a
                    href={`/info/${update.id}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn-outline mt-4 w-full text-sm"
                    aria-label={`Lihat detail: ${update.title} (tab baru)`}
                  >
                    Lihat detail
                  </a>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
