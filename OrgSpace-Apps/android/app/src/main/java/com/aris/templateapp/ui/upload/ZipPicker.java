package com.aris.templateapp.ui.upload;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Pemilih file bawaan Android untuk ZIP template (alur-provider.md bagian 5.2). Bisa membuka Download, folder
 * WhatsApp, file hasil transfer USB, dan Google Drive langsung. Disaring ke ZIP; "octet-stream" ikut agar ZIP kiriman
 * WhatsApp/Drive yang jenisnya tidak dikenali tetap terlihat (RAR yang lolos lewat sini ditangani cek kilat).
 * <p>
 * Harus dibuat saat Fragment dibuat (field atau onCreate), karena launcher hasil Activity wajib didaftarkan
 * sebelum Fragment tampil.
 */
public final class ZipPicker {

    public interface Callback {
        /** @param templateId template yang diperbaiki ("Upload file perbaikan"), atau null untuk upload baru */
        void onPicked(Uri uri, String fileName, long size, @Nullable String templateId);
    }

    private static final String[] MIME_TYPES = {"application/zip", "application/x-zip-compressed",
            "application/x-zip", "application/octet-stream"};

    private final ActivityResultLauncher<String[]> launcher;
    @Nullable
    private String pendingTemplateId;

    public ZipPicker(Fragment fragment, Callback callback) {
        launcher = fragment.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                return;
            }
            ContentResolver resolver = fragment.requireContext().getContentResolver();
            String name = "template.zip";
            long size = 0;
            try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    if (!cursor.isNull(0)) {
                        name = cursor.getString(0);
                    }
                    if (!cursor.isNull(1)) {
                        size = cursor.getLong(1);
                    }
                }
            }
            callback.onPicked(uri, name, size, pendingTemplateId);
        });
    }

    public void launch(@Nullable String templateId) {
        pendingTemplateId = templateId;
        launcher.launch(MIME_TYPES);
    }
}
