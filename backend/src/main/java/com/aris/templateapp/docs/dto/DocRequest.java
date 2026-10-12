package com.aris.templateapp.docs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Membuat / mengubah artikel (admin). {@code status = published} menayangkan artikel; tanggal tayang pertama disimpan.
 */
public record DocRequest(
        @NotBlank(message = "Slug wajib diisi") @Size(max = 120)
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug hanya huruf kecil, angka, dan tanda hubung")
        String slug,
        @NotBlank @Pattern(regexp = "mulai|ruang-kerja|website|template|terbit|provider|akun|faq",
                message = "Kategori tidak dikenal") String category,
        @Pattern(regexp = "all|creator|provider", message = "Pembaca tidak dikenal") String audience,
        @NotBlank(message = "Judul wajib diisi") @Size(max = 150) String title,
        @Size(max = 300) String summary,
        @NotBlank(message = "Isi wajib diisi") @Size(max = 100_000) String body,
        @Min(0) @Max(10_000) Integer sortOrder,
        @Pattern(regexp = "draft|published", message = "Status tidak dikenal") String status) {
}
