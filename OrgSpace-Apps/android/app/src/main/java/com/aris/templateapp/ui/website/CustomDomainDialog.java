package com.aris.templateapp.ui.website;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.BuildConfig;
import com.aris.templateapp.R;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.DomainInstructionDto;
import com.aris.templateapp.data.remote.dto.DomainResponseDto;
import com.aris.templateapp.data.repository.WebsiteRepository;
import com.aris.templateapp.databinding.DialogCustomDomainBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.editor.FullScreenDialogs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar pengaturan custom domain website (alur-publish-website.md 7.10 & 8.7).
 * Memungkinkan user memasang domain miliknya sendiri (mis. webUser.my.id, osis.id, dll.)
 * lengkap dengan instruksi DNS, verifikasi otomatis, serta pengelolaan status domain.
 */
@AndroidEntryPoint
public class CustomDomainDialog extends DialogFragment {

    private static final String TAG = "custom-domain-dialog";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_NAME = "name";
    private static final String ARG_SLUG = "slug";
    private static final String ARG_SITE_URL = "site_url";

    @Inject
    WebsiteRepository repository;

    @Inject
    AppExecutors executors;

    private DialogCustomDomainBinding binding;
    private String projectId;
    private String websiteName;
    private String slug;
    private String defaultSiteUrl;
    @Nullable private DomainResponseDto currentDomain;
    @Nullable private Runnable onDomainChangedListener;

