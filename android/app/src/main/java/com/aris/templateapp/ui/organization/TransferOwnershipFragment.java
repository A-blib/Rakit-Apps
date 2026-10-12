package com.aris.templateapp.ui.organization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Member;
import com.aris.templateapp.databinding.FragmentTransferOwnershipBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.workspace.WorkspaceUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Serah terima kepemilikan (pergantian kepengurusan): anggota terpilih menjadi Pemilik, Pemilik lama tetap sebagai
 * Admin/Editor atau keluar. Website dan datanya tetap milik organisasi.
 */
@AndroidEntryPoint
public class TransferOwnershipFragment extends Fragment {

    private FragmentTransferOwnershipBinding binding;
    private MembersViewModel viewModel;
    private String organizationName = "";
    private String keepMeAs = "admin";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransferOwnershipBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
        viewModel.start(OrganizationArgs.id(this));
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getState().observe(getViewLifecycleOwner(), resource -> {
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
                    if (resource.getData().members.size() <= 1) {
                        binding.content.setVisibility(View.GONE);
                        binding.stateView.showEmpty(getString(R.string.transfer_no_members));
                    } else {
                        binding.stateView.hide(binding.content, () -> bind(resource.getData()));
                    }
                    break;
            }
        });
        binding.memberGroup.setOnCheckedChangeListener((group, id) -> binding.memberError.setVisibility(View.GONE));
        binding.submitButton.setOnClickListener(v -> confirm());
        viewModel.isBusy().observe(getViewLifecycleOwner(), busy -> binding.submitButton.setEnabled(!busy));
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), event -> {
            String receiver = event.getContentIfNotHandled();
            if (receiver == null) {
                return;
            }
            View root = requireActivity().findViewById(android.R.id.content);
            String message = getString(R.string.transfer_done, receiver, organizationName);
            if ("leave".equals(keepMeAs)) {
                OrgUi.backToDashboard(this);
            } else {
                NavHostFragment.findNavController(this).navigateUp();
            }
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
        });
    }

    private void bind(MembersViewModel.State state) {
        organizationName = state.organization.name;
        binding.memberGroup.removeAllViews();
        for (Member member : state.members) {
            if (member.userId.equals(state.myUserId)) {
                continue;
            }
            MaterialRadioButton radio = new MaterialRadioButton(requireContext());
            radio.setId(View.generateViewId());
            radio.setTag(member);
            radio.setMinHeight(getResources().getDimensionPixelSize(R.dimen.touch_target_min));
            radio.setText(getString(R.string.member_role_position, member.displayName,
                    getString(WorkspaceUi.roleLabel(member.role))));
            binding.memberGroup.addView(radio);
        }
    }

    private void confirm() {
        View checked = binding.memberGroup.findViewById(binding.memberGroup.getCheckedRadioButtonId());
        if (checked == null) {
            binding.memberError.setVisibility(View.VISIBLE);
            return;
        }
        Member receiver = (Member) checked.getTag();
        int keep = binding.keepGroup.getCheckedRadioButtonId();
        keepMeAs = keep == R.id.keep_leave ? "leave" : keep == R.id.keep_editor ? "editor" : "admin";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.transfer_confirm_title, receiver.displayName))
                .setMessage(R.string.transfer_confirm_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.transfer_submit, (d, w) -> viewModel.transfer(receiver, keepMeAs))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
