package com.aris.templateapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentRegisterBinding;
import com.aris.templateapp.ui.common.HomeNavigator;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Layar Daftar (bagian 9.4): nama, email, password, lalu alternatif Google/GitHub. */
@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    @Inject
    AuthDeepLinks deepLinks;

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.registerButton.setOnClickListener(v -> submit());
        binding.passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        // Daftar dibuka dari layar Masuk, jadi "Sudah punya akun? Masuk" cukup kembali ke sana.
        binding.loginLink.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        SocialAuthBinder.bind(this, binding.social, binding.linkBanner, viewModel, deepLinks, link -> {
            // Penyambungan lewat email + password dilakukan di layar Masuk; linkToken dibawa sebagai argumen.
            Bundle args = new Bundle();
            args.putString(LoginFragment.ARG_LINK_TOKEN, link.getLinkToken());
            args.putString(LoginFragment.ARG_LINK_NEW_METHOD, link.getNewMethod().value());
            viewModel.cancelLink();
            NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login, args);
        });

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors -> {
            AuthFormBinder.showFieldError(binding.nameLayout, errors.get(AuthFormValidator.Field.NAME));
            AuthFormBinder.showFieldError(binding.emailLayout, errors.get(AuthFormValidator.Field.EMAIL));
            AuthFormBinder.showFieldError(binding.passwordLayout, errors.get(AuthFormValidator.Field.PASSWORD));
        });
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::setLoading);
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var failure = event.getContentIfNotHandled();
            if (failure != null) {
                AuthFormBinder.showFailure(binding.getRoot(), failure,
                        Map.of("displayName", binding.nameLayout, "email", binding.emailLayout,
                                "password", binding.passwordLayout), viewModel::retry);
            }
        });
        viewModel.getSuccess().observe(getViewLifecycleOwner(), event -> {
            var result = event.getContentIfNotHandled();
            if (result != null) {
                HomeNavigator.navigateHome(this, result.getUser());
            }
        });
    }

    private void submit() {
        viewModel.register(text(binding.nameInput), text(binding.emailInput), text(binding.passwordInput));
    }

    private void setLoading(boolean loading) {
        binding.registerButton.setEnabled(!loading);
        binding.registerButton.setText(loading ? R.string.state_loading : R.string.action_register);
        binding.nameLayout.setEnabled(!loading);
        binding.emailLayout.setEnabled(!loading);
        binding.passwordLayout.setEnabled(!loading);
    }

    private static String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
