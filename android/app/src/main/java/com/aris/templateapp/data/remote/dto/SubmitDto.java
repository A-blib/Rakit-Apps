package com.aris.templateapp.data.remote.dto;

/** Body Kirim: konfirmasi hak pakai gambar, font, dan isi (alur-fitur-upload.md bagian 9). */
public class SubmitDto {
    public final boolean agreedAssetRights;

    public SubmitDto(boolean agreedAssetRights) {
        this.agreedAssetRights = agreedAssetRights;
    }
}
