import { getHeroImage } from "@/lib/api/public";

/** Meneruskan gambar hero unggahan dari backend, karena browser tidak pernah memanggil backend langsung. */
export async function GET() {
  let upstream: Response;
  try {
    upstream = await getHeroImage();
  } catch {
    return new Response(null, { status: 502 });
  }
  if (!upstream.ok) {
    return new Response(null, { status: upstream.status === 404 ? 404 : 502 });
  }
  return new Response(upstream.body, {
    // Landing memanggil /hero-image?v=<imageUpdatedAt>; gambar baru = URL baru, jadi aman di-cache lama.
    headers: { "Content-Type": "image/png", "Cache-Control": "public, max-age=2592000, immutable" },
  });
}
