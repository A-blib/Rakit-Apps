package com.aris.templateapp.ui.editor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.aris.templateapp.R;
import com.aris.templateapp.core.template.ColorContrast;
import com.aris.templateapp.core.template.CompletenessChecker;
import com.aris.templateapp.core.template.CustomCss;
import com.aris.templateapp.core.template.LinkRules;
import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;
import com.aris.templateapp.databinding.ItemEditorFieldBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.TickVisibilityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Form editor template mode (alur-buat-website-via-template.md bagian 6.4 & 6.5): tab <b>Isi</b> (teks, foto, link,
 * tombol) dan tab <b>Gaya</b> (tema website + gaya per section). Setiap perubahan diteruskan ke {@link Listener};
 * nilai disimpan ViewModel, bukan di sini.
 * <p>
 * Isian dibuat beberapa per frame: membangun puluhan isian sekaligus menahan layar (pelajaran dari langkah Coba).
 */
final class EditorForm {

    enum Tab { CONTENT, STYLE }

    interface Listener {
        void onText(String key, String text);

        void onHref(String key, @Nullable String href);

        void onPickImage(String key);

        void onResetImage(String key);

        void onStyle(String key, String prop, @Nullable String value);

        void onTheme(String variable, @Nullable String value);

        /** Kolom isian mulai diketik/diketuk: elemennya disorot di preview. */
        void onFieldFocused(String key);
    }

    /** Memuat gambar kecil (foto pilihan atau foto contoh) ke ImageView di thread latar. */
    interface ImageLoader {
        void load(TemplateManifest.Field field, @Nullable ProjectValues.FieldValue value, ImageView target);
    }

    /** Gaya yang sedang berlaku di halaman (dari {@code RakitEditor.computed}). */
    static final class Computed {
        float fontSize;
        float radius;
        String color;
        String background;
    }

    private static final int FIELDS_PER_FRAME = 3;

    private final Context context;
    private final LinearLayout container;
    private final Listener listener;
    private final ImageLoader images;
    private final Map<String, ItemEditorFieldBinding> rows = new HashMap<>();
    private final Map<String, CircularProgressIndicator> imageProgress = new HashMap<>();
    private int buildGeneration;

    EditorForm(LinearLayout container, Listener listener, ImageLoader images) {
        this.context = container.getContext();
        this.container = container;
        this.listener = listener;
        this.images = images;
    }

    /**
     * @param sectionId hanya isian di section ini; null = semua
     */
    void build(TemplateManifest manifest, ProjectValues values, CompletenessChecker.Result completeness, Tab tab,
               @Nullable String sectionId, Map<String, Computed> computed) {
        int build = ++buildGeneration;
        container.removeAllViews();
        rows.clear();
        imageProgress.clear();
        List<Runnable> steps = new ArrayList<>();
        if (tab == Tab.CONTENT) {
            for (TemplateManifest.Field field : fieldsOf(manifest, sectionId)) {
                steps.add(() -> addContentField(field, values, completeness));
            }
        } else {
            if (sectionId == null && !manifest.theme.isEmpty()) {
                steps.add(() -> addGroupTitle(context.getString(R.string.editor_theme_title)));
                for (TemplateManifest.ThemeVar variable : manifest.theme) {
                    steps.add(() -> addTheme(variable, values));
                }
            }
            for (TemplateManifest.Section section : sectionsWithStyles(manifest, sectionId)) {
                steps.add(() -> addGroupTitle(context.getString(R.string.editor_section_title, section.name)));
                for (TemplateManifest.Field field : manifest.fields) {
                    if (section.id.equals(field.sectionId) && !field.styles.isEmpty()) {
                        steps.add(() -> addStyleField(field, values, computed.get(field.key)));
                    }
                }
            }
            for (TemplateManifest.Field field : manifest.fields) {
                // Isian bergaya tanpa section (jarang): tetap bisa diatur di bawah.
                if (field.sectionId == null && sectionId == null && !field.styles.isEmpty()) {
                    steps.add(() -> addStyleField(field, values, computed.get(field.key)));
                }
            }
        }
        if (steps.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setTextAppearance(R.style.TextAppearance_App_Body);
            empty.setTextColor(context.getColor(R.color.color_muted));
            empty.setPadding(0, px(R.dimen.space_4), 0, 0);
            empty.setText(tab == Tab.CONTENT ? R.string.editor_empty_content : R.string.editor_empty_style);
            container.addView(empty);
            return;
        }
        runSteps(steps, 0, build);
    }

