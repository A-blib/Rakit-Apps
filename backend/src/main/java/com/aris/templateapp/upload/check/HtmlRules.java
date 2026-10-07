package com.aris.templateapp.upload.check;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bagian 5.6 C (HTML) ditambah link luar yang disembunyikan (bagian 5.6 F). */
final class HtmlRules {

    private static final List<String> RESERVED = List.of("data-edit", "data-key", "data-label", "data-tpl-id");
    // Elemen yang biasanya berisi teks yang bisa ditandai.
    private static final String TEXT_ELEMENTS = "h1, h2, h3, h4, h5, h6, p, li, a, button, span, blockquote, "
            + "figcaption, td, th, label, dt, dd, strong, em, small";
    private static final Pattern REFRESH_URL = Pattern.compile("url\\s*=\\s*['\"]?([^'\"\\s;]+)", Pattern.CASE_INSENSITIVE);
    // Cara menyembunyikan link titipan (backlink) pada elemen link itu sendiri.
    private static final Pattern HIDDEN_SELF = Pattern.compile(
            "display\\s*:\\s*none|visibility\\s*:\\s*hidden|opacity\\s*:\\s*0(\\.0+)?\\s*(;|!|$)"
                    + "|font-size\\s*:\\s*0(px|em|rem|%)?\\s*(;|!|$)|(?<![-\\w])(width|height)\\s*:\\s*0(px)?\\s*(;|!|$)"
                    + "|(left|top|text-indent)\\s*:\\s*-\\d{4,}", Pattern.CASE_INSENSITIVE);
    // Pada pembungkus, display:none tidak dihitung karena menu HP/dropdown memang sering disembunyikan dulu.
    private static final Pattern HIDDEN_ANCESTOR = Pattern.compile(
            "font-size\\s*:\\s*0(px|em|rem|%)?\\s*(;|!|$)|(?<![-\\w])(width|height)\\s*:\\s*0(px)?\\s*(;|!|$)"
                    + "|(left|top|text-indent)\\s*:\\s*-\\d{4,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern SVELTE_CLASS = Pattern.compile("svelte-[a-z0-9]{5,}");
    private static final Pattern DYNAMIC_HTML = Pattern.compile("\\.innerHTML\\s*[+]?=|insertAdjacentHTML\\s*\\(");

    /** Jejak framework komponen (bagian 5.6 C & 10.6): selector → nama framework. */
    private static final Map<String, String> FRAMEWORK_MARKERS = new LinkedHashMap<>();

    static {
        FRAMEWORK_MARKERS.put("script#__NEXT_DATA__, div#__next", "Next.js");
        FRAMEWORK_MARKERS.put("div#___gatsby", "Gatsby");
        FRAMEWORK_MARKERS.put("div#__nuxt, div#__layout", "Nuxt");
        FRAMEWORK_MARKERS.put("[data-reactroot], [data-reactid]", "React");
        FRAMEWORK_MARKERS.put("astro-island", "Astro island (React/Vue/Svelte)");
        FRAMEWORK_MARKERS.put("[data-server-rendered], [data-v-app]", "Vue");
        FRAMEWORK_MARKERS.put("[data-sveltekit-preload-data], [data-sveltekit-hydrate]", "SvelteKit");
    }

    private final CheckContext ctx;

    HtmlRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        boolean frameworkFound = false;
        for (String page : ctx.files.pages()) {
            Document doc = ctx.pages.get(page);
            byte[] bytes = ctx.files.content(page);
            if (bytes == null) {
                continue; // terlalu besar, sudah dilaporkan FILE_TOO_LARGE
            }
            if (isUnreadable(bytes)) {
                ctx.findings.add(CheckRule.HTML_UNREADABLE, page + " kosong atau tidak bisa dibaca.", page, null,
                        "Pastikan file ini berisi HTML yang disimpan sebagai teks UTF-8.");
                continue;
            }
            if (!frameworkFound) {
                frameworkFound = checkFramework(page, doc);
            }
            checkJsRendered(page, doc);
            checkPartialJs(page, doc);
            checkForms(page, doc);
            checkRedirect(page, doc);
            checkHead(page, doc, bytes);
            checkImages(page, doc);
            checkReserved(page, doc);
            checkForbiddenTags(page, doc);
            checkDuplicateIds(page, doc);
            checkHiddenLinks(page, doc);
        }
        checkDynamicHtmlInScripts();
        checkEditableCount();
    }

