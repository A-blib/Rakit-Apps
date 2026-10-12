package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Membuka setiap halaman di WebView tersembunyi untuk mendeteksi section dan sidik jarinya. Dipakai untuk menawarkan
 * "Header ini sama di 4 halaman. Tandai sekali untuk semua halaman?" (alur-fitur-upload.md bagian 7.6) dan untuk
 * mengisi daftar section halaman yang belum dibuka.
 */
class PageScanner {

    /** Section satu halaman beserta sidik jarinya. */
    static final class ScannedSection {
        final SectionInfo info;
        String hash;
        List<Integer> ids = new ArrayList<>();

        ScannedSection(SectionInfo info) {
            this.info = info;
        }
    }

    static final class Fingerprint {
        String hash;
        List<Integer> ids;
    }

    interface Done {
        void onDone(Map<String, List<ScannedSection>> byPage);
    }

    private final OffscreenPage page;
    private final Map<String, List<ScannedSection>> result = new HashMap<>();
    private String markScript;

    PageScanner(OffscreenPage page) {
        this.page = page;
    }

    void scan(File siteRoot, List<String> allowedHosts, List<String> pages, Done done) {
        try {
            markScript = MarkWebView.readAsset(page.hostContext(), "upload/mark.js");
        } catch (IOException e) {
            done.onDone(result);
            return;
        }
        next(siteRoot, allowedHosts, new ArrayList<>(pages), done);
    }

    private void next(File siteRoot, List<String> allowedHosts, List<String> remaining, Done done) {
        if (remaining.isEmpty()) {
            page.destroy();
            done.onDone(result);
            return;
        }
        String current = remaining.remove(0);
        try {
            page.load(siteRoot, allowedHosts, current, OffscreenPage.MOBILE_WIDTH, () ->
                    page.evaluate(markScript, ignored -> page.evaluate("RakitMark.unstick()", ignored2 ->
                            page.evaluateAsset("upload/sections.js", value -> {
                                List<ScannedSection> sections = new ArrayList<>();
                                for (SectionInfo info : SectionInfo.parse(value)) {
                                    sections.add(new ScannedSection(info));
                                }
                                result.put(current, sections);
                                fingerprint(sections, 0, () -> next(siteRoot, allowedHosts, remaining, done));
                            }))));
        } catch (IOException e) {
            next(siteRoot, allowedHosts, remaining, done);
        }
    }

    private void fingerprint(List<ScannedSection> sections, int index, Runnable then) {
        if (index >= sections.size()) {
            then.run();
            return;
        }
        ScannedSection section = sections.get(index);
        if (section.info.tplId == null) {
            fingerprint(sections, index + 1, then);
            return;
        }
        page.evaluate("RakitMark.fingerprint(" + section.info.tplId + ")", value -> {
            Fingerprint fp = parse(value);
            if (fp != null) {
                section.hash = fp.hash;
                section.ids = fp.ids;
            }
            fingerprint(sections, index + 1, then);
        });
    }

    @Nullable
    private static Fingerprint parse(String evaluated) {
        try {
            String json = JsonParser.parseString(evaluated).getAsString();
            return new Gson().fromJson(json, Fingerprint.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Elemen yang sama di halaman lain: section dengan sidik jari sama, elemen di urutan yang sama.
     *
     * @return halaman → nomor elemen, tidak termasuk halaman asal
     */
    static Map<String, Integer> sameElementElsewhere(Map<String, List<ScannedSection>> byPage, String page, int tplId) {
        Map<String, Integer> matches = new HashMap<>();
        List<ScannedSection> own = byPage.get(page);
        if (own == null) {
            return matches;
        }
        for (ScannedSection section : own) {
            int index = section.ids.indexOf(tplId);
            if (index < 0 || section.hash == null) {
                continue;
            }
            for (Map.Entry<String, List<ScannedSection>> other : byPage.entrySet()) {
                if (other.getKey().equals(page)) {
                    continue;
                }
                for (ScannedSection candidate : other.getValue()) {
                    if (section.hash.equals(candidate.hash) && index < candidate.ids.size()) {
                        matches.put(other.getKey(), candidate.ids.get(index));
                        break;
                    }
                }
            }
        }
        return matches;
    }
}
