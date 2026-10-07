package com.aris.templateapp.ui.upload;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.databinding.SheetMarkElementBinding;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bottom sheet "Tandai elemen" (alur-fitur-upload.md bagian 7.3). Hanya mengisi form dan membaca hasilnya;
 * perubahan data dilakukan pemanggil lewat {@link Callbacks}.
 */
final class MarkElementSheet {

    interface Callbacks {
        void onSave(@Nullable String existingKey, MarkingDto.Field edited);

        void onUnmark();

        void onLinkTo(String key);

        void onParent();

        void onChild();

        void onMakeSection();

        void onClosed();
    }

    private static final int[] TYPE_CHIPS = {R.id.type_text, R.id.type_paragraph, R.id.type_image, R.id.type_link,
            R.id.type_button};
    private static final String[] TYPES = {"text", "paragraph", "image", "link", "button"};
    // Rasio umum; rasio gambar asli yang mendekati salah satunya dibulatkan agar mudah dipahami pembuat website.
    private static final int[][] COMMON_RATIOS = {{16, 9}, {4, 3}, {3, 2}, {1, 1}, {2, 3}, {3, 4}, {9, 16}, {21, 9}};
    // Ukuran huruf boleh diubah ±20% dari ukuran asli (bagian 7.11).
    private static final double FONT_RANGE = 0.2;

    private MarkElementSheet() {
    }

