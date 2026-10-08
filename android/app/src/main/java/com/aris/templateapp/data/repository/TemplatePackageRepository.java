package com.aris.templateapp.data.repository;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.template.DownloadHandle;
import com.aris.templateapp.core.template.PackageExtractor;
import com.aris.templateapp.core.template.TemplateFiles;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.local.TemplatePackageDao;
import com.aris.templateapp.data.local.TemplatePackageEntity;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.TemplateManifest;
import com.aris.templateapp.data.remote.api.GalleryApi;
import com.aris.templateapp.data.remote.dto.GalleryTemplateDetailDto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Paket template di HP (alur-buat-website-via-template.md bagian 5 & 11): detail dari server, unduh dengan progres,
 * ekstrak aman, dan catatan paket di tabel {@code template_packages}. Semua method berjalan di thread latar.
 */
@Singleton
public class TemplatePackageRepository {

    /** Kode error lokal: paket rusak/tidak aman/tidak lengkap. */
    public static final String PACKAGE_INVALID = "PACKAGE_INVALID";
    /** Kode error lokal: template tayang tetapi belum punya paket (template demo lama). */
    public static final String PACKAGE_MISSING = "PACKAGE_MISSING";
    public static final String CANCELLED = "CANCELLED";

    /** Paket yang tidak dibuka selama ini dan tidak dipakai project boleh dihapus (bagian 11.1). */
    static final Duration UNUSED_PACKAGE_AGE = Duration.ofDays(30);

    /** Dipanggil berkala selama unduhan berjalan. */
    public interface ProgressListener {
        void onProgress(long downloaded, long total);
    }

    private final GalleryApi api;
    private final ApiErrorParser errorParser;
    private final TemplatePackageDao dao;
    private final TemplateFiles files;
    private final File cacheDir;
    private final Clock clock;

    @Inject
    public TemplatePackageRepository(GalleryApi api, ApiErrorParser errorParser, TemplatePackageDao dao,
                                     TemplateFiles files, @ApplicationContext Context context) {
        this.api = api;
        this.errorParser = errorParser;
        this.dao = dao;
        this.files = files;
        this.cacheDir = context.getCacheDir();
        this.clock = Clock.systemUTC();
    }

    @WorkerThread
    public Resource<GalleryTemplateDetailDto> detail(String templateId) {
        try {
            Response<GalleryTemplateDetailDto> response = api.detail(templateId).execute();
            GalleryTemplateDetailDto body = response.body();
            return response.isSuccessful() && body != null
                    ? Resource.success(body) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /**
     * Paket terbaru yang sudah ada di HP dan foldernya masih utuh, atau null. Versi template belum dibangun
     * (bagian 4.2), jadi paket mana pun yang tersimpan dianggap "versi sama".
     */
    @WorkerThread
    @Nullable
    public TemplatePackageEntity installed(String templateId) {
        TemplatePackageEntity entity = dao.findLatest(templateId);
        if (entity == null) {
            return null;
        }
        if (!new File(entity.path, TemplateFiles.MANIFEST).isFile()) {
            // Folder terhapus (mis. "Hapus data" sebagian): anggap belum diunduh.
            dao.delete(entity.templateId, entity.version);
            return null;
        }
        return entity;
    }

    @WorkerThread
    @Nullable
    public TemplatePackageEntity find(String templateId, int version) {
        return dao.find(templateId, version);
    }

    @WorkerThread
    @Nullable
    public TemplateManifest manifest(TemplatePackageEntity entity) {
        return files.readManifest(new File(entity.path));
    }

    @WorkerThread
    public void markUsed(TemplatePackageEntity entity) {
        dao.markUsed(entity.templateId, entity.version, clock.millis());
    }

    /**
     * Mengunduh paket ke file sementara di cache, lalu mengekstraknya ke folder sementara dan baru memindahkannya ke
     * folder final setelah lengkap. Jika dibatalkan atau gagal di tengah, file sementara dihapus; unduhan berikutnya
     * dimulai dari awal (bagian 5: "boleh dimulai ulang dari awal di versi ini").
     */
    @WorkerThread
    public Resource<TemplatePackageEntity> download(GalleryTemplateDetailDto detail, DownloadHandle handle,
                                                    ProgressListener listener) {
        File zip = new File(cacheDir, "paket-" + detail.id + ".zip");
        File temp = files.packageTempDir(detail.id, detail.version);
        try {
            Call<ResponseBody> call = api.templatePackage(detail.id);
            handle.attach(call);
            Response<ResponseBody> response = call.execute();
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                ApiError error = errorParser.parse(response);
                return Resource.error("NOT_FOUND".equals(error.getCode())
                        ? ApiError.of(PACKAGE_MISSING) : error);
            }
            long total = body.contentLength() > 0 ? body.contentLength()
                    : detail.packageSizeBytes == null ? -1 : detail.packageSizeBytes;
            try (InputStream in = body.byteStream(); OutputStream out = new FileOutputStream(zip)) {
                byte[] buffer = new byte[32 * 1024];
                long downloaded = 0;
                int n;
                while ((n = in.read(buffer)) > 0) {
                    if (handle.isCancelled()) {
                        return Resource.error(ApiError.of(CANCELLED));
                    }
                    out.write(buffer, 0, n);
                    downloaded += n;
                    listener.onProgress(downloaded, total);
                }
            }
            if (handle.isCancelled()) {
                return Resource.error(ApiError.of(CANCELLED));
            }

            TemplateFiles.deleteRecursively(temp);
            if (!temp.mkdirs()) {
                throw new IOException("Folder paket tidak bisa dibuat");
            }
            try (InputStream in = new FileInputStream(zip)) {
                PackageExtractor.extract(in, temp, PackageExtractor.MAX_EXTRACTED_BYTES);
            }
            if (files.readManifest(temp) == null) {
                throw new PackageExtractor.InvalidPackageException("manifest.json tidak bisa dibaca");
            }
            File target = files.packageDir(detail.id, detail.version);
            TemplateFiles.deleteRecursively(target);
            if (!temp.renameTo(target)) {
                throw new IOException("Folder paket tidak bisa dipindah");
            }
            long now = clock.millis();
            TemplatePackageEntity entity = new TemplatePackageEntity(detail.id, detail.version, detail.name,
                    target.getAbsolutePath(), zip.length(), now, now);
            entity.creatorName = detail.creatorName;
            entity.category = detail.category;
            dao.upsert(entity);
            return Resource.success(entity);
        } catch (PackageExtractor.InvalidPackageException e) {
            return Resource.error(ApiError.of(PACKAGE_INVALID));
        } catch (IOException e) {
            return Resource.error(handle.isCancelled() ? ApiError.of(CANCELLED) : errorParser.parse(e));
        } finally {
            //noinspection ResultOfMethodCallIgnored
            zip.delete();
            TemplateFiles.deleteRecursively(temp);
        }
    }

    /** Menghapus paket lama yang tidak dibuka 30 hari dan tidak dipakai project mana pun (bagian 11.1). */
    @WorkerThread
    public int deleteUnusedPackages() {
        int count = 0;
        for (TemplatePackageEntity entity : dao.findUnused(clock.millis() - UNUSED_PACKAGE_AGE.toMillis())) {
            TemplateFiles.deleteRecursively(new File(entity.path));
            dao.delete(entity.templateId, entity.version);
            count++;
        }
        return count;
    }
}
