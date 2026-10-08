package com.aris.templateapp.upload.marking;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.util.HashMap;
import java.util.IdentityHashMap;
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
