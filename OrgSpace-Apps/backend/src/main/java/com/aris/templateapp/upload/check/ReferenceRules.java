package com.aris.templateapp.upload.check;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bagian 5.6 D: file yang dirujuk dan hubungan antar-halaman. */
final class ReferenceRules {

    // import/export lokal di JS modul: import x from './a.js', export * from '../b.js', import('./c.js').
    private static final Pattern LOCAL_IMPORT = Pattern.compile(
            "(?:^|[;\\s])(?:import|export)\\s+(?:[\\w*{}\\s,$]+\\s+from\\s+)?['\"]((?:\\.{1,2}/|/)[^'\"]+)['\"]"
                    + "|import\\s*\\(\\s*['\"]((?:\\.{1,2}/|/)[^'\"]+)['\"]\\s*\\)");

    private final CheckContext ctx;
    private final Set<String> linkedPages = new HashSet<>();

    ReferenceRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        for (Map.Entry<String, Document> entry : ctx.pages.entrySet()) {
            String page = entry.getKey();
            Document doc = entry.getValue();
            Set<String> assets = ctx.pageAssets.computeIfAbsent(page, k -> new LinkedHashSet<>());
            int rootAbsolute = 0;
            Element firstRootAbsolute = null;

            for (Element el : doc.select("script[src], link[href], img[src], img[srcset], source[src], source[srcset], "
                    + "video[poster], input[type=image][src], a[href], iframe[src]")) {
                for (String url : urlsOf(el)) {
                    Ref ref = Ref.of(page, url);
                    if (ref.isLocal() && ref.rootAbsolute()) {
                        rootAbsolute++;
                        if (firstRootAbsolute == null) {
                            firstRootAbsolute = el;
                        }
                    }
                    checkElementRef(page, doc, el, ref, assets);
                }
            }

            if (rootAbsolute > 0) {
                ctx.findings.add(CheckRule.ROOT_ABSOLUTE_PATH, rootAbsolute + " path di " + page
                                + " diawali / (mis. /assets/index.js); bisa rusak jika website dipasang di subfolder.", page,
                        CheckContext.line(firstRootAbsolute),
                        "Pakai path relatif. Untuk Vite, tambahkan base: './' di vite.config.js lalu build ulang.",
                        firstRootAbsolute.outerHtml());
            }
        }
        checkModuleImports();
        checkOrphanPages();
    }

    private static Set<String> urlsOf(Element el) {
        Set<String> urls = new LinkedHashSet<>();
        for (String attr : new String[]{"src", "href", "poster"}) {
            if (el.hasAttr(attr)) {
                urls.add(el.attr(attr));
            }
        }
        if (el.hasAttr("srcset")) {
            for (String candidate : el.attr("srcset").split(",")) {
                String url = candidate.strip().split("\\s+")[0];
                if (!url.isEmpty()) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    private void checkElementRef(String page, Document doc, Element el, Ref ref, Set<String> assets) {
        Integer line = CheckContext.line(el);
        String tag = el.tagName();
        String rel = el.attr("rel").toLowerCase(Locale.ROOT);
        String as = el.attr("as").toLowerCase(Locale.ROOT);
        boolean isAsset = tag.equals("script")
                || (tag.equals("link") && (rel.contains("stylesheet") || rel.contains("modulepreload")
                || (rel.contains("preload") && (as.equals("script") || as.equals("style")))));
        boolean isImage = tag.equals("img") || tag.equals("source") || tag.equals("video") || tag.equals("input")
                || (tag.equals("link") && (rel.contains("icon") || (rel.contains("preload") && (as.equals("image") || as.equals("font")))));
        boolean isPageLink = tag.equals("a") || tag.equals("iframe");

        if (ref.isExternal()) {
            if (tag.equals("img") || tag.equals("source")) {
                ctx.findings.add(CheckRule.HOTLINK_IMAGE, "Gambar di " + page + " diambil langsung dari " + ref.host()
                        + "; bisa hilang sewaktu-waktu.", page, line, "Simpan gambarnya di ZIP (mis. folder img/).", el.outerHtml());
            }
            return;
        }
        if (ref.kind() == Ref.Kind.FRAGMENT && isPageLink) {
            checkAnchor(page, doc, page, ref.fragment(), el);
            return;
        }
        if (!ref.isLocal()) {
            return;
        }

        if (isAsset || isImage) {
            CheckContext.Lookup found = ctx.lookup(ref, page, line);
            if (found == CheckContext.Lookup.FOUND) {
                assets.add(ref.path());
            } else if (found == CheckContext.Lookup.MISSING) {
                if (isAsset) {
                    ctx.findings.add(CheckRule.MISSING_ASSET, page + (line != null ? " baris " + line : "") + " memanggil "
                                    + ref.raw() + ", tetapi file itu tidak ada di ZIP.", page, line,
                            "Sertakan file tersebut di ZIP atau perbaiki path-nya.", el.outerHtml());
                } else {
                    ctx.findings.add(CheckRule.MISSING_IMAGE, "Gambar " + ref.raw() + " di " + page + " tidak ada di ZIP.",
                            page, line, "Sertakan gambar tersebut di ZIP atau perbaiki path-nya.", el.outerHtml());
                }
            }
            return;
        }
        if (isPageLink && isHtmlTarget(ref.path())) {
            CheckContext.Lookup found = ctx.lookup(ref, page, line);
            if (found == CheckContext.Lookup.MISSING) {
                ctx.findings.add(CheckRule.BROKEN_PAGE_LINK, page + (line != null ? " baris " + line : "")
                                + " menautkan ke " + ref.raw() + ", tetapi halaman itu tidak ada.", page, line,
                        "Tambahkan halamannya ke ZIP atau perbaiki link-nya.", el.outerHtml());
            } else if (found == CheckContext.Lookup.FOUND) {
                if (!ref.path().equals(page)) {
                    linkedPages.add(ref.path());
                }
                if (ref.fragment() != null) {
                    checkAnchor(page, ctx.pages.get(ref.path()), ref.path(), ref.fragment(), el);
                }
            }
        }
    }

    private static boolean isHtmlTarget(String path) {
        if (path == null) {
            return true;
        }
        String ext = Texts.extension(path);
        return ext.equals("html") || ext.equals("htm");
    }

    /** Link {@code #kontak} ke id yang tidak ada hanya Peringatan: menu tidak menggulir, website tidak rusak. */
    private void checkAnchor(String page, Document target, String targetPage, String fragment, Element el) {
        if (target == null || fragment == null || fragment.isEmpty() || fragment.equalsIgnoreCase("top")
                || fragment.startsWith("!") || fragment.startsWith("/")) {
            return;
        }
        String id = Ref.percentDecode(fragment);
        if (target.getElementById(id) == null && target.select("a[name=" + cssEscape(id) + "]").isEmpty()) {
            ctx.findings.add(CheckRule.BROKEN_ANCHOR, "Link #" + id + " di " + page + " menuju id yang tidak ada di "
                            + targetPage + ".", page, CheckContext.line(el),
                    "Tambahkan id=\"" + id + "\" pada bagian tujuan, atau perbaiki link-nya.", el.outerHtml());
        }
    }

    private static String cssEscape(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** import antar file JS lokal boleh (bagian 10.1), asal file yang di-import ada. */
    private void checkModuleImports() {
        for (CheckContext.JsSource js : ctx.ownScripts()) {
            String code = JsScanner.stripComments(js.code());
            Matcher m = LOCAL_IMPORT.matcher(code);
            while (m.find()) {
                String spec = m.group(1) != null ? m.group(1) : m.group(2);
                Ref ref = Ref.of(js.file(), spec);
                Integer line = HtmlRules.lineIn(js, code, m.start());
                if (ctx.lookup(ref, js.file(), line) == CheckContext.Lookup.MISSING) {
                    ctx.findings.add(CheckRule.MISSING_ASSET, js.file() + " meng-import " + spec + ", tetapi file itu tidak ada di ZIP.",
                            js.file(), line, "Sertakan file tersebut di ZIP atau perbaiki path import-nya.",
                            HtmlRules.snippetAt(code, m.start()));
                }
            }
        }
    }

    /** Website satu halaman tanpa navigasi boleh; halaman lain yang tidak ditautkan sama sekali diberi Peringatan. */
    private void checkOrphanPages() {
        for (String page : ctx.pages.keySet()) {
            if (!page.equals("index.html") && !linkedPages.contains(page)) {
                ctx.findings.add(CheckRule.ORPHAN_PAGE, page + " tidak ditautkan dari halaman mana pun, sehingga tidak bisa dibuka dari menu.",
                        page, null, "Tambahkan link ke halaman ini (mis. di menu), atau hapus jika tidak dipakai.");
            }
        }
    }
}
