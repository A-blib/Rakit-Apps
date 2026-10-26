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
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.repository.WorkspaceRepository;
import com.aris.templateapp.databinding.FragmentGuideBinding;
import com.aris.templateapp.ui.organization.MembersFragment;
import com.aris.templateapp.ui.organization.OrganizationArgs;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Halaman satu panduan (provider atau pembuat website). Membukanya menghapus titik "baru" pada kartu panduannya. */
@AndroidEntryPoint
public class GuideFragment extends Fragment {

    /** Argumen navigasi: nama enum {@link Guide}, mis. "HOST_ZIP". */
    public static final String ARG_GUIDE = "guide";

    @Inject
    GuideStore guideStore;
    @Inject
    WorkspaceRepository workspaceRepository;

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
        if (guide == Guide.INVITE) {
            binding.actionButton.setVisibility(View.VISIBLE);
            binding.actionButton.setText(R.string.guide_invite_action);
            binding.actionButton.setOnClickListener(v -> openInvite());
        }
    }

    /** Membuka layar Anggota ruang kerja aktif dengan lembar undangan langsung terbuka. */
    private void openInvite() {
        Workspace active = workspaceRepository.getActive().getValue();
        if (active == null || active.isPersonal()) {
            Snackbar.make(binding.getRoot(), R.string.guide_invite_need_org, Snackbar.LENGTH_LONG).show();
            return;
        }
        Bundle args = OrganizationArgs.of(active.getId());
        args.putBoolean(MembersFragment.ARG_OPEN_INVITE, true);
        NavHostFragment.findNavController(this).navigate(R.id.membersFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
