package com.aris.templateapp.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentRoleSelectBinding;

import dagger.hilt.android.AndroidEntryPoint;

/** Pilih peran untuk akun baru (bagian 6.6): pembuat website atau penyedia template. */
@AndroidEntryPoint
public class RoleSelectFragment extends Fragment {

    private FragmentRoleSelectBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRoleSelectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.creatorCardContent.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_role_select_to_creator_form));
        binding.providerCardContent.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_role_select_to_provider_form));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
