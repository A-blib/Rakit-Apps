import { getUpdateImage } from "@/lib/api/public";

/** Meneruskan gambar info terbaru dari backend, karena browser tidak pernah memanggil backend langsung. */
export async function GET(_: Request, { params }: RouteContext<"/update-image/[id]">) {
  const { id } = await params;
  let upstream: Response;
  try {
    upstream = await getUpdateImage(id);
  } catch {
    return new Response(null, { status: 502 });
  }
  if (!upstream.ok) {
    // Draft, id aneh, atau tanpa gambar: cukup 404, tanpa meneruskan isi error backend.
    return new Response(null, { status: upstream.status < 500 ? 404 : 502 });
  }
  return new Response(upstream.body, {
    // URL membawa ?v=<imageUpdatedAt>; gambar baru = URL baru, jadi aman di-cache lama.
    headers: { "Content-Type": "image/png", "Cache-Control": "public, max-age=2592000, immutable" },
  });
}
