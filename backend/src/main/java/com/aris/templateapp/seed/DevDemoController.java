package com.aris.templateapp.seed;

import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint khusus pengembangan (hanya ada di profile dev): menghapus data demo provider, agar Dashboard Provider
 * bisa dicoba lagi dalam kondisi kosong. Di profile prod class ini tidak dibuat, jadi path-nya tidak ada.
 */
@Tag(name = "Dev", description = "Alat bantu khusus profile dev")
@Profile("dev")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevDemoController {

    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final UploadStorage storage;

    @Operation(summary = "Hapus akun demo provider beserta semua template, event, dan notifikasinya")
    @DeleteMapping("/demo-templates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteDemoTemplates() {
        // ON DELETE CASCADE di migrasi ikut menghapus identitas, profil, template, pengecekan, event, dan notifikasi.
        // File ZIP/paket di folder uploads tidak ikut terhapus database, jadi dihapus di sini.
        userRepository.findByEmailIgnoreCase(DemoTemplateSeeder.DEMO_EMAIL).ifPresent(user -> {
            templateRepository.findByProviderId(user.getId()).forEach(t -> storage.deleteTemplate(t.getId()));
            userRepository.delete(user);
        });
    }
}
