package com.aris.templateapp.docs;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.docs.dto.DocDetailResponse;
import com.aris.templateapp.docs.dto.DocRequest;
import com.aris.templateapp.docs.dto.DocSummaryResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin: Dokumentasi", description = "Kelola artikel pusat dokumentasi (khusus admin)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/admin/docs")
@RequiredArgsConstructor
public class AdminDocsController {

    private final DocService docService;
    private final CurrentUser currentUser;

    @Operation(summary = "Semua artikel (termasuk draft)")
    @GetMapping
    public List<DocSummaryResponse> list() {
        return docService.all();
    }

    @Operation(summary = "Satu artikel (termasuk draft)")
    @GetMapping("/{id}")
    public DocDetailResponse get(@PathVariable UUID id) {
        return docService.get(id);
    }

    @Operation(summary = "Buat artikel", description = "409 DOC_SLUG_TAKEN jika slug sudah dipakai.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocDetailResponse create(@Valid @RequestBody DocRequest request) {
        return docService.create(currentUser.id(), request);
    }

    @Operation(summary = "Ubah artikel (semua field dikirim ulang)")
    @PutMapping("/{id}")
    public DocDetailResponse update(@PathVariable UUID id, @Valid @RequestBody DocRequest request) {
        return docService.update(currentUser.id(), id, request);
    }

    @Operation(summary = "Hapus artikel")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        docService.delete(id);
    }
}