    static BottomSheetDialog show(Fragment fragment, ElementInfo info, @Nullable MarkingDto.Field existing,
                                  List<MarkingDto.Field> others, Callbacks callbacks) {
        BottomSheetDialog sheet = new BottomSheetDialog(fragment.requireContext());
        SheetMarkElementBinding b = SheetMarkElementBinding.inflate(fragment.getLayoutInflater());
        sheet.setContentView(b.getRoot());
        boolean[] handled = {false};

        b.elementTag.setText(info.tag);
        b.elementText.setText(info.text == null || info.text.isEmpty() ? info.src : info.text);
        b.parentButton.setEnabled(info.hasParent);
        b.childButton.setEnabled(info.hasChild);

        String type = existing != null ? existing.type : info.kind;
        b.typeGroup.check(TYPE_CHIPS[Math.max(0, indexOf(type))]);
        b.labelInput.setText(existing == null ? null : existing.label);
        b.hintInput.setText(existing == null ? null : existing.hint);
        Integer max = existing != null ? existing.maxLength : suggestedMaxLength(info);
        b.maxInput.setText(max == null ? null : String.valueOf(max));
        b.requiredSwitch.setChecked(existing == null || existing.required);
        String ratio = existing != null && existing.aspectRatio != null ? existing.aspectRatio : ratioOf(info);
        b.ratioText.setText(fragment.getString(R.string.mark_ratio, ratio));

        List<String> styles = new ArrayList<>();
        int fontMin = (int) Math.round(info.fontSize * (1 - FONT_RANGE));
        int fontMax = (int) Math.round(info.fontSize * (1 + FONT_RANGE));
        if (existing != null && existing.styles != null) {
            for (MarkingDto.Style style : existing.styles) {
                styles.add(style.prop);
                if ("font-size".equals(style.prop) && style.min != null && style.max != null) {
                    fontMin = style.min;
                    fontMax = style.max;
                }
            }
        }
        b.styleColor.setChecked(styles.contains("color"));
        b.styleBackground.setChecked(styles.contains("background-color"));
        b.styleRadius.setChecked(styles.contains("border-radius"));
        b.styleFont.setChecked(styles.contains("font-size"));
        b.fontMinInput.setText(String.valueOf(Math.max(6, fontMin)));
        b.fontMaxInput.setText(String.valueOf(Math.max(6, fontMax)));
        b.fontRange.setVisibility(b.styleFont.isChecked() ? View.VISIBLE : View.GONE);
        b.styleFont.setOnCheckedChangeListener((v, checked) -> b.fontRange.setVisibility(checked ? View.VISIBLE : View.GONE));

        Runnable applyType = () -> {
            String selected = typeOf(b.typeGroup.getCheckedChipId());
            boolean image = "image".equals(selected);
            b.limitsRow.setVisibility(image ? View.GONE : View.VISIBLE);
            b.ratioText.setVisibility(image ? View.VISIBLE : View.GONE);
            b.styleColor.setVisibility(image ? View.GONE : View.VISIBLE);
            b.styleFont.setVisibility(image ? View.GONE : View.VISIBLE);
            if (image) {
                b.styleFont.setChecked(false);
            }
        };
        applyType.run();
        b.typeGroup.setOnCheckedStateChangeListener((group, ids) -> applyType.run());

        List<MarkingDto.Field> linkable = new ArrayList<>();
        for (MarkingDto.Field field : others) {
            if (existing == null || !field.key.equals(existing.key)) {
                linkable.add(field);
            }
        }
        b.linkButton.setVisibility(linkable.isEmpty() ? View.GONE : View.VISIBLE);
        b.linkButton.setOnClickListener(v -> {
            String[] labels = new String[linkable.size()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = linkable.get(i).label;
            }
            new MaterialAlertDialogBuilder(fragment.requireContext())
                    .setTitle(R.string.mark_link_pick_title)
                    .setItems(labels, (d, which) -> {
                        handled[0] = true;
                        sheet.dismiss();
                        callbacks.onLinkTo(linkable.get(which).key);
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });

        b.unmarkButton.setVisibility(existing == null ? View.GONE : View.VISIBLE);
        b.unmarkButton.setOnClickListener(v -> {
            handled[0] = true;
            sheet.dismiss();
            callbacks.onUnmark();
        });
        b.parentButton.setOnClickListener(v -> {
            handled[0] = true;
            sheet.dismiss();
            callbacks.onParent();
        });
        b.childButton.setOnClickListener(v -> {
            handled[0] = true;
            sheet.dismiss();
            callbacks.onChild();
        });
        b.sectionButton.setOnClickListener(v -> {
            handled[0] = true;
            sheet.dismiss();
            callbacks.onMakeSection();
        });
        b.cancelButton.setOnClickListener(v -> sheet.dismiss());
        b.saveButton.setOnClickListener(v -> {
            MarkingDto.Field edited = read(fragment, b, ratio);
            if (edited == null) {
                return;
            }
            handled[0] = true;
            sheet.dismiss();
            callbacks.onSave(existing == null ? null : existing.key, edited);
        });
        sheet.setOnDismissListener(d -> {
            if (!handled[0]) {
                callbacks.onClosed();
            }
        });
        sheet.show();
        return sheet;
    }

    /** Isi form → isian baru. Null jika ada isian yang belum valid (pesannya tampil di form). */
    @Nullable
    private static MarkingDto.Field read(Fragment fragment, SheetMarkElementBinding b, String ratio) {
        String label = OnboardingUi.text(b.labelInput).trim();
        if (label.isEmpty()) {
            b.labelLayout.setError(fragment.getString(R.string.mark_label_required));
            return null;
        }
        b.labelLayout.setError(null);
        MarkingDto.Field field = new MarkingDto.Field();
        field.label = label;
        field.type = typeOf(b.typeGroup.getCheckedChipId());
        String hint = OnboardingUi.text(b.hintInput).trim();
        field.hint = hint.isEmpty() ? null : hint;
        boolean image = "image".equals(field.type);
        field.maxLength = image ? null : parse(OnboardingUi.text(b.maxInput));
        field.required = b.requiredSwitch.isChecked();
        field.aspectRatio = image ? ratio : null;
        if (b.styleColor.isChecked() && !image) {
            field.styles.add(new MarkingDto.Style("color", null, null, null));
        }
        if (b.styleBackground.isChecked()) {
            field.styles.add(new MarkingDto.Style("background-color", null, null, null));
        }
        if (b.styleRadius.isChecked()) {
            field.styles.add(new MarkingDto.Style("border-radius", 0, 32, "px"));
        }
        if (b.styleFont.isChecked() && !image) {
            Integer min = parse(OnboardingUi.text(b.fontMinInput));
            Integer max = parse(OnboardingUi.text(b.fontMaxInput));
            if (min == null || max == null || min < 6 || max > 200 || min > max) {
                b.fontMinLayout.setError(fragment.getString(R.string.mark_font_range_invalid));
                return null;
            }
            b.fontMinLayout.setError(null);
            field.styles.add(new MarkingDto.Style("font-size", min, max, "px"));
        }
        return field;
    }

    /** Maks karakter disarankan dari panjang teks asli agar layout tidak rusak (bagian 7.3). */
    @Nullable
    static Integer suggestedMaxLength(ElementInfo info) {
        if ("image".equals(info.kind) || info.text == null || info.text.isEmpty()) {
            return null;
        }
        int length = info.text.length();
        return Math.min(2000, Math.max(length + 10, (int) Math.ceil(length * 1.5)));
    }

    /** Rasio gambar asli, dibulatkan ke rasio umum jika selisihnya di bawah 3%. */
    static String ratioOf(ElementInfo info) {
        if (info.ratioW <= 0 || info.ratioH <= 0) {
            return "16:9";
        }
        double actual = info.ratioW / (double) info.ratioH;
        for (int[] common : COMMON_RATIOS) {
            double value = common[0] / (double) common[1];
            if (Math.abs(actual - value) / value < 0.03) {
                return common[0] + ":" + common[1];
            }
        }
        int w = info.ratioW;
        int h = info.ratioH;
        while (w > 99 || h > 99) {
            w = Math.max(1, Math.round(w / 2f));
            h = Math.max(1, Math.round(h / 2f));
        }
        int g = gcd(w, h);
        return String.format(Locale.ROOT, "%d:%d", w / g, h / g);
    }

    private static int gcd(int a, int b) {
        return b == 0 ? Math.max(1, a) : gcd(b, a % b);
    }

    private static int indexOf(String type) {
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(type)) {
                return i;
            }
        }
        return 0;
    }

    private static String typeOf(int chipId) {
        for (int i = 0; i < TYPE_CHIPS.length; i++) {
            if (TYPE_CHIPS[i] == chipId) {
                return TYPES[i];
            }
        }
        return "text";
    }

    @Nullable
    private static Integer parse(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
