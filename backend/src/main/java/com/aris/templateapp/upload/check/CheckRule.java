package com.aris.templateapp.upload.check;

import com.aris.templateapp.template.IssueSeverity;

import static com.aris.templateapp.template.IssueSeverity.ERROR;
import static com.aris.templateapp.template.IssueSeverity.WARNING;

/**
 * Daftar aturan pengecekan file template (docs/rancangan/alur-fitur-upload.md bagian 5.6).
 * <p>
 * Kode aturan adalah kontrak: dipakai app untuk membuka artikel Panduan (bagian 5.11), disimpan di laporan
 * "Ini keliru?" (bagian 5.9), dan menjadi nama folder ZIP uji (bagian 5.10). Saat aturan diperketat,
 * naikkan {@code version} agar template lama bisa dicek ulang (bagian 5.7).
 * <p>
 * Setiap aturan baru wajib punya pasangan {@code gagal.zip} & {@code lolos.zip} di
 * {@code src/test/resources/test-fixtures/<KODE>/}; test {@code CheckRuleFixturesTest} akan gagal jika belum ada.
 */
public enum CheckRule {

    // ---------- A. ZIP dan struktur ----------
    ZIP_INVALID(ERROR, 1, "ZIP rusak atau tidak bisa dibuka", true),
    ZIP_ENCRYPTED(ERROR, 1, "ZIP dikunci password", true),
    NO_INDEX(ERROR, 1, "File index.html tidak ada", true),
    ZIP_TOO_LARGE(ERROR, 1, "Ukuran ZIP melebihi batas", true),
    EXTRACTED_TOO_LARGE(ERROR, 1, "Isi ZIP terlalu besar setelah diekstrak", true),
    TOO_MANY_FILES(ERROR, 1, "Jumlah file terlalu banyak", true),
    UNSAFE_PATH(ERROR, 1, "Nama file berbahaya di dalam ZIP"),
    TOO_MANY_PAGES(ERROR, 1, "Halaman HTML terlalu banyak"),
    SOURCE_NOT_BUILT(ERROR, 1, "Sepertinya ini kode sumber, bukan hasil build"),
    UNBUILT_RESOURCE(ERROR, 1, "Ada file yang belum di-build"),
    // Keputusan Aris (7 Okt 2026): file sumber .scss/.less yang tidak dipakai HTML tidak diabaikan diam-diam,
    // tetapi dilaporkan sebagai Peringatan agar provider tahu file itu ikut terupload.
    SOURCE_FILES_INCLUDED(WARNING, 1, "Ada file sumber yang ikut terupload"),
    CASE_MISMATCH(ERROR, 1, "Huruf besar/kecil nama file tidak cocok"),
    FILE_NAME_STYLE(WARNING, 1, "Nama file memakai spasi atau karakter khusus"),

    // ---------- B. Jenis file ----------
    FILE_TYPE_NOT_ALLOWED(ERROR, 1, "Jenis file tidak diizinkan"),
    SERVER_SCRIPT(ERROR, 1, "Template ini butuh server (PHP)"),

    // ---------- C. HTML ----------
    HTML_UNREADABLE(ERROR, 1, "File HTML kosong atau tidak bisa dibaca"),
    JS_RENDERED_CONTENT(ERROR, 1, "Konten utama dibuat oleh JavaScript"),
    JS_PARTIAL_CONTENT(WARNING, 1, "Sebagian konten dibuat oleh JavaScript"),
    FORM_EXTERNAL_ACTION(ERROR, 1, "Form mengirim data ke situs lain"),
    AUTO_REDIRECT(ERROR, 1, "Halaman berpindah otomatis ke situs lain"),
    NO_VIEWPORT(WARNING, 1, "Tidak ada meta viewport"),
    NO_TITLE(WARNING, 1, "Halaman tanpa judul (title)"),
    IMG_NO_ALT(WARNING, 1, "Gambar tanpa teks alternatif (alt)"),
    COMPONENT_FRAMEWORK(ERROR, 1, "Template React/Vue/Svelte belum didukung"),
    TOO_FEW_EDITABLE(ERROR, 1, "Hampir tidak ada teks atau gambar yang bisa ditandai"),
    RESERVED_ATTRIBUTE(ERROR, 1, "Memakai atribut khusus app"),
    BASE_HREF(ERROR, 1, "Ada tag <base href>"),
    PLUGIN_TAG(ERROR, 1, "Memakai teknologi plugin lama"),
    CHARSET(WARNING, 1, "Encoding bukan UTF-8 atau tanpa meta charset"),
    DOCTYPE_LANG(WARNING, 1, "Tanpa <!DOCTYPE html> atau atribut lang"),
    DUPLICATE_ID(WARNING, 1, "Ada id yang dipakai lebih dari sekali"),
    BASE64_IMAGE(WARNING, 1, "Gambar besar ditanam langsung di HTML"),

