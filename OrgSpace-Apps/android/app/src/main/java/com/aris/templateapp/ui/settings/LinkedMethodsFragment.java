package com.aris.templateapp.ui.settings;

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
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.Identity;
import com.aris.templateapp.data.model.LoginMethod;
import com.aris.templateapp.databinding.FragmentLinkedMethodsBinding;
import com.aris.templateapp.databinding.ItemLinkedMethodBinding;
import com.aris.templateapp.ui.auth.AuthDeepLinks;
import com.aris.templateapp.ui.auth.GitHubSignInHelper;
import com.aris.templateapp.ui.auth.GoogleSignInHelper;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Metode login terhubung (bagian 6.4 & 13.2 skenario 8): satu baris per metode (Google, GitHub, email).
 * Metode tersambung bisa dilepas (kecuali yang terakhir); Google/GitHub yang belum tersambung bisa disambungkan.
 */
@AndroidEntryPoint
public class LinkedMethodsFragment extends Fragment {

    private static final LoginMethod[] METHODS = {LoginMethod.GOOGLE, LoginMethod.GITHUB, LoginMethod.LOCAL};

    @Inject
    AuthDeepLinks deepLinks;

    private FragmentLinkedMethodsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLinkedMethodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getIdentities().observe(getViewLifecycleOwner(), this::render);
        viewModel.isBusy().observe(getViewLifecycleOwner(), busy -> binding.list.setAlpha(busy ? 0.5f : 1f));
        viewModel.getOpenUrl().observe(getViewLifecycleOwner(), event -> {
            String url = event.getContentIfNotHandled();
            if (url != null) {
                GitHubSignInHelper.open(requireContext(), url);
            }
        });
        deepLinks.getCallbacks().observe(getViewLifecycleOwner(), event -> {
            var uri = event.getContentIfNotHandled();
            if (uri != null) {
                viewModel.onGitHubCallback(uri);
            }
        });
        viewModel.getLinked().observe(getViewLifecycleOwner(), event -> {
            LoginMethod method = event.getContentIfNotHandled();
            if (method != null) {
                snack(getString(R.string.linked_success, ErrorMessages.methodLabel(requireContext(), method)));
            }
        });
        viewModel.getUnlinked().observe(getViewLifecycleOwner(), event -> {
            LoginMethod method = event.getContentIfNotHandled();
            if (method != null) {
                snack(getString(R.string.unlinked_success, ErrorMessages.methodLabel(requireContext(), method)));
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                snack("LAST_IDENTITY".equals(error.getCode())
                        ? getString(R.string.error_last_identity)
                        : ErrorMessages.forError(requireContext(), error));
            }
        });
    }

    private void render(Resource<List<Identity>> resource) {
        if (resource.getStatus() == Resource.Status.LOADING) {
            binding.list.setVisibility(View.GONE);
            binding.stateView.showLoading();
            return;
        }
        if (resource.getStatus() == Resource.Status.ERROR) {
            binding.list.setVisibility(View.GONE);
            binding.stateView.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
            return;
        }
        binding.stateView.hide();
        binding.list.setVisibility(View.VISIBLE);
        binding.list.removeAllViews();
        List<Identity> identities = resource.getData();
        for (LoginMethod method : METHODS) {
            Identity identity = find(identities, method);
            ItemLinkedMethodBinding row = ItemLinkedMethodBinding.inflate(getLayoutInflater(), binding.list, true);
            bindRow(row, method, identity, identities.size());
        }
    }

    private void bindRow(ItemLinkedMethodBinding row, LoginMethod method, @Nullable Identity identity, int linkedCount) {
        row.icon.setImageResource(method == LoginMethod.GOOGLE ? R.drawable.ic_logo_google
                : method == LoginMethod.GITHUB ? R.drawable.ic_logo_github : R.drawable.ic_mail);
        // Logo Google berwarna asli (tanpa tint); ikon lain mengikuti warna teks.
        row.icon.setImageTintList(method == LoginMethod.GOOGLE ? null
                : requireContext().getColorStateList(R.color.color_foreground));
        String label = ErrorMessages.methodLabel(requireContext(), method);
        row.title.setText(method == LoginMethod.LOCAL ? getString(R.string.field_email) : label);

        if (identity != null) {
            row.subtitle.setText(identity.getEmail());
            row.action.setText(R.string.action_unlink);
            // Metode terakhir tidak boleh dilepas; tombol tetap tampil tetapi nonaktif agar alasannya jelas.
            row.action.setEnabled(linkedCount > 1);
            row.action.setOnClickListener(v -> confirmUnlink(method, label));
        } else if (method == LoginMethod.LOCAL) {
            // Belum ada endpoint untuk membuat password pada akun Google/GitHub.
            row.subtitle.setText(R.string.linked_email_not_available);
            row.action.setVisibility(View.GONE);
        } else {
            row.subtitle.setText(R.string.linked_status_not_connected);
            row.action.setText(R.string.action_link);
            row.action.setOnClickListener(v -> link(method));
        }
    }

    private void link(LoginMethod method) {
        if (method == LoginMethod.GITHUB) {
            viewModel.startLinkGitHub();
            return;
        }
        GoogleSignInHelper.signIn(requireActivity(), new GoogleSignInHelper.Callback() {
            @Override
            public void onIdToken(String idToken) {
                viewModel.linkGoogle(idToken);
            }

            @Override
            public void onCancelled() {
                // User menutup pilihan akun.
            }

            @Override
            public void onError(int messageRes) {
                snack(getString(messageRes));
            }
        });
    }

    private void confirmUnlink(LoginMethod method, String label) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.unlink_confirm_title, label))
                .setMessage(getString(R.string.unlink_confirm_body, label))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_unlink, (d, w) -> viewModel.unlink(method))
                .show();
    }

    private static Identity find(List<Identity> identities, LoginMethod method) {
        for (Identity identity : identities) {
            if (identity.getMethod() == method) {
                return identity;
            }
        }
        return null;
    }

    private void snack(String message) {
        if (binding != null) {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
