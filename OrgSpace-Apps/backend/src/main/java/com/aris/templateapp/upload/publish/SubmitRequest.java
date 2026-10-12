package com.aris.templateapp.upload.publish;

/** Tombol Kirim (bagian 9): wajib mencentang "Saya berhak memakai semua gambar, font, dan isi dalam template ini." */
public record SubmitRequest(boolean agreedAssetRights) {
}
