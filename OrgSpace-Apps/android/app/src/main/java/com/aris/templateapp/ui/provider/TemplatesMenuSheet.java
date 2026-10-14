package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.StatusCountsDto;
import com.aris.templateapp.databinding.SheetTemplatesMenuBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bottom sheet tombol + di "Template Anda" (ala Google Drive): "Folder baru" dan pilihan status yang ditampilkan
 * (Semua, Tayang, Perlu perbaikan, ...) beserta jumlahnya. Pilihan status dulu berupa chip di atas daftar.
 * Data dibaca dari {@link ProviderTemplatesViewModel} milik Activity, jadi sheet tidak perlu argumen.
 */
public class TemplatesMenuSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TemplatesMenuSheet";

    private SheetTemplatesMenuBinding binding;
    /** id radio → nilai filter status di backend, urut sesuai tampilan. */
    private final Map<Integer, String> statusByRadio = new LinkedHashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetTemplatesMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProviderTemplatesViewModel viewModel =
                new ViewModelProvider(requireActivity()).get(ProviderTemplatesViewModel.class);

        binding.newFolder.setOnClickListener(v -> {
            ProviderTemplatesFragment parent = (ProviderTemplatesFragment) requireParentFragment();
            dismiss();
            parent.showCreateFolderDialog(null);
        });

        TemplateFolderDto folder = viewModel.getFolder();
        if (folder != null) {
            binding.showTitle.setText(getString(R.string.templates_menu_show_in_folder, folder.name));
        }

        StatusCountsDto counts = viewModel.getCounts();
        bind(R.id.status_all, ProviderTemplatesViewModel.ALL, R.string.filter_all, counts == null ? null : counts.all);
        bind(R.id.status_published, ProviderTemplatesViewModel.STATUS_PUBLISHED, R.string.filter_published,
                counts == null ? null : counts.published);
        bind(R.id.status_needs_fix, ProviderTemplatesViewModel.STATUS_NEEDS_FIX, R.string.filter_needs_fix,
                counts == null ? null : counts.needsFix);
        bind(R.id.status_checking, "checking", R.string.filter_checking, counts == null ? null : counts.checking);
        bind(R.id.status_draft, "draft", R.string.filter_draft, counts == null ? null : counts.draft);
        bind(R.id.status_disabled, "disabled", R.string.filter_disabled, counts == null ? null : counts.disabled);

        for (Map.Entry<Integer, String> entry : statusByRadio.entrySet()) {
            if (entry.getValue().equals(viewModel.getStatus())) {
                binding.statusGroup.check(entry.getKey());
            }
        }
        // Dipasang setelah check() di atas agar pilihan awal tidak dianggap sebagai pilihan user.
        binding.statusGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String status = statusByRadio.get(checkedId);
            if (status != null) {
                viewModel.setStatus(status);
                dismiss();
            }
        });
    }

    private void bind(int radioId, String status, @StringRes int label, @Nullable Long count) {
        statusByRadio.put(radioId, status);
        MaterialRadioButton radio = binding.statusGroup.findViewById(radioId);
        radio.setText(count == null ? getString(label) : getString(R.string.chip_with_count, getString(label), count));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
