package com.aris.templateapp.ui.organization;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Invitation;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.databinding.SheetInviteBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

/**
 * Undang anggota (revisi-fondasi-orgspace.md F1–F3): pilih peran (Editor bawaan) → buat kode → Salin / Bagikan.
 * Kode hanya tampil sekali karena server menyimpan hash-nya saja. Memakai ViewModel milik layar Anggota.
 */
public class InviteSheet extends BottomSheetDialogFragment {

    public static final String TAG = "InviteSheet";

    private SheetInviteBinding binding;
    private MembersViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetInviteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireParentFragment()).get(MembersViewModel.class);
        binding.roleGroup.setOnCheckedStateChangeListener((group, ids) -> binding.roleDescription.setText(
                ids.contains(R.id.role_admin) ? R.string.invite_role_admin_desc : R.string.invite_role_editor_desc));
        binding.createButton.setOnClickListener(v -> viewModel.createInvitation(
                binding.roleGroup.getCheckedChipId() == R.id.role_admin ? WorkspaceRole.ADMIN : WorkspaceRole.EDITOR));
        viewModel.isBusy().observe(getViewLifecycleOwner(), busy -> binding.createButton.setEnabled(!busy));
        viewModel.getInvitationCreated().observe(getViewLifecycleOwner(), event -> {
            Invitation invitation = event.getContentIfNotHandled();
            if (invitation != null) {
                showCode(invitation);
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void showCode(Invitation invitation) {
        binding.chooseSection.setVisibility(View.GONE);
        binding.codeSection.setVisibility(View.VISIBLE);
        String until = OrgUi.date(invitation.expiresAt);
        binding.code.setText(invitation.code);
        binding.validUntil.setText(getString(R.string.invite_valid_until, until));
        binding.copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.invite_code_label), invitation.code));
            Snackbar.make(binding.getRoot(), R.string.invite_copied, Snackbar.LENGTH_SHORT).show();
        });
        binding.shareButton.setOnClickListener(v -> {
            String name = organizationName();
            Intent send = new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_share_text, name, invitation.code, until));
            startActivity(Intent.createChooser(send, getString(R.string.invite_share_chooser)));
        });
    }

    private String organizationName() {
        var state = viewModel.getState().getValue();
        Organization org = state == null || state.getData() == null ? null : state.getData().organization;
        return org == null ? "" : org.name;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
