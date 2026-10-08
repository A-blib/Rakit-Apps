package com.aris.templateapp.ui.upload;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.core.upload.FileSizes;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.TechInfoDto;
import com.aris.templateapp.databinding.FragmentUploadInfoBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.KeyboardAwareBottomBar;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Langkah 3 Upload: Info template (alur-fitur-upload.md bagian 6). Tombol Lanjut aktif setelah nama (3–60),
 * kategori, deskripsi (20–300), dan minimal 1 kata kunci terisi (bagian 6.4).
 */
@AndroidEntryPoint
public class UploadInfoFragment extends Fragment {

    static final int NAME_MIN = 3;
    static final int DESCRIPTION_MIN = 20;
    static final int KEYWORDS_MAX = 5;
    static final int KEYWORD_LENGTH_MAX = 20;

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentUploadInfoBinding binding;
    private UploadInfoViewModel viewModel;
    private ThumbnailCapture capture;
    @Nullable
    private UploadInfoViewModel.SiteReady site;
    private final List<String> keywords = new ArrayList<>();
    private boolean filled;
    private KeyboardAwareBottomBar keyboardAware;
    private String thumbnailSource;
    // Nama section thumbnail yang dipilih di layar ini, agar ganti HP/Desktop memotret section yang sama.
    @Nullable
    private String thumbnailSectionName;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.customThumbnail(uri, currentView());
                }
            });

    private final OnBackPressedCallback back = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            close();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 3, R.string.upload_step_info, this::close);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
        capture = new ThumbnailCapture(new OffscreenPage(binding.hostFrame));
        keyboardAware = KeyboardAwareBottomBar.attach(binding.getRoot(), binding.actions, binding.actionsDivider);

        viewModel = new ViewModelProvider(this).get(UploadInfoViewModel.class);
        viewModel.getDraft().observe(getViewLifecycleOwner(), this::render);
        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), this::bindSaveStatus);
        viewModel.getNameDuplicate().observe(getViewLifecycleOwner(), duplicate -> binding.nameLayout.setHelperText(
                getString(Boolean.TRUE.equals(duplicate) ? R.string.upload_name_duplicate : R.string.upload_name_helper)));
        viewModel.getThumbnail().observe(getViewLifecycleOwner(), bitmap -> {
            binding.thumbnail.setImageBitmap(bitmap);
            binding.thumbnailPlaceholder.setVisibility(bitmap == null ? View.VISIBLE : View.GONE);
        });
        viewModel.getThumbnailBusy().observe(getViewLifecycleOwner(), busy -> {
            binding.thumbnailStatus.setText(R.string.upload_thumbnail_capturing);
            binding.thumbnailStatus.setVisibility(Boolean.TRUE.equals(busy) ? View.VISIBLE : View.GONE);
            setThumbnailButtonsEnabled(!Boolean.TRUE.equals(busy) && site != null);
        });
        viewModel.getSiteReady().observe(getViewLifecycleOwner(), event -> {
            UploadInfoViewModel.SiteReady ready = event.getContentIfNotHandled();
            if (ready != null) {
                site = ready;
                setThumbnailButtonsEnabled(true);
                DraftDto draft = viewModel.getDraft().getValue() == null ? null : viewModel.getDraft().getValue().getData();
                if (draft != null && draft.thumbnailUrl == null) {
                    captureAuto(); // thumbnail otomatis = bawaan (bagian 6.2)
                }
            }
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), event -> {
            Integer text = event.getContentIfNotHandled();
            if (text != null) {
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getGoNext().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                UploadNav.replaceStep(this, R.id.uploadMarkFragment, viewModel.getTemplateId());
            }
        });

        setThumbnailButtonsEnabled(false);
        binding.thumbnailAuto.setOnClickListener(v -> captureAuto());
        binding.thumbnailSection.setOnClickListener(v -> pickSection());
        binding.thumbnailCustom.setOnClickListener(v -> imagePicker.launch("image/*"));
        binding.viewToggle.addOnButtonCheckedListener((group, id, checked) -> {
            // Ganti HP/Desktop memotret ulang, kecuali thumbnail berupa gambar pilihan sendiri.
            if (checked && filled && site != null && !"custom".equals(thumbnailSource)) {
                if ("section".equals(thumbnailSource) && thumbnailSectionName != null) {
                    recaptureSection(thumbnailSectionName);
                } else {
                    captureAuto();
                }
            }
        });

        binding.nameInput.addTextChangedListener(new AfterChange(text -> {
            if (filled) {
                viewModel.setName(text);
            }
            validate();
        }));
        binding.descriptionInput.addTextChangedListener(new AfterChange(text -> {
            if (filled) {
                viewModel.setDescription(text);
            }
            validate();
        }));
        binding.purposeGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (filled) {
                viewModel.setCategory(OnboardingUi.purposeValue(ids.isEmpty() ? View.NO_ID : ids.get(0)));
            }
            validate();
        });
        binding.keywordLayout.setEndIconOnClickListener(v -> addKeyword());
        binding.keywordInput.setOnEditorActionListener((v, actionId, event) -> {
            // Tombol ✓ keyboard layar memberi IME_ACTION_DONE; Enter keyboard fisik memberi KeyEvent ENTER.
            boolean enterKey = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_DONE || enterKey) {
                addKeyword();
                return true;
            }
            return false;
        });

        binding.backButton.setOnClickListener(v -> {
            viewModel.flush();
            UploadNav.replaceStep(this, R.id.uploadCheckFragment, viewModel.getTemplateId());
        });
        binding.nextButton.setOnClickListener(v -> viewModel.next());
        viewModel.start(requireArguments().getString(UploadNav.ARG_TEMPLATE_ID, ""));
    }

    private void render(@Nullable Resource<DraftDto> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.getStatus()) {
            case LOADING:
                binding.content.setVisibility(View.GONE);
                binding.state.showLoading();
                break;
            case ERROR:
                binding.content.setVisibility(View.GONE);
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
                break;
            case SUCCESS:
            default:
                binding.state.hide(binding.content, () -> fill(resource.getData()));
                break;
        }
    }

    /** Mengisi form sekali dari draft; setelah itu form menjadi sumber kebenaran (tidak ditimpa saat menyimpan). */
    private void fill(DraftDto draft) {
        if (filled) {
            return;
        }
        binding.nameInput.setText(draft.name);
        binding.descriptionInput.setText(draft.description);
        binding.purposeGroup.check(OnboardingUi.purposeChipId(draft.category));
        keywords.clear();
        if (draft.keywords != null) {
            keywords.addAll(draft.keywords);
        }
        renderKeywords();
        binding.viewToggle.check("desktop".equals(draft.thumbnailView) ? R.id.view_desktop : R.id.view_mobile);
        thumbnailSource = draft.thumbnailSource;
        if (draft.thumbnailUrl != null && viewModel.getThumbnail().getValue() == null) {
            thumbnailLoader.load(draft.thumbnailUrl, binding.thumbnail,
                    () -> binding.thumbnailPlaceholder.setVisibility(View.GONE));
        }
        binding.techInfo.setText(techLine(draft.techInfo));
        filled = true;
        validate();
    }

    private String techLine(@Nullable TechInfoDto tech) {
        if (tech == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        parts.add(getString(R.string.upload_tech_pages, tech.pages == null ? 0 : tech.pages.size()));
        if (tech.libraries != null) {
            parts.addAll(tech.libraries);
        }
        parts.add(FileSizes.format(tech.totalBytes));
        parts.add(getString(tech.responsive ? R.string.upload_tech_responsive : R.string.upload_tech_not_responsive));
        return String.join(" · ", parts);
    }

    // ---------- kata kunci ----------

    private void addKeyword() {
        String keyword = OnboardingUi.text(binding.keywordInput).trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            return;
        }
        Integer error = null;
        if (keywords.size() >= KEYWORDS_MAX) {
            error = R.string.upload_keyword_max;
        } else if (keyword.length() > KEYWORD_LENGTH_MAX) {
            error = R.string.upload_keyword_too_long;
        } else if (keywords.contains(keyword)) {
            error = R.string.upload_keyword_exists;
        }
        OnboardingUi.showError(binding.keywordLayout, error);
        if (error != null) {
            return;
        }
        keywords.add(keyword);
        binding.keywordInput.setText(null);
        renderKeywords();
        viewModel.setKeywords(keywords);
        validate();
    }

    private void renderKeywords() {
        binding.keywordsGroup.removeAllViews();
        for (String keyword : keywords) {
            Chip chip = new Chip(requireContext());
            chip.setText(keyword);
            chip.setCloseIconVisible(true);
            chip.setCloseIconContentDescription(getString(R.string.cd_remove_keyword, keyword));
            chip.setOnCloseIconClickListener(v -> {
                keywords.remove(keyword);
                renderKeywords();
                viewModel.setKeywords(keywords);
                validate();
            });
            binding.keywordsGroup.addView(chip);
        }
        binding.keywordLayout.setEnabled(keywords.size() < KEYWORDS_MAX);
    }

    // ---------- validasi (bagian 6.4) ----------

    private void validate() {
        String name = OnboardingUi.text(binding.nameInput).trim();
        String description = OnboardingUi.text(binding.descriptionInput).trim();
        boolean nameOk = name.length() >= NAME_MIN;
        boolean descriptionOk = description.length() >= DESCRIPTION_MIN;
        // Pesan "minimal" baru muncul setelah ada isinya, agar form kosong tidak langsung terlihat merah.
        binding.nameLayout.setError(!nameOk && !name.isEmpty() ? getString(R.string.upload_name_short) : null);
        binding.descriptionLayout.setError(!descriptionOk && !description.isEmpty()
                ? getString(R.string.upload_description_short) : null);
        boolean categoryOk = binding.purposeGroup.getCheckedChipId() != View.NO_ID;
        binding.nextButton.setEnabled(filled && nameOk && descriptionOk && categoryOk && !keywords.isEmpty());
    }

    private void bindSaveStatus(UploadInfoViewModel.SaveStatus status) {
        int text;
        switch (status) {
            case SAVING:
                text = R.string.upload_saving;
                break;
            case SAVED:
                text = R.string.upload_saved;
                break;
            case FAILED:
                text = R.string.upload_save_failed;
                break;
            case IDLE:
            default:
                binding.saveStatus.setText(null);
                return;
        }
        binding.saveStatus.setText(text);
    }

    // ---------- thumbnail (bagian 6.2) ----------

    private String currentView() {
        return binding.viewToggle.getCheckedButtonId() == R.id.view_desktop ? "desktop" : "mobile";
    }

    private int currentWidth() {
        return "desktop".equals(currentView()) ? OffscreenPage.DESKTOP_WIDTH : OffscreenPage.MOBILE_WIDTH;
    }

    private void captureAuto() {
        if (site == null) {
            return;
        }
        String view = currentView();
        viewModel.setThumbnailBusy(true);
        capture.captureTop(site.root, site.allowedHosts, currentWidth(), bitmap -> onCaptured(bitmap, "auto", view));
    }

    private void pickSection() {
        if (site == null) {
            return;
        }
        String view = currentView();
        viewModel.setThumbnailBusy(true);
        capture.loadSections(site.root, site.allowedHosts, currentWidth(), sections -> {
            if (binding == null) {
                return;
            }
            viewModel.setThumbnailBusy(false);
            if (sections.isEmpty()) {
                Snackbar.make(binding.getRoot(), R.string.upload_thumbnail_failed, Snackbar.LENGTH_LONG).show();
                return;
            }
            String[] names = new String[sections.size()];
            for (int i = 0; i < names.length; i++) {
                names[i] = sections.get(i).name;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.upload_section_pick_title)
                    .setItems(names, (d, which) -> {
                        viewModel.setThumbnailBusy(true);
                        thumbnailSectionName = names[which];
                        capture.captureSection(sections.get(which), bitmap -> onCaptured(bitmap, "section", view));
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });
    }

    /** Potret ulang section bernama sama di lebar tampilan sekarang; jika tidak ada di tampilan ini, pakai bagian atas. */
    private void recaptureSection(String name) {
        String view = currentView();
        viewModel.setThumbnailBusy(true);
        capture.loadSections(site.root, site.allowedHosts, currentWidth(), sections -> {
            if (binding == null) {
                return;
            }
            for (SectionInfo section : sections) {
                if (name.equals(section.name)) {
                    capture.captureSection(section, bitmap -> onCaptured(bitmap, "section", view));
                    return;
                }
            }
            captureAuto();
        });
    }

    private void onCaptured(@Nullable Bitmap bitmap, String source, String view) {
        if (binding == null) {
            return;
        }
        if (bitmap == null) {
            viewModel.setThumbnailBusy(false);
            Snackbar.make(binding.getRoot(), R.string.upload_thumbnail_failed, Snackbar.LENGTH_LONG).show();
            return;
        }
        thumbnailSource = source;
        if (!"section".equals(source)) {
            thumbnailSectionName = null;
        }
        viewModel.uploadThumbnail(bitmap, source, view);
    }

    private void setThumbnailButtonsEnabled(boolean enabled) {
        binding.thumbnailAuto.setEnabled(enabled);
        binding.thumbnailSection.setEnabled(enabled);
        binding.thumbnailCustom.setEnabled(enabled);
    }

    /** ✕ setelah langkah 2 lolos: pekerjaan sudah tersimpan sebagai draft (bagian 3.2). */
    private void close() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.upload_exit_title)
                .setMessage(R.string.upload_exit_draft_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.upload_exit, (d, w) -> {
                    viewModel.flush();
                    UploadNav.exit(this);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        keyboardAware.detach();
        capture.destroy();
        binding = null;
    }

    /** TextWatcher ringkas: hanya peduli teks setelah berubah. */
    static final class AfterChange implements TextWatcher {
        interface Listener {
            void onChanged(String text);
        }

        private final Listener listener;

        AfterChange(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            listener.onChanged(s.toString());
        }
    }
}
