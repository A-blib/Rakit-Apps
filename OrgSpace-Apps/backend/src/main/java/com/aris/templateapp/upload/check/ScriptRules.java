package com.aris.templateapp.upload.check;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bagian 5.6 F: pemindaian sederhana JavaScript milik provider (file .js + script inline). File library terkenal
 * yang dikenali dari hash-nya tidak dipindai ulang. Semua pola dijalankan pada kode yang komentarnya sudah dibuang.
 */
final class ScriptRules {

    private static final String REMOTE_URL = "['\"`](?:https?:)?//";
    private static final Pattern EXT_REQUEST = Pattern.compile(
            "(?<![\\w$.])fetch\\s*\\(\\s*" + REMOTE_URL
                    + "|\\.open\\s*\\(\\s*['\"]\\w+['\"]\\s*,\\s*" + REMOTE_URL
                    + "|new\\s+WebSocket\\s*\\("
                    + "|sendBeacon\\s*\\(\\s*" + REMOTE_URL
                    + "|new\\s+EventSource\\s*\\(\\s*" + REMOTE_URL
                    + "|\\$\\.(?:ajax|get|post|getJSON)\\s*\\(\\s*" + REMOTE_URL
                    + "|axios(?:\\.\\w+)?\\s*\\(\\s*" + REMOTE_URL);
    private static final Pattern EVAL = Pattern.compile("(?<![\\w$.])eval\\s*\\(|new\\s+Function\\s*\\(");
    private static final Pattern SERVICE_WORKER = Pattern.compile("serviceWorker\\s*\\.\\s*register\\s*\\(");
    private static final Pattern PACKER = Pattern.compile(
            "eval\\s*\\(\\s*function\\s*\\(\\s*p\\s*,\\s*a\\s*,\\s*c\\s*,\\s*k\\s*,\\s*e\\s*,\\s*[dr]\\s*\\)");
    private static final Pattern OBFUSCATOR_NAME = Pattern.compile("_0x[0-9a-f]{4,}");
    private static final Pattern HEX_ESCAPE = Pattern.compile("\\\\x[0-9a-fA-F]{2}");
    private static final Pattern JSFUCK = Pattern.compile("\\[\\]\\[\\(!\\[\\]\\+\\[\\]\\)");
    private static final Pattern MINER = Pattern.compile(
            "coinhive|coin-hive|cryptonight|cryptoloot|webminepool|deepminer|minero\\.cc|coinimp|jsecoin"
                    + "|WebAssembly\\.instantiateStreaming\\s*\\(\\s*fetch\\s*\\(\\s*" + REMOTE_URL,
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STRING_TIMER = Pattern.compile("set(?:Timeout|Interval)\\s*\\(\\s*['\"`]");
    private static final Pattern DOCUMENT_WRITE = Pattern.compile("document\\.write(?:ln)?\\s*\\(");
    private static final Pattern POPUP = Pattern.compile("(?<![\\w$.])(?:window\\.)?(?:alert|confirm)\\s*\\(|window\\.open\\s*\\(");
    private static final Pattern REDIRECT = Pattern.compile(
            "(?<![\\w$])(?:window\\.|document\\.|top\\.|self\\.)?location(?:\\.href)?\\s*=\\s*" + REMOTE_URL
                    + "|location\\.(?:replace|assign)\\s*\\(\\s*" + REMOTE_URL);
    private static final Pattern CDN_IMPORT = Pattern.compile(
            "(?:^|[;\\s])import\\s+(?:[\\w*{}\\s,$]+\\s+from\\s+)?['\"]((?:https?:)?//[^'\"]+)['\"]"
                    + "|import\\s*\\(\\s*['\"`]((?:https?:)?//[^'\"`]+)['\"`]");
    private static final Pattern URL_LITERAL = Pattern.compile("['\"`]((?:https?:)?//[^'\"`\\s]+)['\"`]");
    // Konteks yang membuat kode berjalan otomatis saat halaman dibuka.
    private static final Pattern AUTO_CONTEXT = Pattern.compile(
            "setTimeout|DOMContentLoaded|['\"]load['\"]|onload|readyState");

    // Fungsi yang langsung dipanggil saat dibuat: (function () { ... })() atau !function () { ... }().
    private static final Pattern IIFE = Pattern.compile("^[\\s(!]*(?:async\\s+)?function\\s*\\w*\\s*\\([^)]*\\)\\s*$"
            + "|^\\s*\\(\\s*(?:async\\s*)?\\([^)]*\\)\\s*=>\\s*$");

    private final CheckContext ctx;

    ScriptRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        for (CheckContext.JsSource js : ctx.ownScripts()) {
            JsScanner.Result scan = JsScanner.scanJs(js.code());
            String code = scan.code();
            if (scan.error() != null) {
                Integer line = HtmlRules.lineIn(js, code, scan.errorIndex());
                ctx.findings.add(CheckRule.JS_SYNTAX, "Kemungkinan kesalahan penulisan di " + js.file() + " baris " + line
                                + ": " + scan.error() + " Script ini mungkin tidak jalan.", js.file(), line,
                        "Buka file di VS Code atau console browser untuk melihat letak kesalahannya.",
                        HtmlRules.snippetAt(code, scan.errorIndex()));
            }

            boolean obfuscated = checkObfuscation(js, code);
            first(js, code, EXT_REQUEST, CheckRule.EXT_FETCH, "%s mengirim/mengambil data ke situs lain.",
                    "Template harus statis. Hapus kode ini atau ambil data dari file lokal (mis. fetch('data/menu.json')).");
            if (!obfuscated) {
                first(js, code, EVAL, CheckRule.JS_EVAL, "%s memakai eval() atau new Function().",
                        "Hindari eval(); tulis kode langsung agar aman dan mudah diperiksa.");
            }
            first(js, code, SERVICE_WORKER, CheckRule.SERVICE_WORKER, "%s mendaftarkan Service Worker.",
                    "Hapus pendaftaran Service Worker; ia bisa terus mengendalikan website walaupun file sudah diganti.");
            first(js, code, MINER, CheckRule.CRYPTO_MINER, "%s berisi pola penambang kripto.",
                    "Hapus script ini.");
            first(js, code, STRING_TIMER, CheckRule.STRING_TIMER, "%s memakai setTimeout/setInterval dengan teks.",
                    "Pakai fungsi, mis. setTimeout(() => mulai(), 1000).");
            first(js, code, DOCUMENT_WRITE, CheckRule.DOCUMENT_WRITE, "%s memakai document.write().",
                    "Pakai document.createElement atau tulis isinya langsung di HTML.");
            first(js, code, POPUP, CheckRule.POPUP_ON_LOAD, "%s memunculkan popup (alert/confirm/window.open).",
                    "Popup mengganggu pengunjung; pakai elemen di halaman sebagai gantinya.");
            checkRedirect(js, code);
            checkImportsAndTrackers(js, code);
        }
    }

