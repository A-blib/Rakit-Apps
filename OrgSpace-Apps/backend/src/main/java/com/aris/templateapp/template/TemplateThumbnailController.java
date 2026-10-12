package com.aris.templateapp.template;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.upload.UploadStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Tag(name = "Template", description = "Event dilihat/didownload dan hasil pengecekan template")
@RestController
@RequiredArgsConstructor
public class TemplateThumbnailController {

    private final TemplateRepository templateRepository;
    private final UploadStorage storage;
    private final CurrentUser currentUser;

    /**
     * Thumbnail template tayang boleh dilihat siapa saja (galeri). Thumbnail draft hanya untuk pemiliknya;
     * selain itu dibalas 404 agar keberadaan draft tidak bocor.
     */
    @Operation(summary = "Gambar thumbnail template")
    @GetMapping("/api/templates/{id}/thumbnail")
    public ResponseEntity<Resource> thumbnail(@PathVariable UUID id) {
        Template template = templateRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        boolean owner = template.getProviderId().equals(currentUser.optionalId());
        Path file = storage.thumbnail(id);
        if (file == null || (template.getStatus() != TemplateStatus.PUBLISHED && !owner)) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        // URL thumbnail membawa ?v=<waktu>, jadi gambar boleh disimpan lama di cache HP.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                .contentType(MediaTypeFactory.getMediaType(file.getFileName().toString()).orElse(MediaType.IMAGE_JPEG))
                .body(new FileSystemResource(file));
    }
}
