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
import com.aris.templateapp.data.model.Invitation;
import com.aris.templateapp.data.model.Member;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.databinding.FragmentMembersBinding;
import com.aris.templateapp.databinding.ItemInvitationBinding;
import com.aris.templateapp.databinding.ItemMemberBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.workspace.WorkspaceUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Anggota organisasi. Semua anggota bisa melihat daftar; Admin & Pemilik bisa mengundang, mencabut undangan,
 * mengubah peran/jabatan, dan mengeluarkan anggota (Admin tidak bisa menyentuh Pemilik, bagian 3.1).
 */
@AndroidEntryPoint
public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private MembersViewModel viewModel;
    /** Pesan untuk aksi yang sedang berjalan, ditampilkan saat aksi selesai. */
    private int pendingMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
        viewModel.start(OrganizationArgs.id(this));
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.inviteButton.setOnClickListener(v -> {
            if (getChildFragmentManager().findFragmentByTag(InviteSheet.TAG) == null) {
                new InviteSheet().show(getChildFragmentManager(), InviteSheet.TAG);
            }
        });

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
        viewModel.getDone().observe(getViewLifecycleOwner(), event -> {
            String name = event.getContentIfNotHandled();
            if (name != null && pendingMessage != 0) {
                Snackbar.make(binding.getRoot(), getString(pendingMessage, name), Snackbar.LENGTH_SHORT).show();
                pendingMessage = 0;
            }
        });
    }

    private void bind(MembersViewModel.State state) {
        Organization org = state.organization;
        boolean manage = org.canManage();
        binding.memberCount.setText(getString(R.string.org_members_count, state.members.size(), org.maxMembers));
        binding.inviteButton.setVisibility(manage ? View.VISIBLE : View.GONE);
        binding.invitationsSection.setVisibility(manage && !state.invitations.isEmpty() ? View.VISIBLE : View.GONE);

        binding.invitationList.removeAllViews();
        for (Invitation invitation : state.invitations) {
            ItemInvitationBinding row = ItemInvitationBinding.inflate(getLayoutInflater(), binding.invitationList, true);
            row.text.setText(getString(R.string.invitation_row, getString(WorkspaceUi.roleLabel(invitation.role)),
                    OrgUi.date(invitation.expiresAt)));
            row.revokeButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.invitation_revoke_title)
                    .setMessage(R.string.invitation_revoke_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.invitation_revoke, (d, w) -> {
                        pendingMessage = R.string.invitation_revoked;
                        viewModel.revoke(invitation);
                    })
                    .show());
        }

        binding.memberList.removeAllViews();
        for (Member member : state.members) {
            ItemMemberBinding row = ItemMemberBinding.inflate(getLayoutInflater(), binding.memberList, true);
            boolean me = member.userId.equals(state.myUserId);
            row.initial.setText(AppBarAccount.initialOf(member.displayName));
            row.name.setText(me ? getString(R.string.member_you, member.displayName) : member.displayName);
            String role = getString(WorkspaceUi.roleLabel(member.role));
            row.detail.setText(member.positionTitle == null ? role
                    : getString(R.string.member_role_position, role, member.positionTitle));
            List<Action> actions = actionsFor(org, member, me);
            row.more.setVisibility(actions.isEmpty() ? View.GONE : View.VISIBLE);
            row.getRoot().setClickable(!actions.isEmpty());
            row.getRoot().setOnClickListener(actions.isEmpty() ? null : v -> showActions(member, actions));
        }
    }

    /** Aksi yang boleh dilakukan user terhadap anggota ini; sama dengan aturan backend (OrganizationAccess). */
    private static List<Action> actionsFor(Organization org, Member member, boolean me) {
        List<Action> actions = new ArrayList<>();
        if (!org.canManage()) {
            return actions;
        }
        // Admin tidak boleh menyentuh Pemilik sama sekali.
        if (member.role == WorkspaceRole.OWNER && !org.isOwner()) {
            return actions;
        }
        if (!me) {
            if (member.role == WorkspaceRole.EDITOR) {
                actions.add(Action.MAKE_ADMIN);
            } else {
                actions.add(Action.MAKE_EDITOR);
            }
        }
        actions.add(Action.POSITION);
        if (!me) {
            actions.add(Action.REMOVE);
        }
        return actions;
    }

    private enum Action {
        MAKE_ADMIN(R.string.member_make_admin),
        MAKE_EDITOR(R.string.member_make_editor),
        POSITION(R.string.member_edit_position),
        REMOVE(R.string.member_remove);

        final int label;

        Action(int label) {
            this.label = label;
        }
    }

    private void showActions(Member member, List<Action> actions) {
        CharSequence[] labels = new CharSequence[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            labels[i] = getString(actions.get(i).label);
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(member.displayName)
                .setItems(labels, (dialog, which) -> run(member, actions.get(which)))
                .show();
    }

    private void run(Member member, Action action) {
        switch (action) {
            case MAKE_ADMIN:
                pendingMessage = R.string.member_updated;
                viewModel.changeRole(member, WorkspaceRole.ADMIN);
                break;
            case MAKE_EDITOR:
                pendingMessage = R.string.member_updated;
                viewModel.changeRole(member, WorkspaceRole.EDITOR);
                break;
            case POSITION:
                OrgUi.textDialog(requireContext(), getString(R.string.member_position_title, member.displayName), null,
                        R.string.org_field_position, member.positionTitle, R.string.action_save, title -> {
                            pendingMessage = R.string.member_updated;
                            viewModel.changePosition(member, title);
                        });
                break;
            case REMOVE:
            default:
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.member_remove_title, member.displayName))
                        .setMessage(R.string.member_remove_body)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.member_remove_confirm, (d, w) -> {
                            pendingMessage = R.string.member_removed;
                            viewModel.remove(member);
                        })
                        .show();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kembali dari WhatsApp/aplikasi lain setelah membagikan kode: anggota baru mungkin sudah bergabung.
        if (viewModel.getState().getValue() != null) {
            viewModel.load();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