    private static boolean isUnreadable(byte[] bytes) {
        String text = Texts.decode(bytes);
        if (text.isBlank()) {
            return true;
        }
        long nulls = text.chars().filter(c -> c == 0).count();
        return nulls > 0 && nulls * 20 > text.length(); // file biner yang diberi nama .html
    }

    private boolean checkFramework(String page, Document doc) {
        for (Map.Entry<String, String> marker : FRAMEWORK_MARKERS.entrySet()) {
            Element found = doc.selectFirst(marker.getKey());
            if (found != null) {
                reportFramework(page, marker.getValue(), CheckContext.line(found), found.outerHtml());
                return true;
            }
        }
        String html = ctx.text(page);
        for (String token : List.of("window.__NUXT__", "__sveltekit_", "/_next/static/", "/_nuxt/")) {
            int i = html.indexOf(token);
            if (i >= 0) {
                reportFramework(page, token.contains("next") ? "Next.js" : token.contains("nuxt") || token.contains("NUXT")
                        ? "Nuxt" : "SvelteKit", Texts.lineOf(html, i), html.substring(i, Math.min(html.length(), i + 60)));
                return true;
            }
        }
        // Svelte menambahkan class ber-hash "svelte-xxxxxx" pada elemen hasil komponen.
        for (Element el : doc.select("[class*=svelte-]")) {
            if (el.classNames().stream().anyMatch(c -> SVELTE_CLASS.matcher(c).matches())) {
                reportFramework(page, "Svelte", CheckContext.line(el), el.outerHtml());
                return true;
            }
        }
        return false;
    }

    private void reportFramework(String page, String name, Integer line, String snippet) {
        ctx.findings.add(CheckRule.COMPONENT_FRAMEWORK, "Terdeteksi jejak " + name + " di " + page + ". Template React/Vue/Svelte belum didukung.",
                page, line, "Gunakan HTML, CSS, dan JS biasa.", snippet);
    }

    /**
     * Ditentukan server secara statis (bagian 5.1): body HTML asli hampir kosong (teks dan gambar di bawah
     * ambang) padahal ada script, artinya isi halaman baru dibuat JavaScript saat dibuka.
     */
    private void checkJsRendered(String page, Document doc) {
        Element body = doc.body().clone();
        body.select("script, style, noscript, template").remove();
        int textLength = body.text().strip().length();
        int images = body.select("img, picture, svg, video[poster]").size();
        boolean hasScript = !doc.select("script").isEmpty();
        if (hasScript && textLength < ctx.limits().jsBodyMinTextChars() && images < ctx.limits().jsBodyMinImages()) {
            ctx.findings.add(CheckRule.JS_RENDERED_CONTENT, page + " hampir kosong (" + textLength
                            + " karakter teks, tanpa gambar); isinya baru dibuat JavaScript saat dibuka.", page, null,
                    "Tulis teks dan gambar langsung di HTML. Hasil build SPA (React/Vue/Svelte) belum didukung.");
        }
    }

    /** Alpine x-text/x-html/x-for: teksnya ada di JavaScript, jadi bagian itu tidak bisa ditandai (Peringatan). */
    private void checkPartialJs(String page, Document doc) {
        Elements dynamic = doc.select("[x-text], [x-html], template[x-for], [v-text], [v-html]");
        if (!dynamic.isEmpty()) {
            Element first = dynamic.first();
            ctx.findings.add(CheckRule.JS_PARTIAL_CONTENT, dynamic.size() + " bagian di " + page
                            + " diisi oleh JavaScript (mis. x-text) dan tidak bisa ditandai.", page, CheckContext.line(first),
                    "Tulis isinya langsung di HTML jika ingin bisa diedit pembuat website.", first.outerHtml());
        }
    }

    private void checkDynamicHtmlInScripts() {
        for (CheckContext.JsSource js : ctx.ownScripts()) {
            if (js.file().endsWith(".min.js")) {
                continue; // file .min.js hampir selalu library; yang dicari adalah konten buatan provider sendiri
            }
            String code = JsScanner.stripComments(js.code());
            Matcher m = DYNAMIC_HTML.matcher(code);
            if (m.find()) {
                ctx.findings.add(CheckRule.JS_PARTIAL_CONTENT, js.file() + " membuat isi halaman lewat JavaScript "
                                + "(innerHTML); bagian itu tidak bisa ditandai.", js.file(), lineIn(js, code, m.start()),
                        "Tulis isinya langsung di HTML jika ingin bisa diedit pembuat website.", snippetAt(code, m.start()));
            }
        }
    }

