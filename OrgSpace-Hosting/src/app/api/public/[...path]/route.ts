import { NextRequest, NextResponse } from "next/server";
import { BACKEND_API_URL } from "@/lib/config";

export const dynamic = "force-dynamic";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
};

export async function OPTIONS() {
  return new NextResponse(null, { status: 204, headers: CORS_HEADERS });
}

async function proxy(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  const pathStr = path.map(encodeURIComponent).join("/");
  const search = request.nextUrl.search;
  const targetUrl = `${BACKEND_API_URL}/api/public/${pathStr}${search}`;

  const headers = new Headers();
  request.headers.forEach((value, key) => {
    const lower = key.toLowerCase();
    if (!["host", "connection", "content-length"].includes(lower)) {
      headers.set(key, value);
    }
  });

  const init: RequestInit = {
    method: request.method,
    headers,
  };

  if (["POST", "PUT", "PATCH"].includes(request.method)) {
    const bodyBuffer = await request.arrayBuffer();
    if (bodyBuffer.byteLength > 0) {
      init.body = bodyBuffer;
    }
  }

  try {
    const res = await fetch(targetUrl, init);
    const resHeaders = new Headers(CORS_HEADERS);
    res.headers.forEach((val, key) => {
      const lower = key.toLowerCase();
      if (!["content-encoding", "transfer-encoding", "access-control-allow-origin"].includes(lower)) {
        resHeaders.set(key, val);
      }
    });

    const responseBody = await res.arrayBuffer();
    return new NextResponse(responseBody, {
      status: res.status,
      statusText: res.statusText,
      headers: resHeaders,
    });
  } catch (err: any) {
    console.error(`[API Proxy Error] Gagal proxy ke ${targetUrl}:`, err);
    return NextResponse.json(
      { code: "GATEWAY_ERROR", message: "Gagal menghubungi server backend.", details: err?.message },
      { status: 502, headers: CORS_HEADERS }
    );
  }
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const DELETE = proxy;
export const PATCH = proxy;
