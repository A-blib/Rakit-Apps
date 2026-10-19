package com.aris.templateapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Hasil cek nama website sebelum terbit (GET projects/publish-direct/slug). */
public class SlugCheckDto {
    @SerializedName("slug")
    public String slug;

    /** true: website sudah pernah terbit, nama tidak bisa diganti agar link lama tetap jalan. */
    @SerializedName("locked")
    public boolean locked;

    @SerializedName("available")
    public boolean available;

    @SerializedName("message")
    public String message;

    @SerializedName("url")
    public String url;
}
