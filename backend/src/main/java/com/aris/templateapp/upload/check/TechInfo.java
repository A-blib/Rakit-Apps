package com.aris.templateapp.upload.check;

import java.util.List;

/**
 * Info teknis otomatis (bagian 6.1), ditampilkan di langkah Info template dan nanti di detail galeri.
 * Disimpan sebagai JSON di kolom {@code templates.tech_info}.
 *
 * @param pages        daftar halaman HTML, index.html pertama
 * @param libraries    mis. ["Bootstrap 5.3.3", "Alpine.js 3.14.1"]
 * @param totalBytes   ukuran semua file setelah diekstrak
 * @param responsive   ada @media atau memakai framework responsif, dan punya meta viewport
 * @param cssVariables variabel CSS di :root, bahan tema global (bagian 7.11)
 */
public record TechInfo(List<String> pages, List<String> libraries, long totalBytes, boolean responsive,
                       List<CssVariable> cssVariables) {

    public record CssVariable(String name, String value) {
    }

    public int pageCount() {
        return pages.size();
    }
}
