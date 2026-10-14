import { NextRequest, NextResponse } from "next/server";

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
    return NextResponse.json(json, { status: res.status });
  } catch (err: any) {
    console.error("Gagal proxy form submission:", err);
    return NextResponse.json(
      { success: false, message: "Terjadi kesalahan saat memproses formulir." },
      { status: 500 }
    );
  }
}
