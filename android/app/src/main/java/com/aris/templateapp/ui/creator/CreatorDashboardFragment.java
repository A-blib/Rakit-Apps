package com.aris.templateapp.ui.creator;

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
import com.aris.templateapp.databinding.FragmentCreatorDashboardBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.LottieTint;
import com.aris.templateapp.ui.common.StatusBannerView;

import dagger.hilt.android.AndroidEntryPoint;

/** Dashboard Pembuat Website: layar "Segera hadir" (bagian 2.3), untuk tamu maupun user login. */
@AndroidEntryPoint
public class CreatorDashboardFragment extends Fragment {

    /** Argumen navigasi (lihat nav_graph.xml): true jika user dialihkan ke sini karena mode provider ditangguhkan. */
    public static final String ARG_PROVIDER_SUSPENDED = "providerSuspended";

    private FragmentCreatorDashboardBinding binding;
    private CurrentUserViewModel viewModel;

    public static Bundle args(boolean providerSuspended) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_PROVIDER_SUSPENDED, providerSuspended);
        return args;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);
        LottieTint.applyForeground(binding.illustration);

        boolean suspended = getArguments() != null && getArguments().getBoolean(ARG_PROVIDER_SUSPENDED, false);
        if (suspended) {
            binding.suspendedBanner.setVisibility(View.VISIBLE);
            binding.suspendedBanner.bind(StatusBannerView.Kind.ERROR, getString(R.string.provider_suspended_redirect),
                    R.drawable.ic_block);
        }

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> AppBarAccount.bind(binding.account, user));
        binding.account.signInButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_creator_dashboard_to_login));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Dibaca ulang setiap layar tampil lagi, mis. setelah kembali dari layar Masuk.
        viewModel.reload();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // View Fragment dihancurkan lebih dulu daripada Fragment-nya; lepaskan binding agar tidak bocor memori.
        binding = null;
    }
}
