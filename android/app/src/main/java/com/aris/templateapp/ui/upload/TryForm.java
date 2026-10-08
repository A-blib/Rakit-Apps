package com.aris.templateapp.ui.upload;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.databinding.ItemTryFieldBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.TickVisibilityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Form isian di langkah Coba (alur-fitur-upload.md bagian 8): bentuknya sama dengan form editor template mode
 * pembuat website. Setiap perubahan disimpan ke {@link TrySession} lalu {@link Listener#onChanged()} dipanggil agar
 * preview langsung berubah.
 */
final class TryForm {

    interface Listener {
        void onChanged();

        void onPickImage(String key);

        void onEditMark(String key);
    }

    private static final Pattern HEX = Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");
    // Rasio kontras minimal agar teks masih terbaca (WCAG untuk teks besar).
    private static final double MIN_CONTRAST = 3.0;
    // Jumlah isian yang dibuat per frame saat membangun form (lihat addFieldsFrom).
    private static final int FIELDS_PER_FRAME = 3;

    private final Context context;
    private final LinearLayout container;
    private final TrySession session;
    private final String templateId;
    private final Listener listener;
    private int buildGeneration;

    TryForm(LinearLayout container, TrySession session, String templateId, Listener listener) {
        this.context = container.getContext();
        this.container = container;
        this.session = session;
        this.templateId = templateId;
        this.listener = listener;
    }

    /**
     * @param sectionId     hanya isian di section ini; null = semua
     * @param originals     teks asli per kunci isian (dari halaman), dipakai sebagai contoh di input
     * @param originalHrefs alamat link asli per kunci isian (untuk jenis Link dan Tombol)
     */
    void build(MarkingDto marking, @Nullable String sectionId, Map<String, String> originals,
               Map<String, String> originalHrefs) {
        // Build lama yang belum selesai dibatalkan: sisa potongannya melihat nomor build yang sudah berganti.
        int build = ++buildGeneration;
        container.removeAllViews();
        if (sectionId == null && marking.theme != null && !marking.theme.isEmpty()) {
            addTheme(marking);
        }
        List<MarkingDto.Field> fields = new ArrayList<>();
        for (MarkingDto.Field field : marking.fields) {
            if (sectionId == null || sectionId.equals(field.sectionId)) {
                fields.add(field);
            }
        }
        if (fields.isEmpty() && container.getChildCount() == 0) {
            TextView empty = new TextView(context);
            empty.setTextAppearance(R.style.TextAppearance_App_Body);
            empty.setText(R.string.try_empty);
            container.addView(empty);
            return;
        }
        addFieldsFrom(fields, 0, build, originals, originalHrefs);
    }

    /**
     * Isian dibuat beberapa per frame. Template besar bisa punya puluhan isian bergaya; membuat semuanya sekaligus
     * menahan layar lebih dari satu detik (bahkan ANR), sedangkan bertahap membuat form langsung bisa dipakai.
     */
    private void addFieldsFrom(List<MarkingDto.Field> fields, int start, int build, Map<String, String> originals,
                               Map<String, String> originalHrefs) {
        if (build != buildGeneration) {
            return;
        }
        int end = Math.min(fields.size(), start + FIELDS_PER_FRAME);
        for (int i = start; i < end; i++) {
            MarkingDto.Field field = fields.get(i);
            addField(field, originals.get(field.key), originalHrefs.get(field.key));
        }
        if (end < fields.size()) {
            container.post(() -> addFieldsFrom(fields, end, build, originals, originalHrefs));
        }
    }

    private void addTheme(MarkingDto marking) {
        TextView title = new TextView(context);
        title.setTextAppearance(R.style.TextAppearance_App_Label);
        title.setText(R.string.try_theme_title);
        title.setPadding(0, px(R.dimen.space_4), 0, 0);
        container.addView(title);
        Map<String, String> theme = session.theme(templateId);
        for (MarkingDto.ThemeVar variable : marking.theme) {
            ItemTryFieldBinding row = ItemTryFieldBinding.inflate(LayoutInflater.from(context), container, true);
            row.label.setText(variable.label);
            row.hint.setText(variable.var);
            row.editMarkButton.setVisibility(View.GONE);
            if ("color".equals(variable.type)) {
                addColorPicker(row.inputs, theme.get(variable.var), color -> {
                    if (color == null) {
                        theme.remove(variable.var);
                    } else {
                        theme.put(variable.var, color);
                    }
                    listener.onChanged();
                });
            } else {
                EditText input = addInput(row.inputs, context.getString(R.string.try_theme_title), false, null);
                input.setText(theme.get(variable.var));
                input.addTextChangedListener(new UploadInfoFragment.AfterChange(text -> {
                    if (text.trim().isEmpty()) {
                        theme.remove(variable.var);
                    } else {
                        theme.put(variable.var, text.trim());
                    }
                    listener.onChanged();
                }));
            }
        }
    }

    private void addField(MarkingDto.Field field, @Nullable String original, @Nullable String originalHref) {
        ItemTryFieldBinding row = ItemTryFieldBinding.inflate(LayoutInflater.from(context), container, true);
        TrySession.Value value = session.value(templateId, field.key);
        row.label.setText(field.label);
        row.hint.setText(field.hint);
        row.hint.setVisibility(field.hint == null ? View.GONE : View.VISIBLE);
        row.editMarkButton.setOnClickListener(v -> listener.onEditMark(field.key));

        switch (field.type) {
            case "image": {
                MaterialButton button = new MaterialButton(context, null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
                button.setText(R.string.try_change_image);
                button.setOnClickListener(v -> listener.onPickImage(field.key));
                row.inputs.addView(button);
                if (field.aspectRatio != null) {
                    TextView ratio = new TextView(context);
                    ratio.setTextAppearance(R.style.TextAppearance_App_BodySmall);
                    ratio.setText(context.getString(R.string.try_ratio, field.aspectRatio));
                    row.inputs.addView(ratio);
                }
                break;
            }
            case "link":
                addHref(row, value, originalHref);
                break;
            case "button":
                addText(row, field, value, original, false);
                addHref(row, value, originalHref);
                break;
            case "paragraph":
                addText(row, field, value, original, true);
                break;
            case "text":
            default:
                addText(row, field, value, original, false);
                break;
        }
        for (MarkingDto.Style style : field.styles) {
            addStyle(row, style, value);
        }
        updateContrast(row, value);
    }

    private void addText(ItemTryFieldBinding row, MarkingDto.Field field, TrySession.Value value,
                         @Nullable String original, boolean multiline) {
        EditText input = addInput(row.inputs, null, multiline, field.maxLength);
        TextInputLayout layout = (TextInputLayout) input.getParent().getParent();
        layout.setPlaceholderText(original);
        input.setText(value.text != null ? value.text : original);
        input.addTextChangedListener(new UploadInfoFragment.AfterChange(text -> {
            value.text = text;
            listener.onChanged();
        }));
    }

    private void addHref(ItemTryFieldBinding row, TrySession.Value value, @Nullable String original) {
        EditText input = addInput(row.inputs, context.getString(R.string.try_link_hint), false, null);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        // Sama seperti teks: alamat asli ditampilkan agar pengguna tahu link sekarang mengarah ke mana.
        input.setText(value.href != null ? value.href : original);
        input.addTextChangedListener(new UploadInfoFragment.AfterChange(text -> {
            value.href = text.trim().isEmpty() ? null : text.trim();
            listener.onChanged();
        }));
    }

    private EditText addInput(LinearLayout parent, @Nullable String hint, boolean multiline, @Nullable Integer maxLength) {
        TextInputLayout layout = new TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layout.setHint(hint);
        if (maxLength != null) {
            layout.setCounterEnabled(true);
            layout.setCounterMaxLength(maxLength);
        }
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | (multiline ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        if (multiline) {
            input.setMinLines(2);
        } else {
            input.setMaxLines(1);
        }
        layout.addView(input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = px(R.dimen.space_2);
        parent.addView(layout, params);
        return input;
    }

    // ---------- gaya (bagian 7.11) ----------

    private void addStyle(ItemTryFieldBinding row, MarkingDto.Style style, TrySession.Value value) {
        TextView label = new TextView(context);
        label.setTextAppearance(R.style.TextAppearance_App_Label);
        label.setPadding(0, px(R.dimen.space_3), 0, 0);
        row.inputs.addView(label);
        switch (style.prop) {
            case "color":
            case "background-color":
                label.setText("color".equals(style.prop) ? R.string.mark_style_color : R.string.mark_style_background);
                addColorPicker(row.inputs, value.styles.get(style.prop), color -> {
                    if (color == null) {
                        value.styles.remove(style.prop);
                    } else {
                        value.styles.put(style.prop, color);
                    }
                    updateContrast(row, value);
                    listener.onChanged();
                });
                break;
            case "font-size":
                label.setText(R.string.mark_style_font);
                addSlider(row.inputs, style.min == null ? 12 : style.min, style.max == null ? 48 : style.max,
                        value.styles.get(style.prop), v -> value.styles.put(style.prop, v + "px"));
                break;
            case "border-radius":
            default:
                label.setText(R.string.mark_style_radius);
                addSlider(row.inputs, style.min == null ? 0 : style.min, style.max == null ? 32 : style.max,
                        value.styles.get(style.prop), v -> value.styles.put(style.prop, v + "px"));
                break;
        }
    }

    private interface IntChanged {
        void onChanged(int value);
    }

    private void addSlider(LinearLayout parent, int min, int max, @Nullable String current, IntChanged changed) {
        Slider slider = new Slider(context);
        slider.setValueFrom(min);
        slider.setValueTo(Math.max(min + 1, max));
        slider.setStepSize(1);
        slider.setTickVisibilityMode(TickVisibilityMode.TICK_VISIBILITY_HIDDEN);
        int start = min;
        if (current != null) {
            try {
                start = Integer.parseInt(current.replace("px", "").trim());
            } catch (NumberFormatException ignored) {
                // Nilai lama tidak terbaca: mulai dari batas bawah.
            }
        }
        slider.setValue(Math.max(min, Math.min(Math.max(min + 1, max), start)));
        slider.setLabelFormatter(v -> context.getString(R.string.try_size_value, Math.round(v)));
        slider.addOnChangeListener((s, v, fromUser) -> {
            if (fromUser) {
                changed.onChanged(Math.round(v));
                listener.onChanged();
            }
        });
        parent.addView(slider);
    }

    private interface ColorChanged {
        void onChanged(@Nullable String color);
    }

    /**
     * Pilihan warna cepat + kode warna sendiri; "Asli" mengembalikan warna bawaan template.
     * Contoh warna dibuat dari View biasa, bukan Chip: template besar bisa punya puluhan pemilih warna, dan ratusan
     * Chip membuat form butuh beberapa detik untuk tampil.
     */
    private void addColorPicker(LinearLayout parent, @Nullable String current, ColorChanged changed) {
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout options = new LinearLayout(context);
        options.setOrientation(LinearLayout.HORIZONTAL);
        options.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(options);
        parent.addView(scroll);
        EditText hex = addInput(parent, context.getString(R.string.try_color_custom), false, null);
        hex.setText(current);

        List<View> all = new ArrayList<>();
        TextView original = new TextView(context);
        original.setText(R.string.try_color_original);
        original.setTextAppearance(R.style.TextAppearance_App_BodySmall);
        original.setGravity(Gravity.CENTER);
        original.setMinHeight(px(R.dimen.touch_target_min));
        original.setPadding(px(R.dimen.space_3), 0, px(R.dimen.space_3), 0);
        original.setOnClickListener(v -> {
            select(all, original);
            hex.setText(null);
            changed.onChanged(null);
        });
        all.add(original);
        options.addView(original);
        View selected = current == null ? original : null;
        for (String color : context.getResources().getStringArray(R.array.try_color_presets)) {
            View swatch = new View(context);
            swatch.setTag(Color.parseColor(color));
            swatch.setContentDescription(context.getString(R.string.cd_color, color));
            swatch.setOnClickListener(v -> {
                select(all, swatch);
                hex.setText(color);
                changed.onChanged(color);
            });
            all.add(swatch);
            int size = px(R.dimen.touch_target_min);
            options.addView(swatch, new LinearLayout.LayoutParams(size, size));
            if (color.equalsIgnoreCase(current)) {
                selected = swatch;
            }
        }
        select(all, selected);
        hex.addTextChangedListener(new UploadInfoFragment.AfterChange(text -> {
            String clean = text.trim();
            if (HEX.matcher(clean).matches()) {
                changed.onChanged(clean);
            }
        }));
    }

    /** Menggambar ulang pilihan warna: yang terpilih bergaris tebal warna foreground, sisanya garis tipis. */
    private void select(List<View> options, @Nullable View selected) {
        int thin = px(R.dimen.border_width);
        int border = context.getColor(R.color.color_border);
        int foreground = context.getColor(R.color.color_foreground);
        // Area sentuh 48dp, lingkaran warna yang terlihat 32dp di tengahnya.
        int inset = (px(R.dimen.touch_target_min) - px(R.dimen.color_swatch_size)) / 2;
        for (View option : options) {
            boolean on = option == selected;
            option.setSelected(on);
            GradientDrawable shape = new GradientDrawable();
            shape.setStroke(on ? thin * 2 : thin, on ? foreground : border);
            if (option.getTag() instanceof Integer) {
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor((Integer) option.getTag());
                option.setBackground(new InsetDrawable(shape, inset));
            } else {
                shape.setCornerRadius(px(R.dimen.radius_small));
                shape.setColor(Color.TRANSPARENT);
                option.setBackground(new InsetDrawable(shape, 0, inset, 0, inset));
            }
        }
    }

    /** Peringatan kontras jika warna teks dan latar yang dipilih terlalu mirip (bagian 7.11). */
    private void updateContrast(ItemTryFieldBinding row, TrySession.Value value) {
        String fg = value.styles.get("color");
        String bg = value.styles.get("background-color");
        boolean low = fg != null && bg != null && contrast(fg, bg) < MIN_CONTRAST;
        row.contrastWarning.setVisibility(low ? View.VISIBLE : View.GONE);
        row.contrastWarning.setText(R.string.try_contrast);
    }

    static double contrast(String a, String b) {
        try {
            double la = luminance(Color.parseColor(expand(a)));
            double lb = luminance(Color.parseColor(expand(b)));
            return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
        } catch (IllegalArgumentException e) {
            return 21;
        }
    }

    private static String expand(String hex) {
        if (hex.length() == 4) {
            return "#" + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2) + hex.charAt(3) + hex.charAt(3);
        }
        return hex;
    }

    private static double luminance(int color) {
        double r = channel(Color.red(color));
        double g = channel(Color.green(color));
        double b = channel(Color.blue(color));
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private int px(int dimen) {
        return context.getResources().getDimensionPixelSize(dimen);
    }

    /** Untuk Uji isi panjang: teks sepanjang batas karakter. */
    static String longText(Context context, @Nullable Integer maxLength) {
        String unit = context.getString(R.string.try_long_text);
        int length = maxLength == null ? 200 : maxLength;
        StringBuilder text = new StringBuilder();
        while (text.length() < length) {
            text.append(unit);
        }
        return text.substring(0, length).trim();
    }
}
