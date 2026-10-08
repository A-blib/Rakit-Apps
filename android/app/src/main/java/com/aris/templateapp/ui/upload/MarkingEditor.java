package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.google.gson.Gson;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Isi editor Tandai bagian (alur-fitur-upload.md bagian 7): halaman, section, isian, tema global, ditambah undo/redo
 * dan penghitung "X perubahan belum disimpan". Class Java biasa tanpa View, sehingga bisa diuji di laptop.
 * <p>
 * Setiap perubahan menyimpan salinan keadaan sebelumnya (JSON) ke tumpukan undo. Data tandaan kecil (paling banyak
 * ratusan isian), jadi cara sederhana ini cukup.
 */
public class MarkingEditor {

    /** Minimal isian agar template boleh dikirim (bagian 7.12). */
    public static final int MIN_FIELDS_TO_SEND = 3;
    private static final int UNDO_LIMIT = 50;

    private final Gson gson = new Gson();
    private final Deque<String> undo = new ArrayDeque<>();
    private final Deque<String> redo = new ArrayDeque<>();
    private MarkingDto data = new MarkingDto();
    private String savedJson = gson.toJson(new MarkingDto());
    private int changesSinceSave;
    private int sectionCounter;

    // ---------- muat & simpan ----------

    /** Data dari server (sudah tersimpan): tidak ada perubahan yang belum disimpan. */
    public void loadSaved(MarkingDto saved) {
        data = copy(saved);
        normalize();
        savedJson = gson.toJson(data);
        changesSinceSave = 0;
        undo.clear();
        redo.clear();
    }

    /** Cadangan di HP yang dipulihkan: dianggap belum disimpan. */
    public void restoreBackup(MarkingDto backup) {
        snapshot();
        data = copy(backup);
        normalize();
        changesSinceSave = Math.max(1, changesSinceSave + 1);
    }

    public void markSaved() {
        savedJson = gson.toJson(data);
        changesSinceSave = 0;
    }

    public MarkingDto data() {
        return data;
    }

    public String toJson() {
        return gson.toJson(data);
    }

    public MarkingDto fromJson(String json) {
        return gson.fromJson(json, MarkingDto.class);
    }

    public boolean hasUnsavedChanges() {
        return changesSinceSave > 0 && !gson.toJson(data).equals(savedJson);
    }

    public int unsavedCount() {
        return hasUnsavedChanges() ? changesSinceSave : 0;
    }

    /** Jumlah isian yang tersimpan di server (yang bisa dicoba di langkah 5). */
    public int savedFieldCount() {
        MarkingDto saved = gson.fromJson(savedJson, MarkingDto.class);
        return saved.fields == null ? 0 : saved.fields.size();
    }

    private void normalize() {
        if (data.pages == null) {
            data.pages = new ArrayList<>();
        }
        if (data.sections == null) {
            data.sections = new ArrayList<>();
        }
        if (data.fields == null) {
            data.fields = new ArrayList<>();
        }
        if (data.theme == null) {
            data.theme = new ArrayList<>();
        }
        for (MarkingDto.Section section : data.sections) {
            sectionCounter = Math.max(sectionCounter, numberOf(section.id));
        }
    }

