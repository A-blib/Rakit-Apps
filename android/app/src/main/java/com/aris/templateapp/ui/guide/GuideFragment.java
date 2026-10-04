package com.aris.templateapp.ui.guide;

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

/** Halaman satu panduan (provider atau pembuat website). Membukanya menghapus titik "baru" pada kartu panduannya. */
@AndroidEntryPoint
public class GuideFragment extends Fragment {

    /** Argumen navigasi: nama enum {@link Guide}, mis. "HOST_ZIP". */
    public static final String ARG_GUIDE = "guide";

    @Inject
    GuideStore guideStore;

    public static void open(Fragment from, Guide guide) {
        Bundle args = new Bundle();
        args.putString(ARG_GUIDE, guide.name());
        NavHostFragment.findNavController(from).navigate(R.id.guideFragment, args);
    }

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
        Guide guide = Guide.valueOf(requireArguments().getString(GuideFragment.ARG_GUIDE, Guide.PREPARE_TEMPLATE.name()));
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
