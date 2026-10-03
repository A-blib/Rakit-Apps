package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.ProviderStatus;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.FragmentProviderDashboardBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.LottieTint;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.aris.templateapp.ui.profile.ProfileSheet;

import dagger.hilt.android.AndroidEntryPoint;

/** Dashboard Provider: "Segera hadir" + banner status verifikasi (bagian 6.6). */
@AndroidEntryPoint
public class ProviderDashboardFragment extends Fragment {

    private FragmentProviderDashboardBinding binding;
    private CurrentUserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);
        LottieTint.applyForeground(binding.illustration);
        binding.account.avatarContainer.setOnClickListener(v ->
                new ProfileSheet().show(getChildFragmentManager(), ProfileSheet.TAG));
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            AppBarAccount.bind(binding.account, user);
            bindStatus(user);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.reload();
    }

    /** pending → kuning, rejected → merah + alasan, suspended → merah, approved → tanpa banner. */
    private void bindStatus(@Nullable User user) {
        ProviderStatus status = user == null ? null : user.getProviderStatus();
        if (status == null || status == ProviderStatus.APPROVED) {
            binding.statusBanner.setVisibility(View.GONE);
            return;
        }
        binding.statusBanner.setVisibility(View.VISIBLE);
        switch (status) {
            case PENDING:
                binding.statusBanner.bind(StatusBannerView.Kind.WARNING, getString(R.string.provider_status_pending));
                break;
            case REJECTED:
                String reason = user.getProviderRejectionReason();
                binding.statusBanner.bind(StatusBannerView.Kind.ERROR, reason == null || reason.isEmpty()
                        ? getString(R.string.provider_status_rejected_no_reason)
                        : getString(R.string.provider_status_rejected, reason));
                break;
            case SUSPENDED:
            default:
                binding.statusBanner.bind(StatusBannerView.Kind.ERROR, getString(R.string.provider_status_suspended),
                        R.drawable.ic_block);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
