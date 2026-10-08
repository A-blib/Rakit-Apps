package com.aris.templateapp.ui.template;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.core.upload.FileSizes;
import com.aris.templateapp.data.local.TemplatePackageEntity;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.GalleryTemplateDetailDto;
import com.aris.templateapp.data.repository.TemplatePackageRepository;
import com.aris.templateapp.databinding.FragmentTemplateDownloadBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.editor.EditorNav;
import com.aris.templateapp.ui.provider.TemplateUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar Unduh paket template (alur-buat-website-via-template.md bagian 5), sekaligus detail singkat template
 * (keputusan D3). Unduhan mulai otomatis; Batal atau tombol kembali menghentikannya.
 */
@AndroidEntryPoint
public class TemplateDownloadFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentTemplateDownloadBinding binding;
    private TemplateDownloadViewModel viewModel;
    private boolean mobileDialogShown;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTemplateDownloadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String name = args.getString(TemplateNav.ARG_NAME);
        binding.name.setText(name);
        binding.toolbar.setNavigationOnClickListener(v -> cancelAndClose());
        binding.cancel.setOnClickListener(v -> cancelAndClose());
        binding.retry.setOnClickListener(v -> viewModel.startDownload());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        cancelAndClose();
                    }
                });

        viewModel = new ViewModelProvider(this).get(TemplateDownloadViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getOpenEditor().observe(getViewLifecycleOwner(), event -> {
            TemplatePackageEntity entity = event.getContentIfNotHandled();
            if (entity != null) {
                EditorNav.openNewFromPackage(this, entity.templateId, entity.version, entity.name);
            }
        });
        viewModel.start(args.getString(TemplateNav.ARG_TEMPLATE_ID, ""), isMetered());
    }

    private void render(@Nullable DownloadState state) {
        if (state == null) {
            return;
        }
        switch (state.phase) {
            case LOADING:
                binding.content.setVisibility(View.GONE);
                binding.state.showLoading();
                return;
            case DETAIL_ERROR:
                binding.content.setVisibility(View.GONE);
                binding.state.showError(detailErrorText(state.error), () -> viewModel.retryDetail(isMetered()));
                return;
            default:
                break;
        }
        binding.state.hide();
        binding.content.setVisibility(View.VISIBLE);
        if (state.detail != null) {
            bindDetail(state.detail);
        }
        boolean failed = state.phase == DownloadState.Phase.FAILED;
        boolean missing = failed && state.error != null
                && TemplatePackageRepository.PACKAGE_MISSING.equals(state.error.getCode());
        binding.retry.setVisibility(failed && !missing ? View.VISIBLE : View.GONE);
        binding.progress.setVisibility(failed ? View.GONE : View.VISIBLE);
        binding.progressBody.setText(failed ? downloadErrorText(state.error) : getString(R.string.template_download_once));
        binding.progressBody.setTextColor(requireContext().getColor(failed ? R.color.color_error : R.color.color_muted));

        switch (state.phase) {
            case ASK_MOBILE:
                binding.progressText.setText(null);
                askMobileData(state.detail);
                break;
            case DOWNLOADING:
                showProgress(state.downloaded, state.total);
                break;
            case EXTRACTING:
                binding.progress.setIndeterminate(true);
                binding.progressText.setText(R.string.template_download_extracting);
                break;
            case FAILED:
            default:
                binding.progressText.setText(null);
                break;
        }
    }

    private void bindDetail(GalleryTemplateDetailDto detail) {
        binding.name.setText(detail.name);
        binding.creator.setText(getString(R.string.template_download_creator, detail.creatorName));
        binding.category.setText(getString(TemplateUi.categoryLabel(detail.category)).toUpperCase(Locale.ROOT));
        int pageCount = detail.pages == null ? 0 : detail.pages.size();
        binding.pages.setVisibility(pageCount > 0 ? View.VISIBLE : View.GONE);
        binding.pages.setText(getString(R.string.template_download_pages, pageCount).toUpperCase(Locale.ROOT));
        binding.size.setVisibility(detail.packageSizeBytes == null ? View.GONE : View.VISIBLE);
        binding.size.setText(detail.packageSizeBytes == null ? null : FileSizes.format(detail.packageSizeBytes));
        binding.description.setVisibility(detail.description == null ? View.GONE : View.VISIBLE);
        binding.description.setText(detail.description);
        if (binding.thumbnail.getTag() == null) {
            thumbnailLoader.load(detail.thumbnailUrl, binding.thumbnail, null);
        }
    }

    private void showProgress(long downloaded, long total) {
        if (total <= 0) {
            binding.progress.setIndeterminate(true);
            binding.progressText.setText(FileSizes.format(downloaded));
            return;
        }
        if (binding.progress.isIndeterminate()) {
            // Indikator harus disembunyikan dulu sebelum berganti mode (aturan LinearProgressIndicator).
            binding.progress.setVisibility(View.INVISIBLE);
            binding.progress.setIndeterminate(false);
            binding.progress.setVisibility(View.VISIBLE);
        }
        binding.progress.setMax(1000);
        binding.progress.setProgressCompat((int) (downloaded * 1000 / total), true);
        binding.progressText.setText(getString(R.string.template_download_progress,
                FileSizes.megabytes(downloaded), FileSizes.megabytes(total)).toUpperCase(Locale.ROOT));
    }

    private void askMobileData(@Nullable GalleryTemplateDetailDto detail) {
        if (mobileDialogShown || detail == null || detail.packageSizeBytes == null) {
            return;
        }
        mobileDialogShown = true;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.template_download_mobile_title)
                .setMessage(getString(R.string.template_download_mobile_body, FileSizes.format(detail.packageSizeBytes)))
                .setPositiveButton(R.string.template_download_mobile_continue, (d, w) -> viewModel.startDownload())
                .setNegativeButton(R.string.action_cancel, (d, w) -> close())
                .setOnCancelListener(d -> close())
                .show();
    }

    private String detailErrorText(@Nullable ApiError error) {
        if (error != null && ApiError.NETWORK_ERROR.equals(error.getCode())) {
            return getString(R.string.template_download_offline);
        }
        if (error != null && "NOT_FOUND".equals(error.getCode())) {
            return getString(R.string.template_download_not_found);
        }
        return ErrorMessages.forError(requireContext(), error);
    }

    private String downloadErrorText(@Nullable ApiError error) {
        String code = error == null ? "" : error.getCode();
        switch (code) {
            case TemplatePackageRepository.PACKAGE_MISSING:
                return getString(R.string.template_download_missing);
            case TemplatePackageRepository.PACKAGE_INVALID:
                return getString(R.string.template_download_invalid);
            case ApiError.NETWORK_ERROR:
                return getString(R.string.template_download_failed);
            default:
                return ErrorMessages.forError(requireContext(), error);
        }
    }

    private boolean isMetered() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.isActiveNetworkMetered();
    }

    private void cancelAndClose() {
        viewModel.cancel();
        close();
    }

    private void close() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
