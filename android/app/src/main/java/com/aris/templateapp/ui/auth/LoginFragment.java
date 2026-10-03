package com.aris.templateapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentLoginBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/** Layar Masuk (bagian 6.2 & 9.4): Google, GitHub, lalu email + password. */
@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.loginButton.setOnClickListener(v -> submit());
        binding.passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        binding.forgotPasswordButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.forgot_password_dialog_title)
                .setMessage(R.string.forgot_password_dialog_body)
                .setPositiveButton(R.string.action_ok, null)
                .show());
        binding.registerLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors -> {
            AuthFormBinder.showFieldError(binding.emailLayout, errors.get(AuthFormValidator.Field.EMAIL));
            AuthFormBinder.showFieldError(binding.passwordLayout, errors.get(AuthFormValidator.Field.PASSWORD));
        });
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::setLoading);
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var failure = event.getContentIfNotHandled();
            if (failure != null) {
                AuthFormBinder.showFailure(binding.getRoot(), failure,
                        Map.of("email", binding.emailLayout, "password", binding.passwordLayout), viewModel::retry);
            }
        });
        viewModel.getSuccess().observe(getViewLifecycleOwner(), event -> {
            var result = event.getContentIfNotHandled();
            if (result != null) {
                PostLoginNavigator.navigate(this, result.getUser());
            }
        });
    }

    private void submit() {
        viewModel.login(text(binding.emailInput), text(binding.passwordInput));
    }

    private void setLoading(boolean loading) {
        binding.loginButton.setEnabled(!loading);
        binding.loginButton.setText(loading ? R.string.state_loading : R.string.action_sign_in);
        binding.emailLayout.setEnabled(!loading);
        binding.passwordLayout.setEnabled(!loading);
    }

    private static String text(android.widget.EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
