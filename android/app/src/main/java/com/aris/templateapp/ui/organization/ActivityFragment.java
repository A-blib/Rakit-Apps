package com.aris.templateapp.ui.organization;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Activity;
import com.aris.templateapp.databinding.FragmentActivityBinding;
import com.aris.templateapp.databinding.ItemActivityBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.workspace.ActivityText;

import dagger.hilt.android.AndroidEntryPoint;

/** Riwayat aktivitas dasar: "Rina mengubah peran Budi menjadi Admin · 2 jam lalu" (revisi-fondasi-orgspace.md R4). */
@AndroidEntryPoint
public class ActivityFragment extends Fragment {

    private FragmentActivityBinding binding;
    private ActivityViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        viewModel.start(OrganizationArgs.id(this));
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.moreButton.setOnClickListener(v -> {
            binding.moreButton.setEnabled(false);
            viewModel.loadMore();
        });

        viewModel.getPage().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.stateView.showLoading();
                    break;
                case ERROR:
                    binding.content.setVisibility(View.GONE);
                    binding.stateView.showError(ErrorMessages.forError(requireContext(), resource.getError()),
                            viewModel::loadMore);
                    break;
                case SUCCESS:
                default:
                    ActivityViewModel.Page page = resource.getData();
                    if (page.items.isEmpty()) {
                        binding.content.setVisibility(View.GONE);
                        binding.stateView.showEmpty(getString(R.string.activity_empty));
                    } else {
                        binding.stateView.hide(binding.content, () -> bind(page));
                    }
                    break;
            }
        });
    }

    private void bind(ActivityViewModel.Page page) {
        binding.list.removeAllViews();
        long now = System.currentTimeMillis();
        for (Activity activity : page.items) {
            ItemActivityBinding row = ItemActivityBinding.inflate(getLayoutInflater(), binding.list, true);
            ActivityText.Sentence sentence = ActivityText.of(activity, getString(R.string.activity_unknown_actor));
            Object[] args = sentence.args.clone();
            for (int i = 0; i < args.length; i++) {
                // Nama peran dikirim sebagai id string; diterjemahkan di sini.
                if (args[i] instanceof Integer) {
                    args[i] = getString((Integer) args[i]);
                }
            }
            row.text.setText(getString(sentence.text, args));
            row.time.setText(relativeTime(activity, now));
        }
        binding.moreButton.setVisibility(page.hasMore ? View.VISIBLE : View.GONE);
        binding.moreButton.setEnabled(true);
    }

    private CharSequence relativeTime(Activity activity, long now) {
        if (activity.createdAt == null) {
            return "";
        }
        long time = activity.createdAt.toEpochMilli();
        // DateUtils menulis "0 menit yang lalu" untuk kejadian di bawah semenit; kalimat itu janggal dibaca.
        if (now - time < DateUtils.MINUTE_IN_MILLIS) {
            return getString(R.string.activity_just_now);
        }
        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
