package com.aris.templateapp.upload.check;

import com.aris.templateapp.config.AppProperties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Tahap A bagian pertama: membuka ZIP dengan aman lalu mengekstraknya ke memori (bagian 5.6 A & G).
 * Mengembalikan null jika ada error fatal; pengecekan berhenti di sini karena isi ZIP tidak bisa/tidak aman dibaca.
 */
final class ZipReader {

    private static final Pattern WINDOWS_DRIVE = Pattern.compile("^[A-Za-z]:.*");
    private static final Set<String> JUNK_NAMES = Set.of(".ds_store", "thumbs.db", "desktop.ini");

    private final AppProperties.Limits limits;
    private final Findings findings;

    ZipReader(AppProperties.Limits limits, Findings findings) {
        this.limits = limits;
        this.findings = findings;
    }

    TemplateFiles read(byte[] zipBytes, String zipName) {
        ZipArchive archive;
        try {
            archive = ZipArchive.open(zipBytes);
        } catch (ZipArchive.ZipFormatException e) {
            findings.add(CheckRule.ZIP_INVALID, e.getMessage(), zipName, null,
                    "Buat ulang ZIP: klik kanan folder project → Kirim ke → Folder terkompresi (zip).");
            return null;
        }

        if (zipBytes.length > limits.maxZipBytes()) {
            findings.add(CheckRule.ZIP_TOO_LARGE, "ZIP " + Texts.size(zipBytes.length) + ", batasnya "
                            + Texts.size(limits.maxZipBytes()) + ". " + largestFiles(archive.entries()), zipName, null,
                    "Kompres gambar atau ubah ke WebP, dan jangan sertakan video atau folder node_modules.");
            return null;
        }

        List<ZipArchive.Entry> files = new ArrayList<>();
        boolean hasNodeModules = false;
        long declaredTotal = 0;
        for (ZipArchive.Entry entry : archive.entries()) {
            if (entry.encrypted()) {
                findings.add(CheckRule.ZIP_ENCRYPTED, "ZIP ini dikunci password, sehingga isinya tidak bisa diperiksa.",
                        zipName, null, "Buat ulang ZIP tanpa password.");
                return null;
            }
            String name = entry.name().replace('\\', '/');
            if (entry.directory() || isJunk(name)) {
                continue;
            }
            if (isUnsafe(name) || entry.symlink()) {
                findings.add(CheckRule.UNSAFE_PATH, entry.symlink()
                                ? "File " + name + " adalah symlink (jalan pintas ke file lain)."
                                : "Nama file " + name + " mengarah ke luar folder ZIP.", name, null,
                        "Buat ulang ZIP langsung dari folder project tanpa symlink atau path ../");
                continue;
            }
            if (hasSegment(name, "node_modules")) {
                // node_modules tidak pernah dibutuhkan website statis; isinya tidak dihitung dan tidak dibaca.
                hasNodeModules = true;
                continue;
            }
            files.add(new ZipArchive.Entry(name, false, false, false, entry.method(), entry.compressedSize(),
                    entry.size(), entry.crc(), entry.localHeaderOffset()));
            declaredTotal += entry.size();
        }

        if (files.size() > limits.maxFiles()) {
            findings.add(CheckRule.TOO_MANY_FILES, "ZIP berisi " + files.size() + " file, batasnya " + limits.maxFiles()
                    + " file.", zipName, null, "Template wajar berisi puluhan sampai ±150 file. Hapus file yang tidak dipakai.");
            return null;
        }
        if (declaredTotal > limits.maxExtractedBytes()) {
            reportExtractedTooLarge(zipName, declaredTotal);
            return null;
        }

        String root = findRoot(files, zipName);
        if (root == null) {
            return null;
        }

        TemplateFiles result = new TemplateFiles(root);
        result.setHasNodeModules(hasNodeModules);
        long extracted = 0;
        for (ZipArchive.Entry entry : files) {
            String path = entry.name().substring(root.length());
            byte[] content;
            try {
                content = archive.read(entry, limits.maxFileBytes());
            } catch (ZipArchive.ZipFormatException e) {
                findings.add(CheckRule.ZIP_INVALID, e.getMessage(), zipName, null,
                        "Buat ulang ZIP: klik kanan folder project → Kirim ke → Folder terkompresi (zip).");
                return null;
            }
            long size = content != null ? content.length : Math.max(entry.size(), limits.maxFileBytes() + 1);
            if (content == null) {
                findings.add(CheckRule.FILE_TOO_LARGE, path + " lebih dari " + Texts.size(limits.maxFileBytes()) + ".",
                        path, null, "Tidak ada file website normal sebesar ini. Kompres atau hapus file ini.");
            }
            extracted += size;
            if (extracted > limits.maxExtractedBytes()) {
                reportExtractedTooLarge(zipName, extracted);
                return null;
            }
            result.put(path, size, content);
        }
        return result;
    }

