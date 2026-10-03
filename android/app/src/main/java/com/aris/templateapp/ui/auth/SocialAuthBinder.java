package com.aris.templateapp.ui.auth;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.LoginMethod;
import com.aris.templateapp.databinding.ViewSocialButtonsBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.google.android.material.snackbar.Snackbar;

import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Bagian yang sama di layar Masuk & Daftar untuk tombol Google/GitHub dan penyambungan akun:
 * <pre>
 * Google  → Credential Manager → idToken → AuthViewModel.signInWithGoogle
 * GitHub  → AuthViewModel.startGitHub → URL → Custom Tab → deep link → AuthViewModel.onGitHubCallback
 * 409 ACCOUNT_LINK_REQUIRED → LinkAccountDialog → user pilih metode lama → jalankan metode itu + linkToken
 * </pre>
 */
final class SocialAuthBinder {

    private SocialAuthBinder() {
    }

    /**
     * @param onLinkWithEmail dipanggil jika user harus masuk dengan email + password untuk menyambungkan akun
     */
    static void bind(Fragment fragment, ViewSocialButtonsBinding social, StatusBannerView linkBanner,
                     AuthViewModel viewModel, AuthDeepLinks deepLinks, Consumer<LinkRequest> onLinkWithEmail) {
        LifecycleOwner owner = fragment.getViewLifecycleOwner();
        View root = fragment.requireView();

        social.googleButton.setOnClickListener(v -> startGoogle(fragment, viewModel));
        social.githubButton.setOnClickListener(v -> viewModel.startGitHub());

        viewModel.isLoading().observe(owner, loading -> {
            social.googleButton.setEnabled(!loading);
            social.githubButton.setEnabled(!loading);
        });
        viewModel.getOpenUrl().observe(owner, event -> {
            String url = event.getContentIfNotHandled();
            if (url != null) {
                GitHubSignInHelper.open(fragment.requireContext(), url);
            }
        });
        deepLinks.getCallbacks().observe(owner, event -> {
            var uri = event.getContentIfNotHandled();
            if (uri != null) {
                viewModel.onGitHubCallback(uri);
            }
        });
        viewModel.getLinkRequired().observe(owner, event -> {
            LinkRequest request = event.getContentIfNotHandled();
            if (request != null) {
                LinkAccountDialog dialog = LinkAccountDialog.newInstance();
                dialog.setRequest(request);
                dialog.show(fragment.getChildFragmentManager(), LinkAccountDialog.TAG);
            }
        });
        viewModel.getContinueWith().observe(owner, event -> {
            LoginMethod method = event.getContentIfNotHandled();
            if (method == LoginMethod.GOOGLE) {
                startGoogle(fragment, viewModel);
            } else if (method == LoginMethod.GITHUB) {
                viewModel.startGitHub();
            } else if (method == LoginMethod.LOCAL) {
                onLinkWithEmail.accept(viewModel.getPendingLink().getValue());
            }
        });
        viewModel.getPendingLink().observe(owner, link -> {
            if (link == null) {
                linkBanner.setVisibility(View.GONE);
                return;
            }
            String existing = ErrorMessages.methodLabels(root.getContext(),
                    link.getExistingMethods().stream().map(LoginMethod::value).collect(Collectors.toList()));
            linkBanner.bind(StatusBannerView.Kind.INFO, root.getContext().getString(R.string.link_banner,
                    existing, ErrorMessages.methodLabel(root.getContext(), link.getNewMethod())), R.drawable.ic_link);
            linkBanner.setVisibility(View.VISIBLE);
        });
    }

    private static void startGoogle(Fragment fragment, AuthViewModel viewModel) {
        GoogleSignInHelper.signIn(fragment.requireActivity(), new GoogleSignInHelper.Callback() {
            @Override
            public void onIdToken(String idToken) {
                viewModel.signInWithGoogle(idToken);
            }

            @Override
            public void onCancelled() {
                // User sengaja menutup pilihan akun; tidak perlu pesan.
            }

            @Override
            public void onError(int messageRes) {
                if (fragment.getView() != null) {
                    Snackbar.make(fragment.requireView(), messageRes, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
