package com.aris.templateapp.upload.marking;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Memberi nomor unik {@code data-tpl-id} pada setiap elemen HTML asli (alur-fitur-upload.md bagian 7.8).
 * <p>
 * Nomor diberikan berurutan menurut urutan elemen di dokumen hasil parse jsoup. Karena parse jsoup selalu sama untuk
 * HTML yang sama, server bisa menemukan elemen yang sama lagi saat Kirim (menyisipkan data-key), walaupun JavaScript
 * provider mengubah halaman di HP. Elemen tanpa nomor di HP pasti dibuat JavaScript.
 */
public final class TemplateNumbering {

    public static final String ATTRIBUTE = "data-tpl-id";

    private TemplateNumbering() {
    }

    public static Document parse(String html) {
        Document doc = Jsoup.parse(html, "", Parser.htmlParser());
        // Tanpa pretty print agar spasi dan baris asli tidak berubah (spasi bisa memengaruhi tampilan inline-block).
        doc.outputSettings().prettyPrint(false);
        return doc;
    }

    /** Dokumen dengan nomor di setiap elemen (mulai 1). */
    public static Document number(String html) {
        Document doc = parse(html);
        int next = 1;
        for (Element element : doc.getAllElements()) {
            if (element == doc) {
                continue;
            }
            element.attr(ATTRIBUTE, String.valueOf(next++));
        }
        return doc;
    }

    /** Atribut yang isinya diisi runtime OrgSpace-Hosting saat website tayang (site-runtime.ts). */
    private static final List<String> FILLED = List.of("data-ws-field", "data-ws-stat", "data-ws-org");
    /** Atribut yang href-nya diatur runtime. */
    private static final List<String> LINKED = List.of("data-ws-link", "data-ws-auth");
    /** Semua penanda modul; elemen yang membungkusnya tidak boleh diganti seluruh isinya. */
    private static final List<String> MODULE = List.of("data-ws-list", "data-ws-collection", "data-ws-item", "data-ws-empty",
            "data-ws-form", "data-ws-field", "data-ws-link", "data-ws-stat", "data-ws-org", "data-ws-auth");

    /**
     * Elemen yang bersinggungan dengan modul dinamis, per nomor elemen → atribut penyebabnya (mis.
     * {@code data-ws-org="nama"}), untuk {@link MarkingValidator#validateDynamic}.
     *
     * @param filled elemen (atau anak dari elemen) yang isinya diisi otomatis
     * @param linked elemen yang href-nya diatur otomatis
     * @param wraps  elemen yang membungkus penanda modul
     */
    public record Dynamic(Map<Integer, String> filled, Map<Integer, String> linked, Map<Integer, String> wraps) {
    }

    public static Dynamic dynamic(String html) {
        Map<Element, Integer> numbers = new IdentityHashMap<>();
        Map<Integer, String> filled = new HashMap<>();
        Map<Integer, String> linked = new HashMap<>();
        Map<Integer, String> wraps = new HashMap<>();
        int next = 1;
        for (Element element : parse(html).getAllElements()) {
            if (element.parent() == null) {
                continue; // dokumen itu sendiri
            }
            int id = next++;
            numbers.put(element, id);
            Integer parent = numbers.get(element.parent());
            String own = firstAttribute(element, FILLED);
            String inherited = own != null ? own : parent == null ? null : filled.get(parent);
            if (inherited != null) {
                filled.put(id, inherited);
            }
            String link = firstAttribute(element, LINKED);
            if (link != null) {
                linked.put(id, link);
            }
            String module = firstAttribute(element, MODULE);
            // getAllElements berurutan dari luar ke dalam, jadi semua pembungkus sudah bernomor.
            for (Element up = element.parent(); module != null && numbers.containsKey(up); up = up.parent()) {
                wraps.putIfAbsent(numbers.get(up), module);
            }
        }
        return new Dynamic(filled, linked, wraps);
    }

    private static String firstAttribute(Element element, List<String> names) {
        for (String name : names) {
            if (element.hasAttr(name)) {
                String value = element.attr(name);
                return value.isEmpty() ? name : name + "=\"" + value + "\"";
            }
        }
        return null;
    }

    /**
     * Induk setiap elemen: nomor elemen → nomor elemen pembungkusnya (null untuk elemen paling luar). Dipakai untuk
     * menolak tandaan bersarang ({@link MarkingValidator#validateNesting}).
     */
    public static Map<Integer, Integer> parents(String html) {
        Map<Element, Integer> numbers = new IdentityHashMap<>();
        Map<Integer, Integer> parents = new HashMap<>();
        Document doc = parse(html);
        int next = 1;
        for (Element element : doc.getAllElements()) {
            if (element == doc) {
                continue;
            }
            int id = next++;
            numbers.put(element, id);
            parents.put(id, numbers.get(element.parent()));
        }
        return parents;
    }
}
