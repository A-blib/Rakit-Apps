package com.aris.templateapp.ui.workspace;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.InvitationPreview;
import com.aris.templateapp.databinding.FragmentJoinOrganizationBinding;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Gabung organisasi lewat kode undangan (revisi-fondasi-orgspace.md bagian 5): user melihat pratinjau nama, logo,
 * dan perannya dulu sebelum benar-benar bergabung, supaya tidak salah masuk organisasi.
 */
@AndroidEntryPoint
public class JoinOrganizationFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentJoinOrganizationBinding binding;
    private WorkspaceSetupViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentJoinOrganizationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(WorkspaceSetupViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.checkButton.setOnClickListener(v -> check());
        binding.codeInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                check();
                return true;
            }
            return false;
        });
        // Kode ditampilkan kapital walau ditempel atau diketik keyboard yang mengabaikan textCapCharacters.
        binding.codeInput.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        binding.codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.codeLayout.setError(null);
                viewModel.clearPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.joinButton.setOnClickListener(v -> viewModel.join(OnboardingUi.text(binding.codeInput),
                OnboardingUi.text(binding.positionInput)));

        viewModel.getPreview().observe(getViewLifecycleOwner(), this::showPreview);
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.checkButton.setEnabled(!loading);
            binding.joinButton.setEnabled(!loading);
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error == null) {
                return;
            }
            // Kode salah/kedaluwarsa ditempel di kolom kode agar jelas yang perlu diperbaiki.
            if ("INVITATION_INVALID".equals(error.getCode())) {
                binding.codeLayout.setError(getString(R.string.error_invitation_invalid));
                return;
            }
            OnboardingUi.showFailure(binding.getRoot(), error, Map.of("positionTitle", binding.positionLayout), () -> {
                if (!viewModel.retryPendingFinish()) {
                    if (viewModel.getPreview().getValue() == null) {
                        check();
                    } else {
                        binding.joinButton.performClick();
                    }
                }
            });
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), event -> {
            var user = event.getContentIfNotHandled();
            if (user != null) {
                HomeNavigator.navigateHome(this, user);
            }
        });
    }

    private void check() {
        String code = OnboardingUi.text(binding.codeInput).trim();
        if (code.isEmpty()) {
            binding.codeLayout.setError(getString(R.string.join_error_code_required));
            return;
        }
        viewModel.checkCode(code);
    }

    private void showPreview(@Nullable InvitationPreview preview) {
        binding.previewSection.setVisibility(preview == null ? View.GONE : View.VISIBLE);
        binding.checkButton.setVisibility(preview == null ? View.VISIBLE : View.GONE);
        if (preview == null) {
            return;
        }
        binding.previewName.setText(preview.organizationName);
        binding.previewRole.setText(getString(R.string.join_preview_role, getString(WorkspaceUi.roleLabel(preview.role))));
        binding.previewLogo.setImageDrawable(null);
        binding.previewLogoPlaceholder.setVisibility(View.VISIBLE);
        thumbnailLoader.load(preview.logoUrl, binding.previewLogo, () -> {
            if (binding != null) {
                binding.previewLogoPlaceholder.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