    private boolean checkObfuscation(CheckContext.JsSource js, String code) {
        Matcher packer = PACKER.matcher(code);
        boolean found = packer.find();
        int index = found ? packer.start() : -1;
        if (!found) {
            long names = OBFUSCATOR_NAME.matcher(code).results().count();
            long hex = HEX_ESCAPE.matcher(code).results().count();
            Matcher jsfuck = JSFUCK.matcher(code);
            // Kode yang hanya di-minify tetap boleh; yang dicari adalah ciri khas alat pengacak.
            found = names >= 20 || (hex >= 200 && hex * 4 * 20 > code.length()) || jsfuck.find();
            index = 0;
        }
        if (found) {
            Integer line = HtmlRules.lineIn(js, code, index);
            ctx.findings.add(CheckRule.OBFUSCATED_JS, js.file() + " berisi kode yang diacak sehingga tidak bisa diperiksa.",
                    js.file(), line, "Upload kode aslinya. Kode yang hanya di-minify tetap boleh.",
                    HtmlRules.snippetAt(code, index));
        }
        return found;
    }

    /** Hanya pindah halaman yang terjadi otomatis yang ditolak; tombol "Pesan via WhatsApp" tetap boleh. */
    private void checkRedirect(CheckContext.JsSource js, String code) {
        Matcher m = REDIRECT.matcher(code);
        while (m.find()) {
            if (runsAutomatically(code, m.start())) {
                Integer line = HtmlRules.lineIn(js, code, m.start());
                ctx.findings.add(CheckRule.AUTO_REDIRECT, js.file() + " memindahkan pengunjung otomatis ke situs lain.",
                        js.file(), line, "Hapus pemindahan otomatis ini; pakai link biasa yang diklik pengunjung.",
                        HtmlRules.snippetAt(code, m.start()));
                return;
            }
        }
    }

