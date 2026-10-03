package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.databinding.FragmentCreatorDashboardBinding;

import dagger.hilt.android.AndroidEntryPoint;

/** Dashboard Pembuat Website: layar "Segera hadir" (bagian 2.3). */
@AndroidEntryPoint
public class CreatorDashboardFragment extends Fragment {

    private FragmentCreatorDashboardBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // View Fragment dihancurkan lebih dulu daripada Fragment-nya; lepaskan binding agar tidak bocor memori.
        binding = null;
    }
}
