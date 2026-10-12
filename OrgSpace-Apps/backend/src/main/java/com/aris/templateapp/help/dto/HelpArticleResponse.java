package com.aris.templateapp.help.dto;

/**
 * Artikel Panduan: kenapa ini masalah, contoh salah ✕ dan benar ✓, cara memperbaiki, tips mencegah.
 * Contoh boleh null untuk aturan yang tidak cocok diberi contoh kode.
 */
public record HelpArticleResponse(String code, String title, String why, String wrongExample, String rightExample,
                                  String howToFix, String tips) {
}
