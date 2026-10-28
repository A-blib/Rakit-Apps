package com.aris.templateapp.ui.organization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.databinding.FragmentOrganizationBinding;
import com.aris.templateapp.databinding.ItemMenuRowBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.workspace.WorkspaceUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar Organisasi (dari pemilih ruang kerja → "Kelola ruang kerja ini"). Menu yang tampil mengikuti peran
 * (revisi-fondasi-orgspace.md 3.1); ruang pribadi tidak punya Anggota dan Serah terima.
 */
@AndroidEntryPoint
public class OrganizationFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentOrganizationBinding binding;
    private OrganizationViewModel viewModel;
    @Nullable private Organization organization;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String id = OrganizationArgs.id(this);
        viewModel = new ViewModelProvider(this).get(OrganizationViewModel.class);
        viewModel.start(id);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        bindRow(binding.profileItem, R.drawable.ic_badge, R.string.org_menu_profile, R.string.org_menu_profile_desc,
                R.id.organizationProfileFragment);
        bindRow(binding.membersItem, R.drawable.ic_groups, R.string.org_menu_members, R.string.org_menu_members_desc,
                R.id.membersFragment);
        bindRow(binding.activityItem, R.drawable.ic_history, R.string.org_menu_activity,
                R.string.org_menu_activity_desc, R.id.activityFragment);
        bindRow(binding.portalItem, R.drawable.ic_key, R.string.org_menu_portal, R.string.org_menu_portal_desc,
                R.id.portalAccessFragment);
        bindRow(binding.transferItem, R.drawable.ic_swap_horiz, R.string.org_menu_transfer,
                R.string.org_menu_transfer_desc, R.id.transferOwnershipFragment);
        binding.leaveItem.setOnClickListener(v -> confirmLeave());
        binding.deleteItem.setOnClickListener(v -> confirmDelete());

        viewModel.getOrganization().observe(getViewLifecycleOwner(), resource -> {
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
        viewModel.isBusy().observe(getViewLifecycleOwner(), busy -> {
            binding.leaveItem.setEnabled(!busy);
            binding.deleteItem.setEnabled(!busy);
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        viewModel.getExit().observe(getViewLifecycleOwner(), event -> {
            var exit = event.getContentIfNotHandled();
            if (exit != null && organization != null) {
                View root = requireActivity().findViewById(android.R.id.content);
                String message = getString(exit == OrganizationViewModel.Exit.LEFT ? R.string.org_left
                        : R.string.org_deleted, organization.name);
                OrgUi.backToDashboard(this);
                Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kembali dari Profil/Anggota/Serah terima: nama, logo, jumlah anggota, atau peran bisa sudah berubah.
        if (organization != null) {
            viewModel.load();
        }
    }

    private void bind(Organization org) {
        organization = org;
        binding.toolbar.setTitle(org.name);
        binding.name.setText(org.name);
        OrgUi.bindLogo(binding.logo, thumbnailLoader, org.name, org.logoUrl);
        binding.role.setText(org.personal ? getString(R.string.workspace_personal_badge)
                : getString(R.string.org_my_role, getString(WorkspaceUi.roleLabel(org.myRole))));
        binding.memberCount.setText(getString(R.string.org_members_count, (int) org.memberCount, org.maxMembers));
        binding.memberCount.setVisibility(org.personal ? View.GONE : View.VISIBLE);

        // Ruang pribadi bukan organisasi; kata "organisasi" di menunya akan membingungkan.
        binding.profileItem.title.setText(org.personal ? R.string.org_menu_profile_personal : R.string.org_menu_profile);
        binding.deleteItem.setText(org.personal ? R.string.org_menu_delete_personal : R.string.org_menu_delete);
        binding.membersItem.getRoot().setVisibility(org.personal ? View.GONE : View.VISIBLE);
        binding.transferItem.getRoot().setVisibility(!org.personal && org.isOwner() ? View.VISIBLE : View.GONE);
        // Hanya Pemilik yang mengatur kredensial Portal (berlaku juga untuk website di ruang pribadi).
        binding.portalItem.getRoot().setVisibility(org.isOwner() ? View.VISIBLE : View.GONE);
        // Ruang pribadi tidak bisa ditinggalkan (pemiliknya satu-satunya anggota); hapus saja jika tidak dipakai.
        binding.leaveItem.setVisibility(org.personal ? View.GONE : View.VISIBLE);
        binding.deleteItem.setVisibility(org.isOwner() ? View.VISIBLE : View.GONE);
    }

    private void bindRow(ItemMenuRowBinding row, @DrawableRes int icon, @StringRes int title, @StringRes int body,
                         @IdRes int destination) {
        row.icon.setImageResource(icon);
        row.title.setText(title);
        row.body.setText(body);
        row.getRoot().setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(destination, OrganizationArgs.of(OrganizationArgs.id(this))));
    }

    private void confirmLeave() {
        if (organization == null) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.org_leave_title, organization.name))
                .setMessage(R.string.org_leave_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.org_leave_confirm, (dialog, which) -> viewModel.leave())
                .show();
    }

    private void confirmDelete() {
        if (organization == null) {
            return;
        }
        boolean personal = organization.personal;
        OrgUi.textDialog(requireContext(), getString(R.string.org_delete_title, organization.name),
                getString(personal ? R.string.org_delete_body_personal : R.string.org_delete_body),
                personal ? R.string.org_field_name_personal : R.string.org_field_name, null,
                R.string.org_delete_confirm, viewModel::delete);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
