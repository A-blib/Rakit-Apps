package com.aris.templateapp.gallery;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.gallery.dto.TemplateDetailResponse;
import com.aris.templateapp.provider.ProviderProfile;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.provider.ProviderStatus;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.upload.check.TechInfo;
import com.aris.templateapp.upload.marking.MarkingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Detail dan paket template untuk pembuat website (alur-buat-website-via-template.md bagian 12). Aturan tampil sama
 * dengan galeri: hanya template {@code published} dari provider {@code active}; selain itu 404.
 */
@Service
@RequiredArgsConstructor
public class TemplatePackageService {

    private final TemplateRepository templateRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final GalleryQueries galleryQueries;
    private final UploadStorage storage;

    @Transactional(readOnly = true)
    public TemplateDetailResponse detail(UUID id) {
        Template template = visible(id);
        ProviderProfile provider = providerProfileRepository.findById(template.getProviderId()).orElseThrow();
        TechInfo tech = template.getTechInfo();
        MarkingData marking = template.getMarking();
        List<String> pages = marking == null || marking.pages() == null ? List.of()
                : marking.pages().stream().map(MarkingData.Page::name).toList();
        return new TemplateDetailResponse(template.getId(), template.getName(), template.getCategory(),
                template.getDescription(), template.getKeywords(), provider.getCreatorName(), template.getThumbnailUrl(),
                galleryQueries.downloads(id), template.getPublishedAt(), pages,
                tech == null || tech.libraries() == null ? List.of() : tech.libraries(),
                tech != null && tech.responsive(), template.getPackageVersion(), template.getPackageSize());
    }

    /** File package.zip template yang tayang. */
    @Transactional(readOnly = true)
    public Package packageFile(UUID id) {
        Template template = visible(id);
        Path file = storage.packageZipPath(id);
        if (template.getPackageSize() == null || !Files.exists(file)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Template ini belum punya paket.");
        }
        return new Package(file, template.getPackageVersion());
    }

    public record Package(Path file, int version) {
    }

    private Template visible(UUID id) {
        Template template = templateRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        boolean active = providerProfileRepository.findById(template.getProviderId())
                .map(p -> p.getStatus() == ProviderStatus.ACTIVE).orElse(false);
        if (template.getStatus() != TemplateStatus.PUBLISHED || !active) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        return template;
    }
}
