package com.aris.templateapp.ui.upload;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.data.remote.dto.TechInfoDto;
import com.aris.templateapp.databinding.DialogRenameProjectBinding;
import com.aris.templateapp.databinding.ItemRowBinding;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * Panel bottom sheet editor Tandai (alur-fitur-upload.md bagian 7.10): daftar elemen (A2), panel semua isian + tema
 * global (A5 & 7.11), dan daftar section beserta koreksinya (A1).
 */
final class MarkPanels {

    /** Satu elemen dari {@code RakitMark.elementsIn}. */
    static class ElementItem {
        int id;
        String tag;
        String kind;
        String text;
        boolean hidden;
    }

    interface ElementPicked {
        void onPicked(int tplId);
    }

    interface FieldPicked {
        void onPicked(MarkingDto.Field field);
    }

    interface ThemeChanged {
        void onChanged(String var, String label, String type);
    }

    interface SectionAction {
        void rename(MarkingDto.Section section, String name);

        void merge(MarkingDto.Section section);

        void delete(MarkingDto.Section section);

        void open(int index);
    }

    private MarkPanels() {
    }

    // ---------- daftar elemen ----------

    static void showElements(Fragment fragment, List<ElementItem> items, MarkingEditor editor, String page,
                             ElementPicked picked) {
        BottomSheetDialog sheet = new BottomSheetDialog(fragment.requireContext());
        LinearLayout list = container(fragment, sheet, fragment.getString(R.string.mark_elements_title));
        if (items.isEmpty()) {
            addEmpty(list, R.string.mark_elements_empty);
        }
        for (ElementItem item : items) {
            ItemRowBinding row = ItemRowBinding.inflate(LayoutInflater.from(list.getContext()), list, true);
            boolean marked = editor.fieldOf(page, item.id) != null;
            row.icon.setImageResource(marked ? R.drawable.ic_check_circle : iconOf(item.kind));
            row.title.setText(item.text == null || item.text.isEmpty() ? "<" + item.tag + ">" : item.text);
            String detail = item.tag + (item.hidden ? " · " + fragment.getString(R.string.mark_element_hidden) : "");
            row.detail.setText(detail);
            row.getRoot().setOnClickListener(v -> {
                sheet.dismiss();
                picked.onPicked(item.id);
            });
        }
        sheet.show();
    }

    private static int iconOf(String kind) {
        if ("image".equals(kind)) {
            return R.drawable.ic_web;
        }
        if ("link".equals(kind) || "button".equals(kind)) {
            return R.drawable.ic_link;
        }
        return R.drawable.ic_edit;
    }

    // ---------- semua isian + tema global ----------

    static void showFields(Fragment fragment, MarkingEditor editor, List<TechInfoDto.CssVariableDto> variables,
                           FieldPicked picked, ThemeChanged themeChanged) {
        BottomSheetDialog sheet = new BottomSheetDialog(fragment.requireContext());
        List<MarkingDto.Field> fields = editor.fields();
        LinearLayout list = container(fragment, sheet, fragment.getString(R.string.mark_fields_title, fields.size()));
        if (fields.isEmpty()) {
            addEmpty(list, R.string.mark_fields_empty);
        }
        for (MarkingDto.Field field : fields) {
            ItemRowBinding row = ItemRowBinding.inflate(LayoutInflater.from(list.getContext()), list, true);
            row.icon.setImageResource(R.drawable.ic_check_circle);
            row.title.setText(field.label);
            MarkingDto.Page page = field.elements.isEmpty() ? null : editor.page(field.elements.get(0).page);
            row.detail.setText(fragment.getString(R.string.mark_field_detail, typeLabel(fragment, field.type),
                    page == null ? "" : page.name, field.elements.size()));
            row.getRoot().setOnClickListener(v -> {
                sheet.dismiss();
                picked.onPicked(field);
            });
        }

        if (!variables.isEmpty()) {
            TextView title = new TextView(list.getContext());
            title.setTextAppearance(R.style.TextAppearance_App_Label);
            title.setText(R.string.mark_theme_title);
            title.setPadding(0, px(list.getContext(), R.dimen.space_6), 0, px(list.getContext(), R.dimen.space_1));
            list.addView(title);
            TextView body = new TextView(list.getContext());
            body.setTextAppearance(R.style.TextAppearance_App_BodySmall);
            body.setTextColor(ContextCompat.getColor(list.getContext(), R.color.color_muted));
            body.setText(R.string.mark_theme_body);
            list.addView(body);
            for (TechInfoDto.CssVariableDto variable : variables) {
                addThemeRow(list, editor, variable, themeChanged);
            }
        }
        sheet.show();
    }

