package com.aris.templateapp.ui.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aris.templateapp.R;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.databinding.FragmentIntroBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Intro singkat saat app pertama kali dibuka. Setelah selesai (atau dilewati) penanda "intro sudah dilihat"
 * disimpan, sehingga saat app dibuka lagi intro tidak muncul (skenario 1 bagian 13.2).
 */
@AndroidEntryPoint
public class IntroFragment extends Fragment {

    @Inject
    SessionStore sessionStore;

    @Inject
    AppExecutors executors;

    private FragmentIntroBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIntroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.pager.setAdapter(new IntroAdapter());
        createDots();
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateForPage(position);
            }
        });
        updateForPage(binding.pager.getCurrentItem());

        binding.skipButton.setOnClickListener(v -> finishIntro());
        binding.nextButton.setOnClickListener(v -> {
            int next = binding.pager.getCurrentItem() + 1;
            if (next < IntroAdapter.PAGES.length) {
                binding.pager.setCurrentItem(next);
            } else {
                finishIntro();
            }
        });
    }

    private void createDots() {
        int size = getResources().getDimensionPixelSize(R.dimen.page_dot_size);
        int gap = getResources().getDimensionPixelSize(R.dimen.space_2);
        for (int i = 0; i < IntroAdapter.PAGES.length; i++) {
            View dot = new View(requireContext());
            dot.setBackgroundResource(R.drawable.bg_page_dot);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(gap);
            binding.pageIndicator.addView(dot, params);
        }
    }

    private void updateForPage(int position) {
        int count = IntroAdapter.PAGES.length;
        int size = getResources().getDimensionPixelSize(R.dimen.page_dot_size);
        int activeWidth = getResources().getDimensionPixelSize(R.dimen.page_dot_active_width);
        for (int i = 0; i < count; i++) {
            View dot = binding.pageIndicator.getChildAt(i);
            dot.setSelected(i == position);
            dot.getLayoutParams().width = i == position ? activeWidth : size;
            dot.requestLayout();
        }
        binding.pageIndicator.setContentDescription(getString(R.string.intro_page_indicator, position + 1, count));
        boolean last = position == count - 1;
        binding.nextButton.setText(last ? R.string.intro_start : R.string.action_continue);
        binding.skipButton.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
    }

    private void finishIntro() {
        // Disimpan di thread latar karena menulis ke penyimpanan.
        executors.diskIO().execute(sessionStore::setIntroSeen);
        NavHostFragment.findNavController(this).navigate(R.id.action_intro_to_creator_dashboard);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
