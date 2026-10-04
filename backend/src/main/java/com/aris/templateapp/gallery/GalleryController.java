package com.aris.templateapp.gallery;

import com.aris.templateapp.gallery.dto.GalleryPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Galeri", description = "Galeri template untuk pembuat website (publik, tanpa login)")
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @Operation(summary = "Daftar template yang tayang",
            description = "Hanya template published dari provider aktif. \"Template untuk anda\" memakai endpoint ini "
                    + "dengan category={tujuan website}&sort=popular&size=6.")
    @GetMapping
    public GalleryPageResponse list(
            @Parameter(description = "all, sekolah, organisasi, umkm, instansi, pribadi, lainnya")
            @RequestParam(required = false) String category,
            @Parameter(description = "Cari berdasarkan nama template") @RequestParam(required = false) String q,
            @Parameter(description = "popular (bawaan) atau newest") @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "1–50, bawaan 20") @RequestParam(required = false) Integer size) {
        return galleryService.list(category, q, sort, page, size);
    }
}
