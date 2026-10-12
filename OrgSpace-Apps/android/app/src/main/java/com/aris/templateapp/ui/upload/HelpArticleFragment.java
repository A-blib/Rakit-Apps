package com.aris.templateapp.ui.upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.HelpArticleDto;
import com.aris.templateapp.databinding.FragmentHelpArticleBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Artikel Panduan per aturan (alur-fitur-upload.md bagian 5.11). Aturan yang belum punya artikel (404) membuka
 * Panduan umum "Syarat lolos pengecekan" sebagai gantinya.
 */
@AndroidEntryPoint
public class HelpArticleFragment extends Fragment {

    private FragmentHelpArticleBinding binding;
    private HelpArticleViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHelpArticleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        viewModel = new ViewModelProvider(this).get(HelpArticleViewModel.class);
        viewModel.getArticle().observe(getViewLifecycleOwner(), this::render);
        viewModel.start(requireArguments().getString(UploadNav.ARG_CODE, ""));
    }

    private void render(@Nullable Resource<HelpArticleDto> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.getStatus()) {
            case LOADING:
                binding.content.setVisibility(View.GONE);
                binding.state.showLoading();
                break;
            case ERROR:
                if (resource.getError() != null && "NOT_FOUND".equals(resource.getError().getCode())) {
                    openGeneralGuide();
                    return;
                }
                binding.content.setVisibility(View.GONE);
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
                break;
            case SUCCESS:
            default:
                binding.state.hide(binding.content, () -> bind(resource.getData()));
                break;
        }
    }

    private void openGeneralGuide() {
        Bundle args = new Bundle();
        args.putString(GuideFragment.ARG_GUIDE, Guide.CHECK_RULES.name());
        NavHostFragment.findNavController(this).navigate(R.id.guideFragment, args,
                new NavOptions.Builder().setPopUpTo(R.id.helpArticleFragment, true).build());
    }

    private void bind(HelpArticleDto article) {
        binding.code.setText(article.code);
        binding.title.setText(article.title);
        binding.why.setText(article.why);
        setOptional(binding.wrongTitle, binding.wrong, article.wrongExample);
        setOptional(binding.rightTitle, binding.right, article.rightExample);
        binding.fix.setText(article.howToFix);
        setOptional(binding.tipsTitle, binding.tips, article.tips);
    }

    private static void setOptional(TextView title, TextView body, @Nullable String text) {
        int visibility = text == null || text.isEmpty() ? View.GONE : View.VISIBLE;
        title.setVisibility(visibility);
        body.setVisibility(visibility);
        body.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
