import { NextResponse, type NextRequest } from "next/server";
import {
  ACCESS_COOKIE,
  REFRESH_COOKIE,
  REFRESH_MAX_AGE_SECONDS,
  cookieOptions,
  secondsUntilExpiry,
} from "@/lib/auth/cookies";
import { refreshTokens } from "@/lib/auth/refresh";

/** Access token diperbarui jika sisa umurnya kurang dari ini. */
const REFRESH_BEFORE_SECONDS = 60;

/**
 * Menjaga sesi dashboard: access token backend hanya 15 menit, jadi sebelum halaman /dashboard dirender, token yang
 * hampir habis diperbarui di sini. Server Component tidak boleh menulis cookie, karena itu tugas ini ada di proxy.
 */
export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const isLogin = pathname === "/dashboard/login";
  const access = request.cookies.get(ACCESS_COOKIE)?.value;
  const refresh = request.cookies.get(REFRESH_COOKIE)?.value;

  if (secondsUntilExpiry(access) > REFRESH_BEFORE_SECONDS) {
    return NextResponse.next();
  }
  if (!refresh) {
    return isLogin ? NextResponse.next() : redirectToLogin(request);
  }
  const tokens = await refreshTokens(refresh);
  if (!tokens) {
    const response = isLogin ? NextResponse.next() : redirectToLogin(request, "expired");
    response.cookies.delete(ACCESS_COOKIE);
    response.cookies.delete(REFRESH_COOKIE);
    return response;
  }
  // Token baru dititipkan ke request (agar halaman yang sedang dirender memakainya) dan ke browser (Set-Cookie).
  request.cookies.set(ACCESS_COOKIE, tokens.accessToken);
  request.cookies.set(REFRESH_COOKIE, tokens.refreshToken);
  const response = NextResponse.next({ request: { headers: request.headers } });
  response.cookies.set(ACCESS_COOKIE, tokens.accessToken, cookieOptions(tokens.expiresIn));
  response.cookies.set(REFRESH_COOKIE, tokens.refreshToken, cookieOptions(REFRESH_MAX_AGE_SECONDS));
  return response;
}

function redirectToLogin(request: NextRequest, reason?: string) {
  const url = new URL("/dashboard/login", request.url);
  if (reason) {
    url.searchParams.set("reason", reason);
  }
  return NextResponse.redirect(url);
}

export const config = {
  matcher: ["/dashboard/:path*"],
};