    private void runSteps(List<Runnable> steps, int start, int build) {
        if (build != buildGeneration) {
            return;
        }
        int end = Math.min(steps.size(), start + FIELDS_PER_FRAME);
        for (int i = start; i < end; i++) {
            steps.get(i).run();
        }
        if (end < steps.size()) {
            container.post(() -> runSteps(steps, end, build));
        }
    }

    static List<TemplateManifest.Field> fieldsOf(TemplateManifest manifest, @Nullable String sectionId) {
        List<TemplateManifest.Field> result = new ArrayList<>();
        for (TemplateManifest.Field field : manifest.fields) {
            if (sectionId == null || sectionId.equals(field.sectionId)) {
                result.add(field);
            }
        }
        return result;
    }

    private static List<TemplateManifest.Section> sectionsWithStyles(TemplateManifest manifest, @Nullable String sectionId) {
        List<TemplateManifest.Section> result = new ArrayList<>();
        for (TemplateManifest.Section section : manifest.sections) {
            if (sectionId != null && !sectionId.equals(section.id)) {
                continue;
            }
            for (TemplateManifest.Field field : manifest.fields) {
                if (section.id.equals(field.sectionId) && !field.styles.isEmpty()) {
                    result.add(section);
                    break;
                }
            }
        }
        return result;
    }

    // ---------- tab Isi ----------

    private void addContentField(TemplateManifest.Field field, ProjectValues values,
                                 CompletenessChecker.Result completeness) {
        ItemEditorFieldBinding row = ItemEditorFieldBinding.inflate(LayoutInflater.from(context), container, true);
        row.getRoot().setTag(field.key);
        rows.put(field.key, row);
        row.label.setText(field.required ? context.getString(R.string.editor_required_label, field.label) : field.label);
        row.hint.setText(field.hint);
        row.hint.setVisibility(field.hint == null || field.hint.isEmpty() ? View.GONE : View.VISIBLE);
        ProjectValues.FieldValue value = values.peek(field.key);
        String type = field.type == null ? TemplateManifest.TYPE_TEXT : field.type;
        switch (type) {
            case TemplateManifest.TYPE_IMAGE:
                addImage(row, field, value);
                break;
            case TemplateManifest.TYPE_LINK:
                addHref(row, field, value, field.sample);
                break;
            case TemplateManifest.TYPE_BUTTON:
                addText(row, field, value, false);
                addHref(row, field, value, field.sampleHref);
                break;
            case TemplateManifest.TYPE_PARAGRAPH:
                addText(row, field, value, true);
                break;
            case TemplateManifest.TYPE_TEXT:
            default:
                addText(row, field, value, false);
                break;
        }
        showWarning(field, row, completeness);
    }

    private void addText(ItemEditorFieldBinding row, TemplateManifest.Field field,
                         @Nullable ProjectValues.FieldValue value, boolean multiline) {
        TextInputLayout layout = newInput(row.inputs, null, multiline, field.maxLength);
        EditText input = layout.getEditText();
        String text = value != null && value.text != null ? value.text : field.sample;
        input.setText(text);
        watchFocus(input, field.key);
        input.addTextChangedListener(new AfterChange(changed -> listener.onText(field.key, changed)));
    }

