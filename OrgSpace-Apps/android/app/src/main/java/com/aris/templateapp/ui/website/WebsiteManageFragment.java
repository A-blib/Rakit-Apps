package com.aris.templateapp.ui.website;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.PublishRequest;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteDetail;
import com.aris.templateapp.data.model.WebsiteStatus;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.databinding.FragmentWebsiteManageBinding;
import com.aris.templateapp.databinding.ItemMenuRowBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.aris.templateapp.ui.creator.ProjectUi;
import com.aris.templateapp.ui.editor.EditorNav;
import com.aris.templateapp.ui.organization.OrgUi;
import com.aris.templateapp.ui.workspace.WorkspaceViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Halaman kelola website (alur-pembuatan-website.md bagian 5), dibuka dari kartu tab Website. Dokumen itu hanya
 * mengatur kerangkanya: Terbitkan, Ringkasan, Riwayat versi, Domain, dan Pengaturan situs diisi fase publish,
 * sedangkan Edit tampilan menyambung ke editor cloud (fase T). Bagian yang tidak boleh diakses peran user
 * tidak ditampilkan (G3).
 */
@AndroidEntryPoint
public class WebsiteManageFragment extends Fragment {

    private static final int NOTE_MAX = 200;

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentWebsiteManageBinding binding;
    private WebsiteManageViewModel viewModel;
    @Nullable private Workspace workspace;
    @Nullable private WebsiteDetail detail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWebsiteManageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(WebsiteManageViewModel.class);
        viewModel.start(WebsiteArgs.organizationId(this), WebsiteArgs.websiteId(this));
        binding.toolbar.setTitle(WebsiteArgs.name(this));
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.toolbar.inflateMenu(R.menu.menu_website_manage);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            onMenu(item.getItemId());
            return true;
        });

        row(binding.summaryItem, R.drawable.ic_qr_code, R.string.website_section_summary);
        row(binding.versionsItem, R.drawable.ic_history, R.string.website_section_versions);
        row(binding.domainItem, R.drawable.ic_language, R.string.website_section_domain);
        row(binding.settingsItem, R.drawable.ic_settings, R.string.website_section_settings);
        soonRow(binding.contentItem, R.drawable.ic_article, R.string.website_section_content,
                R.string.website_content_desc);
        soonRow(binding.statsItem, R.drawable.ic_bar_chart, R.string.website_section_stats,
                R.string.website_stats_desc);
        soonRow(binding.inboxItem, R.drawable.ic_inbox, R.string.website_section_inbox, R.string.website_inbox_desc);

        binding.editButton.setOnClickListener(v -> openEditor());
        binding.publishButton.setOnClickListener(v -> onPublish());

        new ViewModelProvider(this).get(WorkspaceViewModel.class).getActive().observe(getViewLifecycleOwner(), active -> {
            workspace = active;
            if (detail != null) {
                bind(detail);
            }
        });
        viewModel.getDetail().observe(getViewLifecycleOwner(), resource -> {
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
            binding.publishButton.setEnabled(!busy && canPressPublish());
            binding.toolbar.getMenu().setGroupEnabled(0, !busy);
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), event -> {
            WebsiteManageViewModel.Outcome outcome = event.getContentIfNotHandled();
            if (outcome != null) {
                onDone(outcome);
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            ApiError error = event.getContentIfNotHandled();
            if (error != null) {
                String text = "CONFIRM_NAME_MISMATCH".equals(error.getCode())
                        ? getString(R.string.error_website_name_mismatch)
                        : ErrorMessages.forError(requireContext(), error);
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.load();
        }
    }

    private void bind(WebsiteDetail detail) {
        this.detail = detail;
        Website website = detail.website;
        boolean draft = website.status == WebsiteStatus.DRAFT;
        boolean admin = isAdmin();

        binding.toolbar.setTitle(website.name);
        binding.toolbar.getMenu().findItem(R.id.action_delete).setVisible(isOwner());
        binding.name.setText(website.name);
        WebsiteUi.bindStatusBadge(binding.statusBadge, website.status);
        String address = WebsiteUi.shortAddress(website.siteUrl);
        boolean hasUrl = WebsiteUi.canOpenWebsite(website);
        binding.address.setText(address == null ? getString(R.string.website_not_published) : address);
        binding.address.setTextColor(ContextCompat.getColor(requireContext(),
                hasUrl ? R.color.color_link : R.color.color_muted));
        if (hasUrl) {
            binding.address.setClickable(true);
            binding.address.setFocusable(true);
            binding.address.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_link_small, 0);
            binding.address.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.space_1));
            binding.address.setContentDescription(getString(R.string.website_open_address, address));
            binding.address.setOnClickListener(v -> WebsiteUi.openWebsite(requireContext(), website.siteUrl));
            binding.address.setOnLongClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && website.siteUrl != null) {
                    ClipData clip = ClipData.newPlainText("Website URL", website.siteUrl);
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(binding.getRoot(), R.string.publish_success_copied, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        } else {
            binding.address.setClickable(false);
            binding.address.setFocusable(false);
            binding.address.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            binding.address.setContentDescription(null);
            binding.address.setOnClickListener(null);
            binding.address.setOnLongClickListener(null);
        }
        binding.changed.setVisibility(website.isOnlineWithChanges() ? View.VISIBLE : View.GONE);
        binding.updated.setText(getString(R.string.website_updated_mode, WebsiteUi.updatedLine(requireContext(), website),
                getString(ProjectUi.modeLabel(website.mode))));
        binding.lock.setVisibility(website.lockedByName == null ? View.GONE : View.VISIBLE);
        if (website.lockedByName != null) {
            binding.lock.setText(getString(R.string.website_locked_by, website.lockedByName));
        }
        binding.thumbnail.setImageDrawable(null);
        thumbnailLoader.load(website.thumbnailUrl, binding.thumbnail, null);
        bindBanner(detail);

        binding.publishButton.setText(admin ? (draft ? R.string.website_publish_first : R.string.website_publish)
                : R.string.website_request_publish);
        binding.publishButton.setIconResource(admin ? R.drawable.ic_public : R.drawable.ic_send);
        binding.publishButton.setEnabled(canPressPublish());

        // Riwayat versi & Ringkasan berisi data terbit: untuk draft dijelaskan kapan munculnya.
        binding.summaryItem.body.setText(draft ? R.string.website_after_publish : R.string.website_summary_desc);
        binding.versionsItem.body.setText(draft ? R.string.website_after_publish : R.string.website_versions_desc);
        binding.domainItem.body.setText(draft ? R.string.website_after_publish : R.string.website_domain_desc);
        binding.settingsItem.body.setText(R.string.website_settings_desc);
        // Domain & Pengaturan situs hanya Admin/Pemilik (G3, alur-publish-website.md 3.1).
        binding.domainItem.getRoot().setVisibility(admin ? View.VISIBLE : View.GONE);
        binding.settingsItem.getRoot().setVisibility(admin ? View.VISIBLE : View.GONE);
    }

    /** Ditangguhkan (paling penting) → permintaan terbit yang masih terbuka → tanpa banner. */
    private void bindBanner(WebsiteDetail detail) {
        if (detail.website.status == WebsiteStatus.SUSPENDED) {
            binding.banner.setVisibility(View.VISIBLE);
            binding.banner.bind(StatusBannerView.Kind.ERROR, detail.suspensionReason == null
                    ? getString(R.string.website_suspended_no_reason)
                    : getString(R.string.website_suspended_body, detail.suspensionReason));
            return;
        }
        PublishRequest request = detail.openRequest;
        if (request == null) {
            binding.banner.setVisibility(View.GONE);
            return;
        }
        String when = WebsiteUi.ago(requireContext(), request.createdAt);
        binding.banner.setVisibility(View.VISIBLE);
        binding.banner.bind(StatusBannerView.Kind.INFO, isAdmin()
                ? getString(R.string.request_publish_pending,
                        request.requestedByName == null ? "" : request.requestedByName, when)
                : getString(R.string.request_publish_pending_me, when));
    }

    private void onPublish() {
        if (isAdmin()) {
            // Terbitkan dibangun di fase publish (P2–P4).
            showInfo(R.string.website_publish, R.string.website_publish_soon);
            return;
        }
        OrgUi.textDialog(requireContext(), getString(R.string.request_publish_title),
                getString(R.string.request_publish_body), R.string.request_publish_note_hint, null,
                R.string.request_publish_send, note -> {
                    if (note.trim().length() > NOTE_MAX) {
                        Snackbar.make(binding.getRoot(), R.string.error_publish_note_too_long, Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        viewModel.requestPublish(note);
                    }
                });
    }

    /** Website yang ditangguhkan tidak bisa diterbitkan maupun diminta terbit (SITE_SUSPENDED). */
    private boolean canPressPublish() {
        return detail != null && detail.website.status != WebsiteStatus.SUSPENDED;
    }

    private void openEditor() {
        if (detail == null) {
            return;
        }
        EditorNav.openWebsiteEditor(this, detail);
    }

    private void onMenu(int itemId) {
        if (detail == null) {
            return;
        }
        Website website = detail.website;
        if (itemId == R.id.action_rename) {
            WebsiteDialogs.rename(requireContext(), website, viewModel::rename);
        } else if (itemId == R.id.action_duplicate) {
            viewModel.duplicate();
        } else if (itemId == R.id.action_export) {
            showInfo(R.string.website_export, R.string.website_export_soon);
        } else if (itemId == R.id.action_delete) {
            WebsiteDialogs.delete(requireContext(), website, viewModel::delete);
        }
    }

    private void onDone(WebsiteManageViewModel.Outcome outcome) {
        switch (outcome.kind) {
            case RENAMED:
                Snackbar.make(binding.getRoot(), R.string.website_renamed, Snackbar.LENGTH_SHORT).show();
                break;
            case DUPLICATED:
                Snackbar.make(binding.getRoot(), getString(R.string.website_duplicated, outcome.name),
                        Snackbar.LENGTH_LONG).show();
                break;
            case REQUESTED:
                Snackbar.make(binding.getRoot(), R.string.request_publish_sent, Snackbar.LENGTH_SHORT).show();
                break;
            case DELETED:
            default:
                String name = detail == null ? "" : detail.website.name;
                View root = requireActivity().findViewById(android.R.id.content);
                NavHostFragment.findNavController(this).navigateUp();
                Snackbar.make(root, getString(R.string.website_deleted, name), Snackbar.LENGTH_SHORT).show();
                break;
        }
    }

    private void row(ItemMenuRowBinding row, @DrawableRes int icon, @StringRes int title) {
        row.icon.setImageResource(icon);
        row.title.setText(title);
        // Isi bagian-bagian ini dibangun di fase publish; untuk draft cukup penjelasan kapan munculnya.
        row.getRoot().setOnClickListener(v -> {
            boolean draft = detail != null && detail.website.status == WebsiteStatus.DRAFT;
            showInfo(title, draft ? R.string.website_after_publish : R.string.website_section_soon);
        });
    }

    private void soonRow(ItemMenuRowBinding row, @DrawableRes int icon, @StringRes int title, @StringRes int body) {
        row.icon.setImageResource(icon);
        row.title.setText(title);
        row.body.setText(body);
        row.badge.setVisibility(View.VISIBLE);
        row.chevron.setVisibility(View.GONE);
        row.getRoot().setEnabled(false);
    }

    private void showInfo(@StringRes int title, @StringRes int message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }

    private boolean isAdmin() {
        return workspace != null && workspace.getId().equals(WebsiteArgs.organizationId(this))
                && workspace.getRole() != WorkspaceRole.EDITOR;
    }

    private boolean isOwner() {
        return workspace != null && workspace.getId().equals(WebsiteArgs.organizationId(this))
                && workspace.getRole() == WorkspaceRole.OWNER;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