    private void checkForms(String page, Document doc) {
        for (Element el : doc.select("form[action], [formaction]")) {
            String action = el.hasAttr("formaction") ? el.attr("formaction") : el.attr("action");
            Ref ref = Ref.of(page, action);
            if (ref.isExternal()) {
                ctx.findings.add(CheckRule.FORM_EXTERNAL_ACTION, "Form di " + page + " mengirim data ke " + ref.host() + ".",
                        page, CheckContext.line(el),
                        "Demi melindungi data pengunjung, pakai link WhatsApp (https://wa.me/...) atau mailto: sebagai ganti form.",
                        action);
            }
        }
    }

    private void checkRedirect(String page, Document doc) {
        for (Element meta : doc.select("meta[http-equiv]")) {
            if (!meta.attr("http-equiv").equalsIgnoreCase("refresh")) {
                continue;
            }
            Matcher m = REFRESH_URL.matcher(meta.attr("content"));
            if (m.find() && Ref.of(page, m.group(1)).isExternal()) {
                ctx.findings.add(CheckRule.AUTO_REDIRECT, page + " berpindah otomatis ke " + Ref.hostOf(m.group(1)) + ".",
                        page, CheckContext.line(meta), "Hapus meta refresh ini; template tidak boleh membawa pengunjung ke situs lain.",
                        meta.outerHtml());
            }
        }
    }

