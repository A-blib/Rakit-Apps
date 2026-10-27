"use client";

import { deleteUpdate } from "../../actions";

/** Hapus info beserta gambarnya. Tidak bisa dibatalkan, jadi selalu minta konfirmasi dulu. */
export function DeleteUpdateButton({ id, title, className }: { id: string; title: string; className?: string }) {
  return (
    <form
      action={deleteUpdate.bind(null, id)}
      onSubmit={(event) => {
        if (!confirm(`Hapus info "${title}"? Info dan gambarnya hilang permanen.`)) event.preventDefault();
      }}
    >
      <button type="submit" className={className} aria-label={`Hapus info ${title}`}>
        Hapus
      </button>
    </form>
  );
}