    private static int numberOf(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    // ---------- undo / redo (bagian 7.10 A5) ----------

    private void snapshot() {
        undo.push(gson.toJson(data));
        if (undo.size() > UNDO_LIMIT) {
            undo.removeLast();
        }
        redo.clear();
    }

    private void changed() {
        changesSinceSave++;
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public void undo() {
        if (undo.isEmpty()) {
            return;
        }
        redo.push(gson.toJson(data));
        data = gson.fromJson(undo.pop(), MarkingDto.class);
        changesSinceSave = Math.max(0, changesSinceSave - 1);
        if (changesSinceSave == 0 && !gson.toJson(data).equals(savedJson)) {
            changesSinceSave = 1;
        }
    }

    public void redo() {
        if (redo.isEmpty()) {
            return;
        }
        undo.push(gson.toJson(data));
        data = gson.fromJson(redo.pop(), MarkingDto.class);
        changesSinceSave++;
    }

    // ---------- halaman & section ----------

    /** Memastikan setiap halaman template punya entri (nama tampil di pemilih halaman). */
    public void ensurePages(List<String> files) {
        for (String file : files) {
            if (page(file) == null) {
                data.pages.add(new MarkingDto.Page(file, pageName(file)));
            }
        }
    }

    /** "index.html" → "Beranda", "tentang-kami.html" → "Tentang Kami". */
    static String pageName(String file) {
        if (file.equals("index.html")) {
            return "Beranda";
        }
        String base = file.substring(file.lastIndexOf('/') + 1).replaceAll("\\.html?$", "").replaceAll("[-_]+", " ");
        StringBuilder name = new StringBuilder();
        for (String word : base.trim().split("\\s+")) {
            if (!word.isEmpty()) {
                name.append(name.length() == 0 ? "" : " ")
                        .append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
        }
        String result = name.length() == 0 ? file : name.toString();
        return result.length() > 40 ? result.substring(0, 40) : result;
    }

    @Nullable
    public MarkingDto.Page page(String file) {
        for (MarkingDto.Page page : data.pages) {
            if (page.file.equals(file)) {
                return page;
            }
        }
        return null;
    }

    public List<MarkingDto.Section> sectionsOf(String page) {
        List<MarkingDto.Section> result = new ArrayList<>();
        for (MarkingDto.Section section : data.sections) {
            if (section.page.equals(page)) {
                result.add(section);
            }
        }
        return result;
    }

    /**
     * Hasil deteksi otomatis dipakai hanya jika halaman belum punya section (koreksi provider yang tersimpan
     * selalu menang). Tidak dihitung sebagai perubahan, karena provider belum mengubah apa pun.
     */
    public void applyDetectedSections(String page, List<SectionInfo> detected) {
        if (!sectionsOf(page).isEmpty()) {
            return;
        }
        for (SectionInfo info : detected) {
            data.sections.add(new MarkingDto.Section(nextSectionId(), page, cut(info.name, 40), info.tplId));
        }
        // Section hasil deteksi menjadi titik awal; belum ada perubahan dari provider.
        if (changesSinceSave == 0) {
            savedJson = gson.toJson(data);
        }
    }

    private String nextSectionId() {
        return "s" + (++sectionCounter);
    }

    public void renameSection(String id, String name) {
        MarkingDto.Section section = section(id);
        if (section == null || name.trim().isEmpty() || name.trim().equals(section.name)) {
            return;
        }
        snapshot();
        section.name = cut(name.trim(), 40);
        changed();
    }

    /** Gabungkan dengan section berikutnya di halaman yang sama: section berikutnya dihapus (bagian 7.1). */
    public void mergeWithNext(String id) {
        MarkingDto.Section section = section(id);
        if (section == null) {
            return;
        }
        List<MarkingDto.Section> list = sectionsOf(section.page);
        int index = list.indexOf(section);
        if (index < 0 || index >= list.size() - 1) {
            return;
        }
        snapshot();
        removeSectionInternal(list.get(index + 1), section.id);
        changed();
    }

    /** Hapus section yang salah deteksi: isinya menjadi bagian section sebelumnya. Section terakhir tidak bisa dihapus. */
    public void deleteSection(String id) {
        MarkingDto.Section section = section(id);
        if (section == null) {
            return;
        }
        List<MarkingDto.Section> list = sectionsOf(section.page);
        if (list.size() <= 1) {
            return;
        }
        int index = list.indexOf(section);
        String target = index > 0 ? list.get(index - 1).id : list.get(1).id;
        snapshot();
        removeSectionInternal(section, target);
        changed();
    }

    private void removeSectionInternal(MarkingDto.Section section, String moveFieldsTo) {
        data.sections.remove(section);
        for (MarkingDto.Field field : data.fields) {
            if (section.id.equals(field.sectionId)) {
                field.sectionId = moveFieldsTo;
            }
        }
    }

    /**
     * "Jadikan section" (bagian 7.1): elemen terpilih menjadi awal section baru, disisipkan menurut posisinya.
     *
     * @param indexInPage posisi section baru di antara section halaman itu (dihitung layar dari posisi elemen)
     */
    public String splitAt(String page, int tplId, String name, int indexInPage) {
        snapshot();
        MarkingDto.Section created = new MarkingDto.Section(nextSectionId(), page, cut(name, 40), tplId);
        List<MarkingDto.Section> list = sectionsOf(page);
        if (indexInPage >= list.size()) {
            data.sections.add(created);
        } else {
            data.sections.add(data.sections.indexOf(list.get(indexInPage)), created);
        }
        changed();
        return created.id;
    }

    @Nullable
    public MarkingDto.Section section(String id) {
        for (MarkingDto.Section section : data.sections) {
            if (section.id.equals(id)) {
                return section;
            }
        }
        return null;
    }

    // ---------- isian ----------

    public List<MarkingDto.Field> fields() {
        return data.fields;
    }

    /** Isian yang memuat elemen ini, atau null jika elemen belum ditandai. */
    @Nullable
    public MarkingDto.Field fieldOf(String page, int tplId) {
        for (MarkingDto.Field field : data.fields) {
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(page) && element.tplId == tplId) {
                    return field;
                }
            }
        }
        return null;
    }

    @Nullable
    public MarkingDto.Field field(String key) {
        for (MarkingDto.Field field : data.fields) {
            if (field.key.equals(key)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Simpan isi bottom sheet Tandai elemen. Isian baru mendapat kunci otomatis dari labelnya; isian lama diganti isinya
     * tetapi kunci dan daftar elemennya tetap.
     */
    public MarkingDto.Field saveField(@Nullable String existingKey, MarkingDto.Field edited, MarkingDto.Element element) {
        snapshot();
        MarkingDto.Field field = existingKey == null ? null : field(existingKey);
        if (field == null) {
            field = edited;
            field.key = uniqueKey(edited.label);
            field.order = data.fields.size() + 1;
            field.elements = new ArrayList<>();
            field.elements.add(element);
            data.fields.add(field);
            sortFields();
        } else {
            field.label = edited.label;
            field.type = edited.type;
            field.hint = edited.hint;
            field.maxLength = edited.maxLength;
            field.required = edited.required;
            field.aspectRatio = edited.aspectRatio;
            field.styles = edited.styles;
            if (edited.sectionId != null) {
                field.sectionId = edited.sectionId;
            }
        }
        changed();
        return field;
    }

    /**
     * "Hubungkan ke isian yang sudah ada" (bagian 7.3 & 7.5): elemen ini ikut diubah oleh isian lain
     * (versi HP & desktop, atau header yang sama di beberapa halaman). Tandaan lama elemen ini dilepas.
     */
    public void linkElement(String key, MarkingDto.Element element) {
        MarkingDto.Field target = field(key);
        if (target == null) {
            return;
        }
        snapshot();
        detach(element.page, element.tplId);
        target.elements.add(element);
        changed();
    }

    /** Menambahkan beberapa elemen sekaligus ke isian (mis. "tandai sekali untuk semua halaman", bagian 7.6). */
    public void linkElements(String key, List<MarkingDto.Element> elements) {
        MarkingDto.Field target = field(key);
        if (target == null || elements.isEmpty()) {
            return;
        }
        snapshot();
        for (MarkingDto.Element element : elements) {
            if (fieldOf(element.page, element.tplId) == null) {
                target.elements.add(element);
            }
        }
        changed();
    }

    /** Hapus tandaan satu elemen; isian ikut terhapus jika tidak punya elemen lagi. */
    public void unmark(String page, int tplId) {
        if (fieldOf(page, tplId) == null) {
            return;
        }
        snapshot();
        detach(page, tplId);
        changed();
    }

    private void detach(String page, int tplId) {
        MarkingDto.Field field = fieldOf(page, tplId);
        if (field == null) {
            return;
        }
        field.elements.removeIf(e -> e.page.equals(page) && e.tplId == tplId);
        if (field.elements.isEmpty()) {
            data.fields.remove(field);
            sortFields();
        }
    }

    /**
     * Urutan isian = urutan elemennya di halaman (halaman dulu, lalu nomor data-tpl-id yang mengikuti urutan HTML),
     * bukan urutan provider menandai. Form pembuat website jadi tersusun dari atas ke bawah seperti halamannya.
     */
    private void sortFields() {
        List<String> pageOrder = new ArrayList<>();
        for (MarkingDto.Page p : data.pages) {
            pageOrder.add(p.file);
        }
        data.fields.sort((a, b) -> {
            MarkingDto.Element ea = a.elements.isEmpty() ? null : a.elements.get(0);
            MarkingDto.Element eb = b.elements.isEmpty() ? null : b.elements.get(0);
            if (ea == null || eb == null) {
                return ea == null ? (eb == null ? 0 : 1) : -1;
            }
            int byPage = Integer.compare(pageOrder.indexOf(ea.page), pageOrder.indexOf(eb.page));
            return byPage != 0 ? byPage : Integer.compare(ea.tplId, eb.tplId);
        });
        for (int i = 0; i < data.fields.size(); i++) {
            data.fields.get(i).order = i + 1;
        }
    }

    /** Saran tandai otomatis: "Tandai semua" (bagian 7.10 A4). Elemen yang sudah ditandai dilewati. */
    public int addSuggestions(String page, String sectionId, List<Suggestion> suggestions) {
        List<Suggestion> fresh = new ArrayList<>();
        for (Suggestion s : suggestions) {
            if (fieldOf(page, s.id) == null) {
                fresh.add(s);
            }
        }
        if (fresh.isEmpty()) {
            return 0;
        }
        snapshot();
        for (Suggestion s : fresh) {
            MarkingDto.Field field = new MarkingDto.Field();
            field.label = cut(s.label, 60);
            field.type = s.kind;
            field.key = uniqueKey(field.label);
            field.order = data.fields.size() + 1;
            field.sectionId = sectionId;
            field.required = false;
            // Saran berasal dari elemen yang terlihat; dianggap tampil di kedua tampilan sampai terbukti sebaliknya.
            List<String> visible = new ArrayList<>();
            visible.add("mobile");
            visible.add("desktop");
            field.elements.add(new MarkingDto.Element(page, s.id, visible));
            data.fields.add(field);
        }
        sortFields();
        changed();
        return fresh.size();
    }

    /** Satu saran dari {@code mark.js}. */
    public static class Suggestion {
        public int id;
        public String kind;
        public String label;
    }

    /**
     * Memperbarui di tampilan mana setiap elemen terlihat (label HANYA HP / HANYA DESKTOP, bagian 7.4).
     * Tidak dihitung sebagai perubahan provider.
     */
    public void updateVisibility(String page, String view, Map<Integer, Boolean> visibleById) {
        for (MarkingDto.Field field : data.fields) {
            for (MarkingDto.Element element : field.elements) {
                Boolean visible = element.page.equals(page) ? visibleById.get(element.tplId) : null;
                if (visible == null) {
                    continue;
                }
                if (visible && !element.visibleIn.contains(view)) {
                    element.visibleIn.add(view);
                } else if (!visible && element.visibleIn.contains(view) && element.visibleIn.size() > 1) {
                    element.visibleIn.remove(view);
                }
            }
        }
    }

    // ---------- tema global (bagian 7.11 B) ----------

    @Nullable
    public MarkingDto.ThemeVar theme(String var) {
        for (MarkingDto.ThemeVar theme : data.theme) {
            if (theme.var.equals(var)) {
                return theme;
            }
        }
        return null;
    }

    public void setTheme(String var, @Nullable String label, String type) {
        MarkingDto.ThemeVar existing = theme(var);
        String clean = label == null ? null : label.trim();
        if (clean != null && clean.isEmpty()) {
            return;
        }
        if (existing == null && clean == null) {
            return;
        }
        if (existing != null && clean != null && clean.equals(existing.label)) {
            return;
        }
        snapshot();
        if (clean == null) {
            data.theme.remove(existing);
        } else if (existing == null) {
            data.theme.add(new MarkingDto.ThemeVar(var, cut(clean, 40), type));
        } else {
            existing.label = cut(clean, 40);
        }
        changed();
    }

    /** Variabel CSS ditebak jenisnya dari nilainya: warna atau ukuran. */
    public static String themeType(String value) {
        String v = value.trim().toLowerCase(Locale.ROOT);
        return v.startsWith("#") || v.startsWith("rgb") || v.startsWith("hsl") || v.matches("[a-z]+") ? "color" : "size";
    }

    /** "--primary" → "Primary", "--font-heading" → "Font heading". */
    public static String themeLabel(String var) {
        String words = var.replaceFirst("^--", "").replace('-', ' ').trim();
        return words.isEmpty() ? var : words.substring(0, 1).toUpperCase(Locale.ROOT) + words.substring(1);
    }

    // ---------- pengecekan langsung (bagian 7.7) ----------

    /** Pesan masalah pertama yang mencegah Simpan, atau null jika semua isian siap disimpan. */
    @Nullable
    public String firstProblem() {
        Set<String> keys = new HashSet<>();
        for (MarkingDto.Field field : data.fields) {
            if (field.label == null || field.label.trim().isEmpty()) {
                return "LABEL";
            }
            if (!keys.add(field.key)) {
                return "KEY";
            }
        }
        return null;
    }

    // ---------- helper ----------

    /** Kunci data-key dari label: "Judul utama" → "judul_utama", dibuat unik dengan akhiran _2, _3, ... */
    String uniqueKey(String label) {
        String base = Normalizer.normalize(label == null ? "" : label, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isEmpty() || !Character.isLetter(base.charAt(0))) {
            base = "isian_" + base;
        }
        base = base.replaceAll("_+$", "");
        if (base.length() > 34) {
            base = base.substring(0, 34);
        }
        String key = base;
        int n = 2;
        while (field(key) != null) {
            key = base + "_" + n++;
        }
        return key;
    }

    private static String cut(String text, int max) {
        String value = text == null ? "" : text.trim();
        return value.length() <= max ? value : value.substring(0, max);
    }

    private MarkingDto copy(MarkingDto source) {
        return gson.fromJson(gson.toJson(source == null ? new MarkingDto() : source), MarkingDto.class);
    }
}
