package com.aris.templateapp.ui.editor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.aris.templateapp.R;
import com.aris.templateapp.core.template.CustomCss;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog pemilih warna (alur-buat-website-via-template.md bagian 6.5): palet warna cepat + kode hex sendiri.
 * "Asli" mengembalikan warna bawaan template. Hanya warna hex valid yang bisa dipakai.
 */
final class ColorPickerDialog {

    interface Picked {
        /** @param color "#rrggbb", atau null = kembali ke warna asli */
        void onPicked(@Nullable String color);
    }

    private ColorPickerDialog() {
    }

    static void show(Context context, @Nullable String current, Picked picked) {
        int padding = px(context, R.dimen.screen_padding_horizontal);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(padding, px(context, R.dimen.space_2), padding, 0);

        // ChipGroup dipakai sebagai wadah yang otomatis turun baris; isinya View biasa (lebih ringan dari Chip).
        ChipGroup palette = new ChipGroup(context);
        palette.setChipSpacing(px(context, R.dimen.space_1));
        content.addView(palette);

        TextInputLayout layout = new TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layout.setHint(context.getString(R.string.try_color_custom));
        TextInputEditText hex = new TextInputEditText(layout.getContext());
        hex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hex.setMaxLines(1);
        hex.setText(CustomCss.color(current));
        layout.addView(hex);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = px(context, R.dimen.space_3);
        content.addView(layout, params);

        List<View> swatches = new ArrayList<>();
        int size = px(context, R.dimen.touch_target_min);
        for (String color : context.getResources().getStringArray(R.array.try_color_presets)) {
            View swatch = new View(context);
            swatch.setTag(color);
            swatch.setContentDescription(context.getString(R.string.cd_color, color));
            swatch.setOnClickListener(v -> {
                hex.setText(color.toLowerCase(java.util.Locale.ROOT));
                select(context, swatches, color);
            });
            swatches.add(swatch);
            palette.addView(swatch, new ViewGroup.LayoutParams(size, size));
        }
        select(context, swatches, current);
        hex.addTextChangedListener(new EditorForm.AfterChange(text -> {
            boolean valid = CustomCss.color(text) != null;
            layout.setError(valid || text.trim().isEmpty() ? null : context.getString(R.string.editor_color_invalid));
            select(context, swatches, text);
        }));

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.editor_color_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setNeutralButton(R.string.try_color_original, (d, w) -> picked.onPicked(null))
                .setPositiveButton(R.string.editor_use, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String color = CustomCss.color(String.valueOf(hex.getText()));
            if (color == null) {
                layout.setError(context.getString(R.string.editor_color_invalid));
                return;
            }
            picked.onPicked(color);
            dialog.dismiss();
        }));
        dialog.show();
    }

    /** Yang terpilih bergaris tebal warna foreground; sisanya garis tipis. */
    private static void select(Context context, List<View> swatches, @Nullable String selected) {
        String chosen = CustomCss.color(selected);
        int thin = px(context, R.dimen.border_width);
        int inset = (px(context, R.dimen.touch_target_min) - px(context, R.dimen.color_swatch_size)) / 2;
        for (View swatch : swatches) {
            String color = (String) swatch.getTag();
            boolean on = color.equalsIgnoreCase(chosen == null ? "" : chosen);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(Color.parseColor(color));
            shape.setStroke(on ? thin * 2 : thin, context.getColor(on ? R.color.color_foreground : R.color.color_border));
            swatch.setBackground(new InsetDrawable(shape, inset));
            swatch.setSelected(on);
        }
    }

    private static int px(Context context, int dimen) {
        return context.getResources().getDimensionPixelSize(dimen);
    }
}