    // ---------- D. File yang dirujuk dan antar-halaman ----------
    MISSING_ASSET(ERROR, 1, "File CSS/JS yang dirujuk tidak ada"),
    MISSING_IMAGE(WARNING, 1, "Gambar yang dirujuk tidak ada"),
    BROKEN_PAGE_LINK(ERROR, 1, "Link ke halaman yang tidak ada"),
    BROKEN_ANCHOR(WARNING, 1, "Link #anchor ke id yang tidak ada"),
    ORPHAN_PAGE(WARNING, 1, "Halaman tidak bisa dibuka dari menu"),
    HOTLINK_IMAGE(WARNING, 1, "Gambar diambil langsung dari situs lain"),
    ROOT_ABSOLUTE_PATH(WARNING, 1, "Path diawali tanda /"),

    // ---------- C2. CSS ----------
    CSS_MISSING_URL(WARNING, 1, "Gambar/font di CSS tidak ada"),
    CSS_SYNTAX(WARNING, 1, "Kesalahan penulisan CSS"),
    CSS_SCRIPT_TRICK(ERROR, 1, "CSS menjalankan script"),
    NOT_RESPONSIVE(WARNING, 1, "Tampilan mungkin tidak menyesuaikan layar HP"),
    CSS_TOO_LARGE(WARNING, 1, "File CSS terlalu besar"),

    // ---------- E. Library dan sumber dari luar ----------
    CDN_NOT_ALLOWED(ERROR, 1, "Library belum diizinkan"),
    CDN_VERSION_UNCLEAR(ERROR, 1, "Versi library tidak jelas"),
    UNTRUSTED_SOURCE(ERROR, 1, "Script/CSS dari situs yang bukan CDN terpercaya"),
    TAILWIND_PLAY_CDN(WARNING, 1, "Memakai Tailwind Play CDN"),
    GOOGLE_FONTS(WARNING, 1, "Font dari Google Fonts butuh internet"),
    JS_CDN_IMPORT(ERROR, 1, "import library dari CDN di dalam file JS"),
    TRACKER(ERROR, 1, "Ada analytics atau pelacak"),
    REMOTE_DATA_SDK(ERROR, 1, "Library yang mengambil data dari server"),
    IFRAME_NOT_ALLOWED(ERROR, 1, "iframe dari situs yang tidak diizinkan"),

    // ---------- F. JavaScript ----------
    EXT_FETCH(ERROR, 1, "JavaScript mengirim data ke situs lain"),
    JS_EVAL(WARNING, 1, "Memakai eval() atau new Function()"),
    HIDDEN_EXTERNAL_LINK(ERROR, 1, "Link ke situs luar disembunyikan"),
    SERVICE_WORKER(ERROR, 1, "Mendaftarkan Service Worker"),
    OBFUSCATED_JS(ERROR, 1, "Kode JavaScript diacak (obfuscated)"),
    CRYPTO_MINER(ERROR, 1, "Pola penambang kripto"),
    STRING_TIMER(WARNING, 1, "setTimeout/setInterval dengan teks"),
    DOCUMENT_WRITE(WARNING, 1, "Memakai document.write()"),
    POPUP_ON_LOAD(WARNING, 1, "Ada popup (alert, confirm, window.open)"),
    JS_SYNTAX(WARNING, 1, "Kesalahan penulisan JavaScript"),
    // Dua aturan ini ditangkap WebView di HP (tahap C), bukan oleh server.
    JS_RUNTIME_ERROR(WARNING, 1, "Error JavaScript saat halaman dijalankan"),
    HORIZONTAL_OVERFLOW(WARNING, 1, "Ada elemen yang lebih lebar dari layar HP"),

    // ---------- G. Ukuran dan performa ----------
    FILE_TOO_LARGE(ERROR, 1, "Ada file yang terlalu besar"),
    IMAGE_TOO_LARGE(WARNING, 1, "Gambar terlalu besar"),
    PAGE_TOO_HEAVY(WARNING, 1, "Halaman terlalu berat");

    private final IssueSeverity severity;
    private final int version;
    private final String title;
    // Error fatal menghentikan pengecekan di tahap A (bagian 5.6): isi ZIP tidak bisa/tidak aman dibaca lebih lanjut.
    private final boolean fatal;

    CheckRule(IssueSeverity severity, int version, String title) {
        this(severity, version, title, false);
    }

    CheckRule(IssueSeverity severity, int version, String title, boolean fatal) {
        this.severity = severity;
        this.version = version;
        this.title = title;
        this.fatal = fatal;
    }

    public IssueSeverity severity() {
        return severity;
    }

    public int version() {
        return version;
    }

    public String title() {
        return title;
    }

    public boolean fatal() {
        return fatal;
    }

    /** Aturan yang hasilnya datang dari WebView HP, sehingga tidak diuji dengan ZIP di server. */
    public boolean checkedOnDevice() {
        return this == JS_RUNTIME_ERROR || this == HORIZONTAL_OVERFLOW;
    }
}
