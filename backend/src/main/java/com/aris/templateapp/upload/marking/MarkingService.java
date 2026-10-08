package com.aris.templateapp.upload.marking;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.provider.ProviderAccess;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.upload.check.TechInfo;
import com.aris.templateapp.upload.check.TemplateFiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Langkah 4 Upload di server: salinan HTML bernomor untuk HP, dan menyimpan tandaan (alur-fitur-upload.md bagian 7). */
@Service
public class MarkingService {

    private final ProviderAccess providerAccess;
    private final TemplateRepository templateRepository;
    private final UploadStorage storage;
    private final AppProperties.Limits limits;
    private final Clock clock;

    public MarkingService(ProviderAccess providerAccess, TemplateRepository templateRepository, UploadStorage storage,
                          AppProperties properties, Clock clock) {
        this.providerAccess = providerAccess;
        this.templateRepository = templateRepository;
        this.storage = storage;
        this.limits = properties.upload().limits();
        this.clock = clock;
    }

    /**
     * ZIP salinan bernomor: sama dengan ZIP asli (tanpa folder pembungkus), tetapi setiap elemen HTML punya
     * {@code data-tpl-id}. File provider tidak pernah diubah; salinan ini dibuat ulang jika ZIP sumber diganti.
     */
    @Transactional(readOnly = true)
    public Path workPackage(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        editableDraft(userId, templateId);
        Path work = storage.workZipPath(templateId);
        Path source = storage.sourceZipPath(templateId);
        try {
            if (!Files.exists(work) || Files.getLastModifiedTime(work).compareTo(Files.getLastModifiedTime(source)) < 0) {
                buildWorkPackage(source, work);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return work;
    }

    private void buildWorkPackage(Path source, Path work) throws IOException {
        TemplateFiles files = TemplateFiles.readZip(Files.readAllBytes(source), limits);
        if (files == null) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "File template tidak bisa dibaca. Upload ulang ZIP-nya.");
        }
        Path temp = work.resolveSibling("work.zip.tmp");
        try (OutputStream out = Files.newOutputStream(temp); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (String path : files.paths()) {
                byte[] content = files.content(path);
                if (content == null) {
                    continue;
                }
                if (files.pages().contains(path)) {
                    content = TemplateNumbering.number(new String(content, StandardCharsets.UTF_8)).outerHtml()
                            .getBytes(StandardCharsets.UTF_8);
                }
                zip.putNextEntry(new ZipEntry(path));
                zip.write(content);
                zip.closeEntry();
            }
        }
        // Ditulis ke file sementara dulu, agar HP tidak pernah mengunduh ZIP yang setengah jadi.
        Files.move(temp, work, StandardCopyOption.REPLACE_EXISTING);
    }

    @Transactional(readOnly = true)
    public MarkingData marking(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        Template template = ownedTemplate(userId, templateId);
        return template.getMarking();
    }

    /** Tombol Simpan di layar Tandai (bagian 7.12): tandaan dicek lalu disimpan ke draft. */
    @Transactional
    public MarkingData save(UUID userId, UUID templateId, MarkingData data) {
        providerAccess.requireActive(userId);
        Template template = editableDraft(userId, templateId);
        TemplateFiles files = TemplateFiles.readZip(readSource(templateId), limits);
        if (files == null) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "File template tidak bisa dibaca. Upload ulang ZIP-nya.");
        }
        Map<String, Set<Integer>> idsByPage = new HashMap<>();
        Map<String, Map<Integer, Integer>> parentsByPage = new HashMap<>();
        for (String page : files.pages()) {
            byte[] html = files.content(page);
            if (html != null) {
                Map<Integer, Integer> parents = TemplateNumbering.parents(new String(html, StandardCharsets.UTF_8));
                parentsByPage.put(page, parents);
                idsByPage.put(page, parents.keySet());
            }
        }
        Set<String> variables = new HashSet<>();
        TechInfo tech = template.getTechInfo();
        if (tech != null && tech.cssVariables() != null) {
            tech.cssVariables().forEach(v -> variables.add(v.name()));
        }
        MarkingValidator.validate(data, idsByPage, variables);
        MarkingValidator.validateNesting(data, parentsByPage);

        MarkingData ordered = data.inPageOrder();
        template.setMarking(ordered);
        template.setMarkingFieldCount(ordered.fields() == null ? 0 : ordered.fields().size());
        template.touch(clock.instant());
        return ordered;
    }

    private byte[] readSource(UUID templateId) {
        try {
            return Files.readAllBytes(storage.sourceZipPath(templateId));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Template ownedTemplate(UUID userId, UUID templateId) {
        return templateRepository.findByIdAndProviderId(templateId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    private Template editableDraft(UUID userId, UUID templateId) {
        Template template = ownedTemplate(userId, templateId);
        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "Hanya draft yang bisa ditandai.");
        }
        return template;
    }
}