    public static void show(Fragment parent, String projectId, String name, String slug, String siteUrl,
                            @Nullable Runnable onDomainChanged) {
        CustomDomainDialog dialog = new CustomDomainDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_NAME, name);
        args.putString(ARG_SLUG, slug);
        args.putString(ARG_SITE_URL, siteUrl);
        dialog.setArguments(args);
        dialog.onDomainChangedListener = onDomainChanged;
        dialog.show(parent.getChildFragmentManager(), TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_App_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogCustomDomainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        projectId = args.getString(ARG_PROJECT_ID, "");
        websiteName = args.getString(ARG_NAME, "");
        slug = args.getString(ARG_SLUG, "");
        defaultSiteUrl = args.getString(ARG_SITE_URL, "");

        binding.toolbar.setNavigationOnClickListener(v -> dismiss());
        binding.toolbar.setTitle(websiteName.isEmpty() ? getString(R.string.custom_domain_title) : websiteName);

        binding.textDefaultSubdomain.setText(defaultSiteUrl);

        binding.btnConnectDomain.setOnClickListener(v -> onConnectDomain());
        binding.btnVerifyDomain.setOnClickListener(v -> onVerifyDomain(false));
        binding.btnCancelDomain.setOnClickListener(v -> onConfirmRemoveDomain());
        binding.btnRemoveActiveDomain.setOnClickListener(v -> onConfirmRemoveDomain());

        binding.btnOpenActiveSite.setOnClickListener(v -> {
            if (currentDomain != null) {
                String url = "https://" + currentDomain.hostname;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        binding.btnCopyActiveSite.setOnClickListener(v -> {
            if (currentDomain != null) {
                copyToClipboard("Domain Website", "https://" + currentDomain.hostname);
                Toast.makeText(requireContext(), R.string.custom_domain_copied, Toast.LENGTH_SHORT).show();
            }
        });

        loadDomain();
    }

    @Override
    public void onStart() {
        super.onStart();
        FullScreenDialogs.apply(this, binding.getRoot());
    }

    private void loadDomain() {
        setLoading(true);
        executors.networkIO().execute(() -> {
            Resource<DomainResponseDto> res = repository.getDomain(projectId);
            executors.mainThread().execute(() -> {
                if (!isAdded()) return;
                setLoading(false);
                if (res.getStatus() == Resource.Status.SUCCESS) {
                    currentDomain = res.getData();
                    renderDomain(currentDomain);
                } else if (res.getError() != null) {
                    showError(ErrorMessages.forError(requireContext(), res.getError()));
                }
            });
        });
    }

    private void renderDomain(@Nullable DomainResponseDto domain) {
        // Saat domain kustom aktif, link dari halaman website sudah berupa domain kustom; link bawaan dari server.
        if (domain != null && domain.defaultUrl != null) {
            binding.textDefaultSubdomain.setText(domain.defaultUrl);
        }
        if (domain == null) {
            // Belum pasang domain
            binding.badgeCustomStatus.setText("Belum Ada");
            binding.badgeCustomStatus.setBackgroundResource(R.drawable.bg_badge);
            binding.layoutNoDomain.setVisibility(View.VISIBLE);
            binding.layoutPendingDomain.setVisibility(View.GONE);
            binding.layoutActiveDomain.setVisibility(View.GONE);
            binding.inputDomainText.setText("");
            return;
        }

        if (domain.isActive()) {
            // Aktif
            binding.badgeCustomStatus.setText(R.string.custom_domain_status_active);
            binding.badgeCustomStatus.setBackgroundResource(R.drawable.bg_badge_success);
            binding.layoutNoDomain.setVisibility(View.GONE);
            binding.layoutPendingDomain.setVisibility(View.GONE);
            binding.layoutActiveDomain.setVisibility(View.VISIBLE);
            binding.textActiveDomainUrl.setText("https://" + domain.hostname);
        } else {
            // Pending (Menunggu verifikasi DNS)
            binding.badgeCustomStatus.setText(R.string.custom_domain_status_pending);
            binding.badgeCustomStatus.setBackgroundResource(R.drawable.bg_badge_accent);
            binding.layoutNoDomain.setVisibility(View.GONE);
            binding.layoutPendingDomain.setVisibility(View.VISIBLE);
            binding.layoutActiveDomain.setVisibility(View.GONE);
            binding.textPendingHostname.setText(domain.hostname);

            setupDnsInstructions(domain);

            if (domain.lastError != null && !domain.lastError.isBlank()) {
                binding.textPendingError.setText(domain.lastError);
                binding.textPendingError.setVisibility(View.VISIBLE);
            } else {
                binding.textPendingError.setVisibility(View.GONE);
            }
        }
    }

    private void setupDnsInstructions(DomainResponseDto domain) {
        List<DomainInstructionDto> instructions = domain.instructions;
        if (instructions == null || instructions.isEmpty()) {
            return;
        }

        for (DomainInstructionDto inst : instructions) {
            if ("CNAME".equalsIgnoreCase(inst.type) || "A".equalsIgnoreCase(inst.type)) {
                String typeHost = inst.type + " (Host: " + inst.host + ")";
                binding.textTargetTypeLabel.setText(typeHost);
                binding.textTargetValue.setText(inst.value);
                binding.btnCopyTarget.setOnClickListener(v -> {
                    copyToClipboard("DNS Target", inst.value);
                    Toast.makeText(requireContext(), R.string.custom_domain_copied, Toast.LENGTH_SHORT).show();
                });
            } else if ("TXT".equalsIgnoreCase(inst.type)) {
                binding.textTxtHostLabel.setText("Record TXT (Verifikasi)");
                binding.textTxtHostName.setText("Host: " + inst.host);
                binding.textTxtValue.setText(inst.value);
                binding.btnCopyTxt.setOnClickListener(v -> {
                    copyToClipboard("Token Verifikasi TXT", inst.value);
                    Toast.makeText(requireContext(), R.string.custom_domain_copied, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void onConnectDomain() {
        String raw = binding.inputDomainText.getText() != null ? binding.inputDomainText.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            binding.inputDomainLayout.setError("Ketikkan nama domain terlebih dahulu");
            return;
        }
        binding.inputDomainLayout.setError(null);

        setLoading(true);
        executors.networkIO().execute(() -> {
            Resource<DomainResponseDto> res = repository.addDomain(projectId, raw);
            executors.mainThread().execute(() -> {
                if (!isAdded()) return;
                setLoading(false);
                if (res.getStatus() == Resource.Status.SUCCESS) {
                    currentDomain = res.getData();
                    renderDomain(currentDomain);
                    Snackbar.make(binding.getRoot(), "Domain berhasil didaftarkan. Silakan atur DNS.", Snackbar.LENGTH_LONG).show();
                    notifyChanged();
                } else if (res.getError() != null) {
                    showError(ErrorMessages.forError(requireContext(), res.getError()));
                }
            });
        });
    }

    private void onVerifyDomain(boolean bypassDns) {
        if (currentDomain == null) return;
        setLoading(true);

        executors.networkIO().execute(() -> {
            Resource<DomainResponseDto> res = repository.verifyDomain(projectId, currentDomain.id, bypassDns);
            executors.mainThread().execute(() -> {
                if (!isAdded()) return;
                setLoading(false);
                if (res.getStatus() == Resource.Status.SUCCESS) {
                    currentDomain = res.getData();
                    renderDomain(currentDomain);
                    Toast.makeText(requireContext(), R.string.custom_domain_success_activated, Toast.LENGTH_LONG).show();
                    notifyChanged();
                } else if (res.getError() != null && (bypassDns || !BuildConfig.DEBUG)) {
                    showError(ErrorMessages.forError(requireContext(), res.getError()));
                } else if (res.getError() != null) {
                    String errorMsg = ErrorMessages.forError(requireContext(), res.getError());
                    // Hanya build debug: tawarkan aktivasi tanpa DNS (server juga hanya mengizinkannya di profile dev).
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Verifikasi DNS")
                            .setMessage(errorMsg + "\n\nIngin mengaktifkan langsung untuk pengujian (Testing/Dev)?")
                            .setPositiveButton("Aktifkan Langsung", (dialog, which) -> onVerifyDomain(true))
                            .setNegativeButton("Tutup", null)
                            .show();
                }
            });
        });
    }

    private void onConfirmRemoveDomain() {
        if (currentDomain == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.custom_domain_confirm_remove_title)
                .setMessage(R.string.custom_domain_confirm_remove_msg)
                .setPositiveButton("Lepas Domain", (dialog, which) -> doRemoveDomain())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void doRemoveDomain() {
        if (currentDomain == null) return;
        setLoading(true);

        executors.networkIO().execute(() -> {
            Resource<Boolean> res = repository.removeDomain(projectId, currentDomain.id);
            executors.mainThread().execute(() -> {
                if (!isAdded()) return;
                setLoading(false);
                if (res.getStatus() == Resource.Status.SUCCESS) {
                    currentDomain = null;
                    renderDomain(null);
                    Toast.makeText(requireContext(), "Domain berhasil dilepas", Toast.LENGTH_SHORT).show();
                    notifyChanged();
                } else if (res.getError() != null) {
                    showError(ErrorMessages.forError(requireContext(), res.getError()));
                }
            });
        });
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void setLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.scrollContent.setAlpha(loading ? 0.5f : 1.0f);
        binding.btnConnectDomain.setEnabled(!loading);
        binding.btnVerifyDomain.setEnabled(!loading);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void notifyChanged() {
        if (onDomainChangedListener != null) {
            onDomainChangedListener.run();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
