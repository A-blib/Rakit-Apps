import { NextRequest, NextResponse } from "next/server";

/**
 * Halaman website user berjalan di sandbox (origin "null"), jadi form-nya memanggil route ini lintas origin.
 * Boleh dari mana saja karena tanpa cookie/token: sama dengan form publik biasa.
 */
const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
};

export function OPTIONS() {
  return new NextResponse(null, { status: 204, headers: CORS });
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ slug: string; formName: string }> }
) {
  const { slug, formName } = await context.params;
  const backendUrl = process.env.BACKEND_API_URL || "http://localhost:8080";

  try {
    const body = await request.json();
    const res = await fetch(
      `${backendUrl}/api/public/sites/${encodeURIComponent(slug)}/forms/${encodeURIComponent(formName)}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "User-Agent": request.headers.get("user-agent") || "OrgSpace-Hosting-Proxy",
          "X-Forwarded-For": request.headers.get("x-forwarded-for") || "127.0.0.1",
        },
        body: JSON.stringify(body),
      }
    );

    const json = await res.json();
    return NextResponse.json(json, { status: res.status, headers: CORS });
  } catch (err: any) {
    console.error("Gagal proxy form submission:", err);
    return NextResponse.json(
      { success: false, message: "Terjadi kesalahan saat memproses formulir." },
      { status: 500, headers: CORS }
    );
  }
}
