package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentGuideBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Halaman satu panduan provider. Membukanya menghapus titik "baru" pada kartu panduan di Beranda. */
@AndroidEntryPoint
public class GuideFragment extends Fragment {

    @Inject
    GuideStore guideStore;

    private FragmentGuideBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        Guide guide = Guide.valueOf(requireArguments().getString(ProviderNav.ARG_GUIDE, Guide.PREPARE_TEMPLATE.name()));
        binding.title.setText(guide.title);
        binding.body.setText(guide.available ? guide.body : R.string.guide_coming_soon_body);
        binding.badge.setVisibility(guide.available ? View.GONE : View.VISIBLE);
        guideStore.markOpened(guide);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