    private void addHref(ItemEditorFieldBinding row, TemplateManifest.Field field,
                         @Nullable ProjectValues.FieldValue value, @Nullable String sampleHref) {
        TextInputLayout layout = newInput(row.inputs, context.getString(R.string.editor_link_hint), false, null);
        EditText input = layout.getEditText();
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(value != null && value.href != null ? value.href : sampleHref);
        updateLinkError(layout, value == null ? null : value.href);
        watchFocus(input, field.key);
        input.addTextChangedListener(new AfterChange(changed -> {
            String href = changed.trim();
            updateLinkError(layout, href);
            listener.onHref(field.key, href);
        }));
        MaterialButton whatsapp = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
        whatsapp.setText(R.string.editor_whatsapp_button);
        whatsapp.setTextColor(context.getColor(R.color.color_link));
        whatsapp.setPadding(0, 0, 0, 0);
        whatsapp.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        whatsapp.setOnClickListener(v -> askWhatsapp(input));
        row.inputs.addView(whatsapp, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                px(R.dimen.touch_target_min)));
    }

    /** Hanya link yang diketik user yang dinilai; link bawaan template (mis. "kontak.html") tidak ditandai salah. */
    private void updateLinkError(TextInputLayout layout, @Nullable String typed) {
        boolean invalid = typed != null && !typed.isEmpty() && !LinkRules.isValid(typed);
        layout.setError(invalid ? context.getString(R.string.editor_link_invalid) : null);
    }

    /** Bantuan WhatsApp (bagian 6.4): nomor biasa → https://wa.me/62… Nomor tidak valid diberi pesan, dialog tetap terbuka. */
    private void askWhatsapp(EditText hrefInput) {
        TextInputLayout layout = new TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layout.setHint(context.getString(R.string.editor_whatsapp_number));
        TextInputEditText number = new TextInputEditText(layout.getContext());
        number.setInputType(InputType.TYPE_CLASS_PHONE);
        number.setText(LinkRules.whatsappNumber(hrefInput.getText().toString()));
        // Nomor lama (sering nomor contoh) langsung terpilih, jadi mengetik menggantinya, bukan menambah di belakangnya.
        number.setSelectAllOnFocus(true);
        layout.addView(number);
        FrameLayout frame = new FrameLayout(context);
        int padding = px(R.dimen.screen_padding_horizontal);
        frame.setPadding(padding, px(R.dimen.space_2), padding, 0);
        frame.addView(layout);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.editor_whatsapp_title)
                .setView(frame)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.editor_use, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String link = LinkRules.whatsappLink(String.valueOf(number.getText()));
                    if (link == null) {
                        layout.setError(context.getString(R.string.editor_whatsapp_invalid));
                        return;
                    }
                    hrefInput.setText(link);
                    dialog.dismiss();
                }));
        dialog.show();
        number.requestFocus();
    }

    private void addImage(ItemEditorFieldBinding row, TemplateManifest.Field field,
                          @Nullable ProjectValues.FieldValue value) {
        FrameLayout frame = new FrameLayout(context);
        frame.setBackgroundResource(R.drawable.bg_thumbnail);
        ImageView preview = new ImageView(context);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setPadding(px(R.dimen.border_width), px(R.dimen.border_width), px(R.dimen.border_width),
                px(R.dimen.border_width));
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        frame.addView(preview, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        CircularProgressIndicator progress = new CircularProgressIndicator(context);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        frame.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        imageProgress.put(field.key, progress);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(px(R.dimen.editor_image_preview_width),
                px(R.dimen.editor_image_preview_height));
        frameParams.topMargin = px(R.dimen.space_2);
        row.inputs.addView(frame, frameParams);
        images.load(field, value, preview);

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        MaterialButton change = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        change.setText(R.string.editor_change_image);
        change.setIconResource(R.drawable.ic_image);
        change.setOnClickListener(v -> {
            listener.onFieldFocused(field.key);
            listener.onPickImage(field.key);
        });
        actions.addView(change);
        if (value != null && value.image != null) {
            MaterialButton reset = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
            reset.setText(R.string.editor_reset_image);
            reset.setTextColor(context.getColor(R.color.color_link));
            reset.setOnClickListener(v -> listener.onResetImage(field.key));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(px(R.dimen.space_2));
            actions.addView(reset, params);
        }
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        actionParams.topMargin = px(R.dimen.space_2);
        row.inputs.addView(actions, actionParams);
        if (field.aspectRatio != null) {
            TextView ratio = new TextView(context);
            ratio.setTextAppearance(R.style.TextAppearance_App_Label);
            ratio.setTextColor(context.getColor(R.color.color_muted));
            ratio.setText(context.getString(R.string.editor_ratio, field.aspectRatio));
            row.inputs.addView(ratio);
        }
    }

    /** Indikator loading pada isian gambar selama foto diproses (bagian 6.6). */
    void setImageLoading(String key, boolean loading) {
        CircularProgressIndicator progress = imageProgress.get(key);
        if (progress != null) {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    /** Memperbarui tanda "Masih teks contoh" dsb. tanpa membangun ulang form (dipanggil setiap ketikan). */
    void updateWarnings(TemplateManifest manifest, CompletenessChecker.Result completeness) {
        for (TemplateManifest.Field field : manifest.fields) {
            ItemEditorFieldBinding row = rows.get(field.key);
            if (row != null) {
                showWarning(field, row, completeness);
            }
        }
    }

    private void showWarning(TemplateManifest.Field field, ItemEditorFieldBinding row,
                             CompletenessChecker.Result completeness) {
        CompletenessChecker.Reason reason = completeness.reasonFor(field.key);
        boolean suggestion = reason == null && completeness.isSuggestion(field.key);
        @StringRes int text = 0;
        if (reason == CompletenessChecker.Reason.EMPTY) {
            text = R.string.editor_warning_empty;
        } else if (reason == CompletenessChecker.Reason.INVALID_LINK) {
            text = R.string.editor_warning_link;
        } else if (reason == CompletenessChecker.Reason.SAMPLE || suggestion) {
            text = TemplateManifest.TYPE_IMAGE.equals(field.type) ? R.string.editor_warning_sample_image
                    : field.hasHref() && !field.hasText() || TemplateManifest.TYPE_BUTTON.equals(field.type)
                    ? R.string.editor_warning_sample_link : R.string.editor_warning_sample_text;
        }
        row.warning.setVisibility(text == 0 ? View.GONE : View.VISIBLE);
        if (text != 0) {
            row.warning.setText(text);
        }
    }

    /** Membuka isian (ketuk elemen di preview / baris Kelengkapan): fokus ke input pertamanya. */
    @Nullable
    View focusField(String key) {
        ItemEditorFieldBinding row = rows.get(key);
        if (row == null) {
            return null;
        }
        View input = findInput(row.inputs);
        if (input != null) {
            input.requestFocus();
        }
        return row.getRoot();
    }

    boolean hasRow(String key) {
        return rows.containsKey(key);
    }

    @Nullable
    private static View findInput(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof EditText) {
                return child;
            }
            if (child instanceof ViewGroup) {
                View found = findInput((ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // ---------- tab Gaya ----------

    private void addGroupTitle(String text) {
        TextView title = new TextView(context);
        title.setTextAppearance(R.style.TextAppearance_App_Label);
        title.setTextColor(context.getColor(R.color.color_muted));
        title.setText(text);
        title.setAllCaps(true);
        title.setPadding(0, px(R.dimen.space_6), 0, 0);
        container.addView(title);
    }

    private void addTheme(TemplateManifest.ThemeVar variable, ProjectValues values) {
        ItemEditorFieldBinding row = ItemEditorFieldBinding.inflate(LayoutInflater.from(context), container, true);
        row.label.setText(variable.label);
        String current = values.theme.get(variable.var);
        if ("color".equals(variable.type)) {
            addColorRow(row.inputs, null, current, variable.defaultValue,
                    color -> listener.onTheme(variable.var, color));
        } else {
            int original = parsePx(variable.defaultValue, 0);
            int max = Math.min(CustomCss.THEME_SIZE_MAX, Math.max(48, original));
            addSizeRow(row.inputs, null, 0, max, current, original, size -> listener.onTheme(variable.var, size));
        }
    }

    private void addStyleField(TemplateManifest.Field field, ProjectValues values, @Nullable Computed computed) {
        ItemEditorFieldBinding row = ItemEditorFieldBinding.inflate(LayoutInflater.from(context), container, true);
        row.label.setText(field.label);
        Map<String, String> chosen = values.styles.get(field.key);
        // Salinan yang ikut berubah saat user memilih, agar peringatan kontras langsung diperbarui tanpa membangun
        // ulang form (membangun ulang membuat posisi gulir melompat).
        Map<String, String> live = chosen == null ? new HashMap<>() : new HashMap<>(chosen);
        for (TemplateManifest.Style style : field.styles) {
            String current = live.get(style.prop);
            ValueChanged changed = value -> {
                if (value == null) {
                    live.remove(style.prop);
                } else {
                    live.put(style.prop, value);
                }
                updateContrast(row, live, computed);
                listener.onStyle(field.key, style.prop, value);
            };
            switch (style.prop) {
                case "color":
                    addColorRow(row.inputs, context.getString(R.string.mark_style_color), current,
                            computed == null ? null : ColorContrast.toHex(computed.color), changed);
                    break;
                case "background-color":
                    addColorRow(row.inputs, context.getString(R.string.mark_style_background), current,
                            computed == null ? null : ColorContrast.toHex(computed.background), changed);
                    break;
                case "font-size":
                    addSizeRow(row.inputs, context.getString(R.string.mark_style_font),
                            style.min == null ? 12 : style.min, style.max == null ? 48 : style.max, current,
                            computed == null ? null : Math.round(computed.fontSize), changed);
                    break;
                case "border-radius":
                    addSizeRow(row.inputs, context.getString(R.string.mark_style_radius),
                            style.min == null ? 0 : style.min, style.max == null ? 32 : style.max, current,
                            computed == null ? null : Math.round(computed.radius), changed);
                    break;
                default:
                    break;
            }
        }
        updateContrast(row, live, computed);
    }

    /** Peringatan kontras (bagian 6.5) hanya jika user mengubah warna teks/latar; tidak memblokir. */
    private void updateContrast(ItemEditorFieldBinding row, Map<String, String> live, @Nullable Computed computed) {
        String fg = live.get("color") != null ? live.get("color") : computed == null ? null : computed.color;
        String bg = live.get("background-color") != null ? live.get("background-color")
                : computed == null ? null : computed.background;
        boolean styled = live.containsKey("color") || live.containsKey("background-color");
        row.warning.setVisibility(styled && ColorContrast.isLow(fg, bg) ? View.VISIBLE : View.GONE);
        row.warning.setText(R.string.editor_contrast_low);
    }

    private interface ValueChanged {
        void onChanged(@Nullable String value);
    }

    /** Baris warna: kotak warna (ketuk → dialog pemilih) + tombol ↺ kembali ke warna asli. */
    private void addColorRow(LinearLayout parent, @Nullable String label, @Nullable String current,
                             @Nullable String original, ValueChanged changed) {
        LinearLayout line = new LinearLayout(context);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setMinimumHeight(px(R.dimen.touch_target_min));
        addRowName(line, label);

        View swatch = new View(context);
        String shown = current != null ? current : original;
        swatch.setBackground(swatchDrawable(shown));
        swatch.setContentDescription(context.getString(R.string.cd_color, shown == null ? "-" : shown));
        int size = px(R.dimen.touch_target_min);
        line.addView(swatch, new LinearLayout.LayoutParams(size, size));
        MaterialButton reset = addResetButton(line, current != null, () -> {
            swatch.setBackground(swatchDrawable(original));
            swatch.setTag(null);
            changed.onChanged(null);
        });
        swatch.setTag(current);
        swatch.setOnClickListener(v -> ColorPickerDialog.show(context,
                swatch.getTag() != null ? (String) swatch.getTag() : original, picked -> {
                    swatch.setBackground(swatchDrawable(picked != null ? picked : original));
                    swatch.setTag(picked);
                    reset.setVisibility(picked != null ? View.VISIBLE : View.INVISIBLE);
                    changed.onChanged(picked);
                }));
        parent.addView(line);
    }

    /** Baris ukuran: Slider dengan rentang manifest + tombol ↺. Mulai dari ukuran asli elemen jika belum diubah. */
    private void addSizeRow(LinearLayout parent, @Nullable String label, int min, int max, @Nullable String current,
                            @Nullable Integer original, ValueChanged changed) {
        int top = Math.max(min + 1, max);
        LinearLayout line = new LinearLayout(context);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setMinimumHeight(px(R.dimen.touch_target_min));
        addRowName(line, label);
        TextView valueText = new TextView(context);
        valueText.setTextAppearance(R.style.TextAppearance_App_Label);
        line.addView(valueText);

        Slider slider = new Slider(context);
        int foreground = context.getColor(R.color.color_foreground);
        slider.setTrackActiveTintList(ColorStateList.valueOf(foreground));
        slider.setTrackInactiveTintList(ColorStateList.valueOf(context.getColor(R.color.color_border)));
        slider.setThumbTintList(ColorStateList.valueOf(foreground));
        slider.setValueFrom(min);
        slider.setValueTo(top);
        slider.setStepSize(1);
        slider.setTickVisibilityMode(TickVisibilityMode.TICK_VISIBILITY_HIDDEN);
        slider.setLabelFormatter(v -> context.getString(R.string.try_size_value, Math.round(v)));
        int start = current != null ? parsePx(current, min) : original != null && original > 0 ? original : min;
        int clamped = Math.max(min, Math.min(top, start));
        slider.setValue(clamped);
        valueText.setText(context.getString(R.string.try_size_value, clamped));
        MaterialButton reset = addResetButton(line, current != null, () -> {
            int back = Math.max(min, Math.min(top, original != null && original > 0 ? original : min));
            slider.setValue(back);
            changed.onChanged(null);
        });
        slider.addOnChangeListener((s, v, fromUser) -> {
            valueText.setText(context.getString(R.string.try_size_value, Math.round(v)));
            if (fromUser) {
                reset.setVisibility(View.VISIBLE);
                changed.onChanged(String.valueOf(Math.round(v)));
            }
        });
        parent.addView(line);
        parent.addView(slider);
    }

    /** Nama kontrol di kiri baris; baris tema tidak butuh (label isian sudah menjelaskannya). */
    private void addRowName(LinearLayout line, @Nullable String label) {
        TextView name = new TextView(context);
        name.setTextAppearance(R.style.TextAppearance_App_BodySmall);
        name.setText(label);
        line.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private MaterialButton addResetButton(LinearLayout line, boolean visible, Runnable reset) {
        MaterialButton button = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
        button.setIconResource(R.drawable.ic_undo);
        button.setIconTint(ColorStateList.valueOf(context.getColor(R.color.color_foreground)));
        button.setContentDescription(context.getString(R.string.editor_reset_style));
        button.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        button.setOnClickListener(v -> {
            button.setVisibility(View.INVISIBLE);
            reset.run();
        });
        int size = px(R.dimen.touch_target_min);
        line.addView(button, new LinearLayout.LayoutParams(size, size));
        return button;
    }

    private GradientDrawable swatchDrawable(@Nullable String color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(px(R.dimen.radius_small));
        shape.setStroke(px(R.dimen.border_width), context.getColor(R.color.color_border_strong));
        String hex = CustomCss.color(color);
        shape.setColor(hex == null ? Color.TRANSPARENT : Color.parseColor(hex));
        return shape;
    }

    // ---------- bantuan ----------

    private TextInputLayout newInput(LinearLayout parent, @Nullable String hint, boolean multiline,
                                     @Nullable Integer maxLength) {
        TextInputLayout layout = new TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layout.setHint(hint);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | (multiline ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        if (multiline) {
            input.setMinLines(2);
        } else {
            input.setMaxLines(1);
        }
        if (maxLength != null && maxLength > 0) {
            // Batas ditegakkan saat mengetik (bagian 6.4), dan penghitung menunjukkan sisa karakter.
            input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            layout.setCounterEnabled(true);
            layout.setCounterMaxLength(maxLength);
        }
        layout.addView(input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = px(R.dimen.space_2);
        parent.addView(layout, params);
        return layout;
    }

    private void watchFocus(EditText input, String key) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                listener.onFieldFocused(key);
            }
        });
    }

    static int parsePx(@Nullable String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Math.round(Float.parseFloat(value.replace("px", "").trim()));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int px(int dimen) {
        return context.getResources().getDimensionPixelSize(dimen);
    }

    /** TextWatcher yang hanya peduli teks akhir. */
    static final class AfterChange implements TextWatcher {
        interface OnText {
            void onText(String text);
        }

        private final OnText onText;

        AfterChange(OnText onText) {
            this.onText = onText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onText.onText(s.toString());
        }
    }
}