    private static void addThemeRow(LinearLayout list, MarkingEditor editor, TechInfoDto.CssVariableDto variable,
                                    ThemeChanged changed) {
        Context context = list.getContext();
        MarkingDto.ThemeVar existing = editor.theme(variable.name);
        String type = MarkingEditor.themeType(variable.value);
        CheckBox check = new CheckBox(context);
        check.setText(context.getString(R.string.mark_theme_var, variable.name, variable.value));
        check.setChecked(existing != null);
        list.addView(check);
        TextInputLayout layout = new TextInputLayout(context);
        layout.setHint(context.getString(R.string.mark_theme_label_hint));
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setSingleLine(true);
        input.setText(existing != null ? existing.label : MarkingEditor.themeLabel(variable.name));
        layout.addView(input);
        layout.setVisibility(existing != null ? View.VISIBLE : View.GONE);
        list.addView(layout);
        check.setOnCheckedChangeListener((v, isChecked) -> {
            layout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            changed.onChanged(variable.name, isChecked ? OnboardingUi.text(input) : null, type);
        });
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && check.isChecked()) {
                changed.onChanged(variable.name, OnboardingUi.text(input), type);
            }
        });
    }

    static String typeLabel(Fragment fragment, String type) {
        switch (type == null ? "" : type) {
            case "paragraph":
                return fragment.getString(R.string.mark_type_paragraph);
            case "image":
                return fragment.getString(R.string.mark_type_image);
            case "link":
                return fragment.getString(R.string.mark_type_link);
            case "button":
                return fragment.getString(R.string.mark_type_button);
            case "text":
            default:
                return fragment.getString(R.string.mark_type_text);
        }
    }

    // ---------- section ----------

    static void showSections(Fragment fragment, List<MarkingDto.Section> sections, SectionAction action) {
        BottomSheetDialog sheet = new BottomSheetDialog(fragment.requireContext());
        LinearLayout list = container(fragment, sheet, fragment.getString(R.string.mark_sections_title));
        for (int i = 0; i < sections.size(); i++) {
            MarkingDto.Section section = sections.get(i);
            int index = i;
            ItemRowBinding row = ItemRowBinding.inflate(LayoutInflater.from(list.getContext()), list, true);
            row.icon.setImageResource(R.drawable.ic_sections);
            row.title.setText(fragment.getString(R.string.mark_section_row, i + 1, section.name));
            row.detail.setVisibility(View.GONE);
            row.chevron.setImageResource(R.drawable.ic_more_vert);
            row.chevron.setVisibility(View.VISIBLE);
            makeMenuButton(row.chevron, fragment.getString(R.string.cd_upload_more, section.name));
            row.chevron.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(fragment.requireContext(), v);
                menu.getMenu().add(0, 1, 0, R.string.mark_section_rename);
                if (index < sections.size() - 1) {
                    menu.getMenu().add(0, 2, 1, R.string.mark_section_merge);
                }
                if (sections.size() > 1) {
                    menu.getMenu().add(0, 3, 2, R.string.mark_section_delete);
                }
                menu.setOnMenuItemClickListener(item -> {
                    sheet.dismiss();
                    if (item.getItemId() == 1) {
                        rename(fragment, section, action);
                    } else if (item.getItemId() == 2) {
                        action.merge(section);
                    } else {
                        action.delete(section);
                    }
                    return true;
                });
                menu.show();
            });
            row.getRoot().setOnClickListener(v -> {
                sheet.dismiss();
                action.open(index);
            });
        }
        sheet.show();
    }

    /** Ikon panah di baris biasa hanya hiasan; di sini ia menjadi tombol menu, jadi butuh area sentuh 48dp dan label. */
    private static void makeMenuButton(ImageView icon, String description) {
        Resources res = icon.getResources();
        int size = res.getDimensionPixelSize(R.dimen.touch_target_min);
        int padding = (size - res.getDimensionPixelSize(R.dimen.icon_size_small)) / 2;
        ViewGroup.LayoutParams params = icon.getLayoutParams();
        params.width = size;
        params.height = size;
        icon.setLayoutParams(params);
        icon.setPadding(padding, padding, padding, padding);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        icon.setContentDescription(description);
        TypedValue ripple = new TypedValue();
        icon.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, ripple, true);
        icon.setBackgroundResource(ripple.resourceId);
    }

    private static void rename(Fragment fragment, MarkingDto.Section section, SectionAction action) {
        DialogRenameProjectBinding binding = DialogRenameProjectBinding.inflate(fragment.getLayoutInflater());
        binding.nameLayout.setHint(fragment.getString(R.string.mark_section_name));
        binding.nameLayout.setCounterMaxLength(40);
        binding.nameInput.setText(section.name);
        binding.nameInput.selectAll();
        new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(R.string.mark_section_rename)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.mark_save, (d, w) -> action.rename(section, OnboardingUi.text(binding.nameInput)))
                .show();
    }

    // ---------- helper ----------

    private static LinearLayout container(Fragment fragment, BottomSheetDialog sheet, String title) {
        Context context = fragment.requireContext();
        NestedScrollView scroll = new NestedScrollView(context);
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        int side = context.getResources().getDimensionPixelSize(R.dimen.screen_padding_horizontal);
        list.setPadding(side, px(context, R.dimen.space_4), side, px(context, R.dimen.space_6));
        TextView heading = new TextView(context);
        heading.setTextAppearance(R.style.TextAppearance_App_Title);
        heading.setText(title);
        heading.setPadding(0, 0, 0, px(context, R.dimen.space_2));
        list.addView(heading);
        scroll.addView(list);
        sheet.setContentView(scroll);
        return list;
    }

    private static void addEmpty(LinearLayout list, int text) {
        TextView empty = new TextView(list.getContext());
        empty.setTextAppearance(R.style.TextAppearance_App_Body);
        empty.setTextColor(ContextCompat.getColor(list.getContext(), R.color.color_muted));
        empty.setText(text);
        list.addView(empty);
    }

    private static int px(Context context, int dimen) {
        return context.getResources().getDimensionPixelSize(dimen);
    }
}
