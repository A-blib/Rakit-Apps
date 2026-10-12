package com.aris.templateapp.ui.workspace;

import android.view.View;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.OrgCategories;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * Pasangan chip Jenjang + Kategori (revisi-fondasi-orgspace.md bagian 6): kategori dibangun ulang setiap jenjang
 * berganti, dan pilihan kategori lama dipertahankan bila masih tersedia untuk jenjang baru.
 * Dipakai form "Buat ruang organisasi" dan Profil organisasi.
 */
public class CategoryChips {

    /** Dipanggil saat user mengganti jenjang atau kategori (mis. untuk menyembunyikan pesan error). */
    public interface Listener {
        void onChanged();
    }

    private final ChipGroup levelGroup;
    private final ChipGroup categoryGroup;
    @Nullable private final View emptyCategories;
    @Nullable private OrgCategories categories;
    @Nullable private String level;
    @Nullable private String category;
    private boolean enabled = true;

    public CategoryChips(ChipGroup levelGroup, ChipGroup categoryGroup, @Nullable View emptyCategories,
                         Listener listener) {
        this.levelGroup = levelGroup;
        this.categoryGroup = categoryGroup;
        this.emptyCategories = emptyCategories;
        levelGroup.setOnCheckedStateChangeListener((group, ids) -> {
            level = checkedValue(group, ids);
            showCategories();
            listener.onChanged();
        });
        categoryGroup.setOnCheckedStateChangeListener((group, ids) -> {
            category = checkedValue(group, ids);
            listener.onChanged();
        });
    }

    /** Mengisi chip jenjang sekali; {@code initialLevel}/{@code initialCategory} = nilai tersimpan (boleh null). */
    public void bind(OrgCategories data, @Nullable String initialLevel, @Nullable String initialCategory) {
        if (categories != null) {
            return;
        }
        categories = data;
        category = initialCategory;
        levelGroup.removeAllViews();
        for (OrgCategories.Option option : data.getLevels()) {
            Chip chip = chip(option);
            levelGroup.addView(chip);
            if (option.value.equals(initialLevel)) {
                // Memicu listener → level terisi dan kategori dibangun dengan pilihan awal.
                chip.setChecked(true);
            }
        }
        showCategories();
    }

    @Nullable
    public String getLevel() {
        return level;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    /** Mode baca (Editor membuka Profil organisasi). */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setChildrenEnabled(levelGroup, enabled);
        setChildrenEnabled(categoryGroup, enabled);
    }

    private void showCategories() {
        if (categories == null) {
            return;
        }
        List<OrgCategories.Category> options = categories.categoriesFor(level);
        String keep = category;
        category = null;
        categoryGroup.removeAllViews();
        for (OrgCategories.Category option : options) {
            Chip chip = chip(option);
            categoryGroup.addView(chip);
            if (option.value.equals(keep)) {
                chip.setChecked(true);
            }
        }
        setChildrenEnabled(categoryGroup, enabled);
        if (emptyCategories != null) {
            emptyCategories.setVisibility(options.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private Chip chip(OrgCategories.Option option) {
        Chip chip = new Chip(levelGroup.getContext());
        chip.setText(option.label);
        chip.setTag(option.value);
        chip.setCheckable(true);
        chip.setId(View.generateViewId());
        return chip;
    }

    @Nullable
    private static String checkedValue(ChipGroup group, List<Integer> ids) {
        if (ids.isEmpty()) {
            return null;
        }
        View chip = group.findViewById(ids.get(0));
        return chip == null ? null : (String) chip.getTag();
    }

    private static void setChildrenEnabled(ChipGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }
}
