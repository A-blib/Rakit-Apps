package com.aris.templateapp.ui.organization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.PortalCredential;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.databinding.DialogPortalCredentialBinding;
import com.aris.templateapp.databinding.FragmentPortalAccessBinding;
import com.aris.templateapp.databinding.ItemMenuRowBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.workspace.WorkspaceUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Akses Portal website (Kelola organisasi, hanya Pemilik). Satu username + password per peran berlaku di Portal
 * semua website organisasi ini; username menentukan peran saat login. Password tidak bisa dilihat ulang.
 */
@AndroidEntryPoint
public class PortalAccessFragment extends Fragment {

    private FragmentPortalAccessBinding binding;
    private PortalAccessViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPortalAccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(PortalAccessViewModel.class);
        viewModel.start(OrganizationArgs.id(this));
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getCredentials().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.stateView.showLoading();
                    break;
                case ERROR:
                    binding.content.setVisibility(View.GONE);
                    binding.stateView.showError(ErrorMessages.forError(requireContext(), resource.getError()),
                            viewModel::load);
                    break;
                case SUCCESS:
                default:
                    binding.stateView.hide(binding.content, () -> bind(resource.getData()));
                    break;
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        viewModel.getSaved().observe(getViewLifecycleOwner(), event -> {
            WorkspaceRole role = event.getContentIfNotHandled();
            if (role != null) {
                Snackbar.make(binding.getRoot(), getString(R.string.portal_saved, getString(WorkspaceUi.roleLabel(role))),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void bind(List<PortalCredential> credentials) {
        bindRow(binding.ownerItem, WorkspaceRole.OWNER, R.string.portal_role_owner, credentials);
        bindRow(binding.adminItem, WorkspaceRole.ADMIN, R.string.portal_role_admin, credentials);
        bindRow(binding.editorItem, WorkspaceRole.EDITOR, R.string.portal_role_editor, credentials);
    }

    private void bindRow(ItemMenuRowBinding row, WorkspaceRole role, @StringRes int title,
                         List<PortalCredential> credentials) {
        PortalCredential credential = null;
        for (PortalCredential c : credentials) {
            if (c.role == role) {
                credential = c;
            }
        }
        row.icon.setImageResource(R.drawable.ic_key);
        row.title.setText(title);
        boolean stale = credential != null && credential.changeRecommended;
        row.body.setText(credential == null ? getString(R.string.portal_role_unset)
                : getString(stale ? R.string.portal_role_stale : R.string.portal_role_set, credential.username,
                OrgUi.date(credential.updatedAt)));
        // Baris dipakai ulang saat daftar dimuat ulang: kembalikan warna bawaan dulu, baru beri warna peringatan.
        TextViewCompat.setTextAppearance(row.body, R.style.TextAppearance_App_BodySmall);
        if (stale) {
            row.body.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_warning));
        }
        String username = credential == null ? null : credential.username;
        row.getRoot().setOnClickListener(v -> openEditor(role, username));
    }

    private void openEditor(WorkspaceRole role, @Nullable String username) {
        DialogPortalCredentialBinding form = DialogPortalCredentialBinding.inflate(getLayoutInflater());
        if (username != null) {
            form.usernameInput.setText(username);
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.portal_dialog_title, getString(WorkspaceUi.roleLabel(role))))
                .setView(form.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null)
                .show();
        // Tombol Simpan dipasang setelah show() agar dialog tetap terbuka saat isian salah.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = form.usernameInput.getText() == null ? "" : form.usernameInput.getText().toString();
            String password = form.passwordInput.getText() == null ? "" : form.passwordInput.getText().toString();
            Integer nameError = PortalCredentialRules.usernameError(name);
            Integer passwordError = PortalCredentialRules.passwordError(password);
            form.usernameLayout.setError(nameError == null ? null : getString(nameError));
            form.passwordLayout.setError(passwordError == null ? null : getString(passwordError));
            if (nameError == null && passwordError == null) {
                viewModel.save(role, name, password);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
