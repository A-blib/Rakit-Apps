package com.aris.templateapp.upload.publish;

import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.UploadStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Melengkapi paket template yang tayang sebelum {@code manifest.json} ada (Fase 18): manifest ditambahkan dan ukuran
 * paket dicatat. Berjalan sekali saat backend start; template yang sudah lengkap tidak disentuh lagi.
 * <p>
 * Dijalankan paling akhir ({@code Order}) agar paket dari seeder demo sudah tertulis lebih dulu.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class PackageManifestBackfill implements ApplicationRunner {

    private final TemplateRepository templateRepository;
    private final UploadStorage storage;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Template template : templateRepository.findByStatusAndPackageSizeIsNull(TemplateStatus.PUBLISHED)) {
            Path file = storage.packageZipPath(template.getId());
            if (!Files.exists(file) || template.getMarking() == null) {
                continue; // template demo lama tanpa paket: tetap tampil di galeri, tetapi belum bisa dipakai
            }
            try {
                byte[] zip = Files.readAllBytes(file);
                if (ManifestBuilder.read(zip) == null) {
                    zip = ManifestBuilder.addTo(zip, new ManifestBuilder.Info(template.getId(),
                            template.getPackageVersion(), template.getTechInfo()), template.getMarking());
                    storage.writePackage(template.getId(), zip);
                }
                template.setPackageSize((long) zip.length);
                log.info("Paket template {} dilengkapi manifest.json", template.getId());
            } catch (IOException | RuntimeException e) {
                log.warn("Paket template {} gagal dilengkapi manifest: {}", template.getId(), e.getMessage());
            }
        }
    }
}
