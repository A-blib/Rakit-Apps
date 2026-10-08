package com.aris.templateapp.core.template;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.data.model.ProjectValues;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Folder project template mode di HP (alur-buat-website-via-template.md bagian 11.2):
 * <pre>
 * files/projects/{projectId}/
 * ├── values.json      ← semua nilai isian, gaya, tema
 * ├── thumbnail.webp
 * └── images/          ← gambar pilihan user (WebP)
 * </pre>
 */
@Singleton
public class ProjectStore {

    public static final String VALUES = "values.json";
    public static final String THUMBNAIL = "thumbnail.webp";
    public static final String IMAGES = "images";

    private final TemplateFiles files;
    private final Gson gson;

    @Inject
    public ProjectStore(TemplateFiles files, Gson gson) {
        this.files = files;
        this.gson = gson;
    }

    public File dir(String projectId) {
        return files.projectDir(projectId);
    }

    public File imagesDir(String projectId) {
        return new File(dir(projectId), IMAGES);
    }

    public File thumbnail(String projectId) {
        return new File(dir(projectId), THUMBNAIL);
    }

    /** Nilai project; values.json yang belum ada atau rusak dianggap kosong (semua masih isi template). */
    @WorkerThread
    public ProjectValues read(String projectId) {
        File file = new File(dir(projectId), VALUES);
        if (!file.isFile()) {
            return new ProjectValues();
        }
        try (InputStream in = new FileInputStream(file); Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            ProjectValues values = gson.fromJson(reader, ProjectValues.class);
            return values == null ? new ProjectValues() : normalize(values);
        } catch (IOException | JsonParseException e) {
            return new ProjectValues();
        }
    }

    /**
     * Ditulis ke file sementara lalu diganti namanya: jika app tertutup di tengah menulis, values.json lama tetap
     * utuh (tidak setengah tertulis).
     */
    @WorkerThread
    public void write(String projectId, ProjectValues values) throws IOException {
        File folder = dir(projectId);
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Folder project tidak bisa dibuat");
        }
        File temp = new File(folder, VALUES + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write(gson.toJson(values).getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        if (!temp.renameTo(new File(folder, VALUES))) {
            throw new IOException("values.json tidak bisa disimpan");
        }
    }

    /** Salinan seluruh folder project (untuk Duplikat). */
    @WorkerThread
    public void copy(String fromProjectId, String toProjectId) throws IOException {
        copyRecursively(dir(fromProjectId), dir(toProjectId));
    }

    @WorkerThread
    public void delete(String projectId) {
        TemplateFiles.deleteRecursively(dir(projectId));
    }

    private static ProjectValues normalize(ProjectValues values) {
        ProjectValues clean = new ProjectValues();
        if (values.fields != null) {
            values.fields.forEach((k, v) -> {
                if (k != null && v != null) {
                    clean.fields.put(k, v);
                }
            });
        }
        if (values.styles != null) {
            values.styles.forEach((k, v) -> {
                if (k != null && v != null) {
                    clean.styles.put(k, v);
                }
            });
        }
        if (values.theme != null) {
            clean.theme.putAll(values.theme);
        }
        return clean;
    }

    private static void copyRecursively(File from, File to) throws IOException {
        if (!from.exists()) {
            return;
        }
        if (from.isDirectory()) {
            if (!to.isDirectory() && !to.mkdirs()) {
                throw new IOException("Folder tidak bisa dibuat: " + to);
            }
            File[] children = from.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyRecursively(child, new File(to, child.getName()));
                }
            }
            return;
        }
        try (InputStream in = new FileInputStream(from); OutputStream out = new FileOutputStream(to)) {
            byte[] buffer = new byte[32 * 1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        }
    }
}
