package com.aris.templateapp.site;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * IP pengunjung website untuk kiriman formulir. Jika OrgSpace-Hosting berjalan di Vercel, request datang dari server
 * Vercel (IP-nya sama untuk semua pengunjung), jadi Hosting mengirim IP asli di header {@code X-OrgSpace-Visitor-IP}
 * beserta kunci rahasia bersama ({@code HOSTING_SHARED_KEY}). Header itu hanya dipercaya jika kuncinya cocok; tanpa
 * kunci dipakai IP request biasa (Hosting/Caddy di VPS sudah diteruskan lewat X-Forwarded-For).
 */
@Component
public class VisitorIp {

    static final String IP_HEADER = "X-OrgSpace-Visitor-IP";
    static final String KEY_HEADER = "X-OrgSpace-Hosting-Key";
    private static final Pattern IP = Pattern.compile("^[0-9A-Fa-f:.]{2,45}$");

    private final byte[] key;

    public VisitorIp(@Value("${app.sites.hosting-key:}") String key) {
        this.key = key == null ? new byte[0] : key.strip().getBytes(StandardCharsets.UTF_8);
    }

    public String of(HttpServletRequest request) {
        String sent = request.getHeader(KEY_HEADER);
        String ip = request.getHeader(IP_HEADER);
        boolean trusted = key.length > 0 && sent != null
                && MessageDigest.isEqual(key, sent.getBytes(StandardCharsets.UTF_8));
        return trusted && ip != null && IP.matcher(ip.strip()).matches() ? ip.strip() : request.getRemoteAddr();
    }
}
