package com.aris.templateapp.core.template;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.ProjectStatus;
import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aturan kelengkapan project (alur-buat-website-via-template.md bagian 7.1). Murni Java agar semua cabangnya bisa
 * diuji tanpa HP.
 * <ul>
 *   <li>isian wajib kosong → Perlu dilengkapi</li>
 *   <li>isian wajib masih berisi contoh provider → Perlu dilengkapi</li>
 *   <li>isian opsional masih berisi contoh → Saran (tidak memengaruhi status)</li>
 *   <li>link/tombol dengan URL tidak valid → Perlu dilengkapi</li>
 * </ul>
 * "Masih contoh" untuk link dan tombol berarti link-nya belum diganti dan menunjuk ke luar website (mis. nomor
 * WhatsApp contoh); link ke halaman sendiri seperti {@code kontak.html} dianggap sudah benar.
 */
public final class CompletenessChecker {

    public enum Reason { EMPTY, SAMPLE, INVALID_LINK }

    public static final class Item {
        public final TemplateManifest.Field field;
        public final Reason reason;

        Item(TemplateManifest.Field field, Reason reason) {
            this.field = field;
            this.reason = reason;
        }
    }

    public static final class Result {
        /** Perlu dilengkapi, urut sesuai urutan isian. */
        public final List<Item> missing;
        /** Saran (isian opsional yang masih contoh). */
        public final List<Item> suggestions;
        /** Jumlah isian wajib yang sudah lengkap, untuk "9/12 isian lengkap". */
        public final int completeRequired;
        public final int totalRequired;
        /** sectionId → jumlah isian yang belum lengkap (chip "·2"). */
        public final Map<String, Integer> missingBySection;

        Result(List<Item> missing, List<Item> suggestions, int completeRequired, int totalRequired,
               Map<String, Integer> missingBySection) {
            this.missing = Collections.unmodifiableList(missing);
            this.suggestions = Collections.unmodifiableList(suggestions);
            this.completeRequired = completeRequired;
            this.totalRequired = totalRequired;
            this.missingBySection = Collections.unmodifiableMap(missingBySection);
        }

        public int missingCount() {
            return missing.size();
        }

        @Nullable
        public Reason reasonFor(String key) {
            for (Item item : missing) {
                if (item.field.key.equals(key)) {
                    return item.reason;
                }
            }
            return null;
        }

        public boolean isSuggestion(String key) {
            for (Item item : suggestions) {
                if (item.field.key.equals(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    private CompletenessChecker() {
    }

    public static Result check(TemplateManifest manifest, ProjectValues values) {
        List<Item> missing = new ArrayList<>();
        List<Item> suggestions = new ArrayList<>();
        Map<String, Integer> bySection = new LinkedHashMap<>();
        int totalRequired = 0;
        for (TemplateManifest.Field field : manifest.fields) {
            Reason reason = problem(field, values.peek(field.key));
            if (field.required) {
                totalRequired++;
            }
            if (reason == null) {
                continue;
            }
            if (field.required || reason == Reason.INVALID_LINK) {
                missing.add(new Item(field, reason));
                if (field.sectionId != null) {
                    bySection.merge(field.sectionId, 1, Integer::sum);
                }
            } else if (reason == Reason.SAMPLE) {
                suggestions.add(new Item(field, reason));
            }
        }
        int completeRequired = 0;
        for (TemplateManifest.Field field : manifest.fields) {
            if (field.required && problem(field, values.peek(field.key)) == null) {
                completeRequired++;
            }
        }
        return new Result(missing, suggestions, completeRequired, totalRequired, bySection);
    }

    /**
     * Status project (bagian 7.1): pernah diexport → tetap {@code exported}; selain itu ditentukan jumlah isian yang
     * belum lengkap.
     */
    public static ProjectStatus status(int missingCount, @Nullable Long lastExportedAt) {
        if (lastExportedAt != null) {
            return ProjectStatus.EXPORTED;
        }
        return missingCount > 0 ? ProjectStatus.DRAFT : ProjectStatus.READY;
    }

    /** Masalah satu isian, atau null jika sudah benar. Isian opsional yang kosong tidak dianggap masalah. */
    @Nullable
    static Reason problem(TemplateManifest.Field field, @Nullable ProjectValues.FieldValue value) {
        String type = field.type == null ? TemplateManifest.TYPE_TEXT : field.type;
        switch (type) {
            case TemplateManifest.TYPE_IMAGE: {
                if (value != null && value.image != null) {
                    return null;
                }
                return field.sample == null ? Reason.EMPTY : Reason.SAMPLE;
            }
            case TemplateManifest.TYPE_LINK:
                return hrefProblem(field.sample, value == null ? null : value.href, field.required);
            case TemplateManifest.TYPE_BUTTON: {
                String text = value != null && value.text != null ? value.text : field.sample;
                if (isBlank(text)) {
                    return field.required ? Reason.EMPTY : null;
                }
                return hrefProblem(field.sampleHref, value == null ? null : value.href, field.required);
            }
            default: {
                String text = value != null && value.text != null ? value.text : field.sample;
                if (isBlank(text)) {
                    return field.required ? Reason.EMPTY : null;
                }
                return field.sample != null && text.trim().equals(field.sample.trim()) ? Reason.SAMPLE : null;
            }
        }
    }

    @Nullable
    private static Reason hrefProblem(@Nullable String sampleHref, @Nullable String chosen, boolean required) {
        if (chosen != null) {
            if (isBlank(chosen)) {
                return required ? Reason.EMPTY : null;
            }
            return LinkRules.isValid(chosen) ? null : Reason.INVALID_LINK;
        }
        if (isBlank(sampleHref)) {
            return required ? Reason.EMPTY : null;
        }
        return LinkRules.isExternal(sampleHref) ? Reason.SAMPLE : null;
    }

    private static boolean isBlank(@Nullable String text) {
        return text == null || text.trim().isEmpty();
    }
}