    private void reportExtractedTooLarge(String zipName, long total) {
        findings.add(CheckRule.EXTRACTED_TOO_LARGE, "Isi ZIP setelah diekstrak lebih dari "
                        + Texts.size(limits.maxExtractedBytes()) + " (terbaca " + Texts.size(total) + ").", zipName, null,
                "Template statis yang sehat biasanya 2–6 MB. Kompres gambar dan hapus file yang tidak dipakai.");
    }

    /**
     * Folder utama = folder tempat index.html: paling luar, atau di dalam satu folder pembungkus
     * (mis. {@code toko-kue/index.html}, diterima otomatis).
     */
    private String findRoot(List<ZipArchive.Entry> files, String zipName) {
        Set<String> names = new LinkedHashSet<>();
        Set<String> topFolders = new LinkedHashSet<>();
        for (ZipArchive.Entry entry : files) {
            names.add(entry.name());
            int slash = entry.name().indexOf('/');
            topFolders.add(slash < 0 ? "" : entry.name().substring(0, slash + 1));
        }
        if (names.contains("index.html")) {
            return "";
        }
        if (topFolders.size() == 1 && !topFolders.contains("") && names.contains(topFolders.iterator().next() + "index.html")) {
            return topFolders.iterator().next();
        }

        String caseVariant = names.stream().filter(n -> n.equalsIgnoreCase("index.html")).findFirst().orElse(null);
        String deeper = names.stream().filter(n -> n.endsWith("/index.html"))
                .min(Comparator.comparingInt(String::length)).orElse(null);
        if (caseVariant != null) {
            findings.add(CheckRule.NO_INDEX, "Ada " + caseVariant + ", tetapi namanya harus index.html (huruf kecil semua).",
                    caseVariant, null, "Ganti nama file menjadi index.html lalu ZIP ulang.");
        } else if (deeper != null) {
            String folder = deeper.substring(0, deeper.length() - "index.html".length());
            findings.add(CheckRule.NO_INDEX, "index.html ditemukan di " + folder + ", bukan di folder paling luar ZIP.",
                    deeper, null, "ZIP ulang isi folder " + folder + " saja, bukan folder di atasnya.");
        } else {
            findings.add(CheckRule.NO_INDEX, "File index.html tidak ada.", zipName, null,
                    "Pastikan index.html ada di folder paling luar ZIP sebagai halaman utama.");
        }
        return null;
    }

    /** Pesan "5 file terbesar" (bagian 5.7b) agar provider tahu apa yang harus dikecilkan. */
    private static String largestFiles(List<ZipArchive.Entry> entries) {
        List<String> top = entries.stream()
                .filter(e -> !e.directory())
                .sorted(Comparator.comparingLong(ZipArchive.Entry::size).reversed())
                .limit(5)
                .map(e -> Texts.fileName(e.name().replace('\\', '/')) + " " + Texts.size(e.size()))
                .toList();
        return top.isEmpty() ? "" : "5 file terbesar: " + String.join(", ", top) + ".";
    }

    private static boolean isJunk(String name) {
        if (hasSegment(name, "__MACOSX")) {
            return true;
        }
        String file = Texts.fileName(name);
        return JUNK_NAMES.contains(file.toLowerCase(Locale.ROOT)) || file.startsWith("._");
    }

    /** Zip slip: path absolut, huruf drive Windows, atau segmen ".." yang keluar dari folder ekstrak. */
    private static boolean isUnsafe(String name) {
        return name.startsWith("/") || WINDOWS_DRIVE.matcher(name).matches() || hasSegment(name, "..");
    }

    private static boolean hasSegment(String path, String segment) {
        for (String part : path.split("/")) {
            if (part.equals(segment)) {
                return true;
            }
        }
        return false;
    }
}
