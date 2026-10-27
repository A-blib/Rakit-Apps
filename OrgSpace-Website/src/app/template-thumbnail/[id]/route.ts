import { getTemplateThumbnail } from "@/lib/api/public";

/** Meneruskan thumbnail template dari backend, karena browser tidak pernah memanggil backend langsung. */
export async function GET(_: Request, { params }: RouteContext<"/template-thumbnail/[id]">) {
  const { id } = await params;
  let upstream: Response;
  try {
    upstream = await getTemplateThumbnail(id);
  } catch {
    return new Response(null, { status: 502 });
  }
  if (!upstream.ok) {
    // id aneh ditolak backend (4xx): cukup 404, tanpa meneruskan isi error backend.
    return new Response(null, { status: upstream.status < 500 ? 404 : 502 });
  }
  return new Response(upstream.body, {
    // URL membawa ?v= dari backend; thumbnail baru = URL baru, jadi aman di-cache lama.
    headers: {
      "Content-Type": upstream.headers.get("Content-Type") ?? "image/webp",
      "Cache-Control": "public, max-age=2592000, immutable",
    },
  });
}
