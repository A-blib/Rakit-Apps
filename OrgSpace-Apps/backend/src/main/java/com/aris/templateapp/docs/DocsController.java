package com.aris.templateapp.docs;

import com.aris.templateapp.docs.dto.DocDetailResponse;
import com.aris.templateapp.docs.dto.DocTreeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Publik: Dokumentasi", description = "Pusat dokumentasi untuk web (tanpa login, hanya artikel tayang)")
@RestController
@RequestMapping("/api/public/docs")
@RequiredArgsConstructor
public class DocsController {

    private final DocService docService;

    @Operation(summary = "Menu dokumentasi: kategori + artikel tayang")
    @GetMapping
    public DocTreeResponse tree() {
        return docService.tree();
    }

    @Operation(summary = "Satu artikel tayang", description = "404 DOC_NOT_FOUND jika slug tidak ada atau masih draft.")
    @GetMapping("/{slug}")
    public DocDetailResponse article(@PathVariable String slug) {
        return docService.published(slug);
    }
}