    /**
     * Perkiraan apakah kode di {@code index} berjalan sendiri saat halaman dibuka. Naik dari blok { } terdalam:
     * pembungkus load/DOMContentLoaded/setTimeout/IIFE berarti otomatis; fungsi lain (mis. handler klik) berarti
     * menunggu aksi pengunjung; blok biasa (if, for) diteruskan ke pembungkus di atasnya. Tingkat paling luar = otomatis.
     */
    private static boolean runsAutomatically(String code, int index) {
        int open = enclosingBrace(code, index);
        while (open >= 0) {
            int boundary = Math.max(Math.max(code.lastIndexOf(';', open - 1), code.lastIndexOf('{', open - 1)),
                    code.lastIndexOf('}', open - 1));
            String context = code.substring(boundary + 1, open);
            if (AUTO_CONTEXT.matcher(context).find() || IIFE.matcher(context).find()) {
                return true;
            }
            if (context.contains("function") || context.contains("=>")) {
                return false;
            }
            open = enclosingBrace(code, open);
        }
        return true;
    }

    private void checkImportsAndTrackers(CheckContext.JsSource js, String code) {
        Matcher m = CDN_IMPORT.matcher(code);
        while (m.find()) {
            String url = m.group(1) != null ? m.group(1) : m.group(2);
            Integer line = HtmlRules.lineIn(js, code, m.start());
            if (ExternalRules.matchesAny(url.toLowerCase(Locale.ROOT), ctx.settings.remoteDataPatterns(), false)) {
                ctx.findings.add(CheckRule.REMOTE_DATA_SDK, js.file() + " meng-import library yang mengambil data dari server.",
                        js.file(), line, "Template harus statis. Tulis datanya langsung di HTML.", HtmlRules.snippetAt(code, m.start()));
            } else {
                ctx.findings.add(CheckRule.JS_CDN_IMPORT, js.file() + " meng-import library dari CDN di dalam file JS ("
                                + Ref.hostOf(url) + ").", js.file(), line,
                        "Pakai <script src> di HTML, atau sertakan file library di ZIP.", HtmlRules.snippetAt(code, m.start()));
            }
        }

        Matcher urls = URL_LITERAL.matcher(code);
        boolean tracker = ExternalRules.matchesAny(code, ctx.settings.trackerPatterns(), true);
        while (!tracker && urls.find()) {
            tracker = ExternalRules.matchesAny(urls.group(1).toLowerCase(Locale.ROOT), ctx.settings.trackerPatterns(), false);
        }
        if (tracker) {
            ctx.findings.add(CheckRule.TRACKER, js.file() + " berisi kode analytics/pelacak.", js.file(), js.line(),
                    "Hapus kode pelacak. Template tidak boleh melacak pengunjung website orang lain.");
        }
    }

    private void first(CheckContext.JsSource js, String code, Pattern pattern, CheckRule rule, String message,
                       String suggestion) {
        Matcher m = pattern.matcher(code);
        if (m.find()) {
            ctx.findings.add(rule, String.format(message, js.file()), js.file(), HtmlRules.lineIn(js, code, m.start()),
                    suggestion, HtmlRules.snippetAt(code, m.start()));
        }
    }

    /** Posisi "{" terdekat yang belum ditutup sebelum {@code index}; -1 jika kode berada di tingkat paling luar. */
    private static int enclosingBrace(String code, int index) {
        int depth = 0;
        for (int i = index - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c == '}') {
                depth++;
            } else if (c == '{') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
        }
        return -1;
    }
}