    private void checkHead(String page, Document doc, byte[] bytes) {
        if (doc.selectFirst("meta[name=viewport]") == null) {
            ctx.findings.add(CheckRule.NO_VIEWPORT, page + " tidak punya <meta name=\"viewport\">.", page, null,
                    "Tambahkan <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"> di <head>.");
        }
        Element title = doc.selectFirst("title");
        if (title == null || title.text().isBlank()) {
            ctx.findings.add(CheckRule.NO_TITLE, page + " tidak punya judul <title>.", page, null,
                    "Tambahkan <title>Nama website</title> di <head>.");
        }

        String charset = null;
        Element metaCharset = doc.selectFirst("meta[charset]");
        if (metaCharset != null) {
            charset = metaCharset.attr("charset");
        } else {
            for (Element meta : doc.select("meta[http-equiv][content]")) {
                String content = meta.attr("content").toLowerCase(Locale.ROOT);
                int i = content.indexOf("charset=");
                if (meta.attr("http-equiv").equalsIgnoreCase("content-type") && i >= 0) {
                    charset = content.substring(i + 8).strip();
                }
            }
        }
        boolean utf8Bytes = Texts.isValidUtf8(bytes);
        if (charset == null || !charset.toLowerCase(Locale.ROOT).replace("_", "-").equals("utf-8") || !utf8Bytes) {
            String why = !utf8Bytes ? "File " + page + " tidak disimpan sebagai UTF-8."
                    : charset == null ? page + " tidak punya <meta charset>." : page + " memakai charset " + charset + ".";
            ctx.findings.add(CheckRule.CHARSET, why + " Huruf bisa rusak.", page, null,
                    "Simpan file sebagai UTF-8 dan tambahkan <meta charset=\"utf-8\"> di <head>.");
        }

        Element html = doc.selectFirst("html");
        boolean hasLang = html != null && !html.attr("lang").isBlank();
        if (doc.documentType() == null || !hasLang) {
            ctx.findings.add(CheckRule.DOCTYPE_LANG, page + (doc.documentType() == null ? " tidak diawali <!DOCTYPE html>."
                    : " tidak punya atribut lang di <html>."), page, null,
                    "Awali file dengan <!DOCTYPE html> dan tulis <html lang=\"id\">.");
        }
    }

    private void checkImages(String page, Document doc) {
        Elements noAlt = doc.select("img:not([alt])");
        if (!noAlt.isEmpty()) {
            ctx.findings.add(CheckRule.IMG_NO_ALT, noAlt.size() + " gambar di " + page + " tanpa atribut alt.", page,
                    CheckContext.line(noAlt.first()),
                    "Tambahkan alt berisi keterangan gambar, mis. alt=\"Foto kue cokelat\".", noAlt.first().outerHtml());
        }
        for (Element img : doc.select("img[src^=data:image]")) {
            long bytes = img.attr("src").length() * 3L / 4;
            if (bytes > ctx.limits().base64WarnBytes()) {
                ctx.findings.add(CheckRule.BASE64_IMAGE, "Gambar " + Texts.size(bytes) + " ditanam langsung di " + page + ".",
                        page, CheckContext.line(img), "Simpan gambar sebagai file terpisah (mis. img/foto.webp) agar HTML ringan.");
            }
        }
    }

    private void checkReserved(String page, Document doc) {
        for (String attr : RESERVED) {
            for (Element el : doc.select("[" + attr + "]")) {
                ctx.findings.add(CheckRule.RESERVED_ATTRIBUTE, page + " memakai atribut " + attr + " yang khusus dipakai app.",
                        page, CheckContext.line(el), "Ganti nama atribut ini (mis. data-" + attr.substring(5) + "-x).",
                        el.outerHtml());
            }
        }
    }

    private void checkForbiddenTags(String page, Document doc) {
        for (Element base : doc.select("base[href]")) {
            ctx.findings.add(CheckRule.BASE_HREF, page + " punya <base href> yang membuat file diambil dari tempat lain, bukan dari ZIP.",
                    page, CheckContext.line(base), "Hapus baris <base href=...> ini.", base.outerHtml());
        }
        for (Element plugin : doc.select("object, embed, applet")) {
            ctx.findings.add(CheckRule.PLUGIN_TAG, page + " memakai <" + plugin.tagName() + "> (teknologi plugin lama).",
                    page, CheckContext.line(plugin), "Ganti dengan <img>, <video>, atau <iframe> YouTube/Google Maps.",
                    plugin.outerHtml());
        }
    }

    private void checkDuplicateIds(String page, Document doc) {
        Map<String, Element> firstById = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        Element firstDuplicate = null;
        for (Element el : doc.select("[id]")) {
            String id = el.id();
            if (id.isBlank()) {
                continue;
            }
            if (firstById.putIfAbsent(id, el) != null && !duplicates.contains(id)) {
                duplicates.add(id);
                if (firstDuplicate == null) {
                    firstDuplicate = el;
                }
            }
        }
        if (!duplicates.isEmpty()) {
            ctx.findings.add(CheckRule.DUPLICATE_ID, "id dipakai lebih dari sekali di " + page + ": "
                            + String.join(", ", duplicates.subList(0, Math.min(3, duplicates.size()))) + ".", page,
                    CheckContext.line(firstDuplicate), "Buat setiap id unik dalam satu halaman agar menu dan JavaScript tidak salah sasaran.");
        }
    }

    private void checkHiddenLinks(String page, Document doc) {
        for (Element a : doc.select("a[href]")) {
            Ref ref = Ref.of(page, a.attr("href"));
            if (!ref.isExternal()) {
                continue;
            }
            boolean hidden = a.hasAttr("hidden") || HIDDEN_SELF.matcher(a.attr("style")).find();
            for (Element parent = a.parent(); !hidden && parent != null; parent = parent.parent()) {
                hidden = HIDDEN_ANCESTOR.matcher(parent.attr("style")).find();
            }
            if (hidden) {
                ctx.findings.add(CheckRule.HIDDEN_EXTERNAL_LINK, "Link ke " + ref.host() + " di " + page + " disembunyikan.",
                        page, CheckContext.line(a), "Hapus link tersembunyi ini. Link titipan di website orang lain tidak diizinkan.",
                        a.outerHtml());
            }
        }
    }

    /** Kasus ekstrem (bagian 5.6 C): seluruh template hampir tanpa teks/gambar yang bisa ditandai. */
    private void checkEditableCount() {
        if (ctx.findings.has(CheckRule.JS_RENDERED_CONTENT) || ctx.pages.isEmpty()) {
            return; // sudah dijelaskan aturan "konten dibuat JS"
        }
        int count = 0;
        for (Document doc : ctx.pages.values()) {
            for (Element el : doc.body().select(TEXT_ELEMENTS)) {
                if (el.ownText().strip().length() >= 2) {
                    count++;
                }
            }
            count += doc.body().select("img[src]").size();
        }
        if (count < ctx.limits().minEditableElements()) {
            ctx.findings.add(CheckRule.TOO_FEW_EDITABLE, "Seluruh template hanya punya " + count
                            + " teks/gambar yang bisa ditandai, minimal " + ctx.limits().minEditableElements() + ".", null, null,
                    "Tambahkan judul, paragraf, atau gambar langsung di HTML.");
        }
    }

    static Integer lineIn(CheckContext.JsSource js, String code, int index) {
        int line = Texts.lineOf(code, index);
        return js.line() != null ? js.line() + line - 1 : line;
    }

    static String snippetAt(String code, int index) {
        int end = code.indexOf('\n', index);
        return code.substring(index, end < 0 ? Math.min(code.length(), index + 120) : Math.min(end, index + 120));
    }
}
