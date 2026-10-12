package com.aris.templateapp.core.template;

import android.content.Context;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.TemplateManifest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Lokasi file template mode di penyimpanan internal app (alur-buat-website-via-template.md bagian 11.2):
 * <pre>
 * files/templates/{templateId}/{version}/   ← paket hasil unduh (TIDAK PERNAH diubah)
 * files/projects/{projectId}/               ← values.json, thumbnail.webp, images/
 * </pre>
 */
@Singleton
public class TemplateFiles {

    public static final String MANIFEST = "manifest.json";

    private final File templatesDir;
    private final File projectsDir;
    private final Gson gson;

    @Inject
    public TemplateFiles(@ApplicationContext Context context, Gson gson) {
        this(new File(context.getFilesDir(), "templates"), new File(context.getFilesDir(), "projects"), gson);
    }

    TemplateFiles(File templatesDir, File projectsDir, Gson gson) {
        this.templatesDir = templatesDir;
        this.projectsDir = projectsDir;
        this.gson = gson;
    }

    public File packageDir(String templateId, int version) {
        return new File(new File(templatesDir, safe(templateId)), String.valueOf(version));
    }

    /** Folder sementara saat mengekstrak; dipindah ke {@link #packageDir} setelah lengkap. */
    public File packageTempDir(String templateId, int version) {
        return new File(new File(templatesDir, safe(templateId)), ".tmp-" + version);
    }

    public File projectDir(String projectId) {
        return new File(projectsDir, safe(projectId));
    }

    @Nullable
    public TemplateManifest readManifest(File packageDir) {
        File file = new File(packageDir, MANIFEST);
        try (InputStream in = new FileInputStream(file); Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, TemplateManifest.class);
        } catch (IOException | JsonParseException e) {
            return null;
        }
    }

    public static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /** ID dari server/Room dipakai sebagai nama folder; karakter selain huruf, angka, dan '-' dibuang. */
    static String safe(String id) {
        String cleaned = id.replaceAll("[^A-Za-z0-9-]", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("ID tidak valid");
        }
        return cleaned;
    }
}
