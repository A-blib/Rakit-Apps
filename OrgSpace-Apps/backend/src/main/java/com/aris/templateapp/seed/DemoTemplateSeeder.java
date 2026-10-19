package com.aris.templateapp.seed;

import com.aris.templateapp.auth.UserIdentity;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.notification.NotificationService;
import com.aris.templateapp.notification.NotificationType;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.provider.ProviderProfile;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckIssue;
import com.aris.templateapp.template.TemplateCheckIssueRepository;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.UploadCompletedEvent;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.upload.check.CheckStage;
import com.aris.templateapp.upload.check.Finding;
import com.aris.templateapp.upload.check.TemplateChecker;
import com.aris.templateapp.user.ActiveMode;
import com.aris.templateapp.user.CreatorProfile;
import com.aris.templateapp.user.CreatorProfileRepository;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Seeder demo Dashboard Provider (alur-provider.md bagian 5.0): satu akun provider demo dengan template berbagai
 * status di wizard Upload (gagal cek, sedang dicek, draft, dinonaktifkan) beserta notifikasinya.
 * Template tayang demo (statis, tanpa modul data-ws) sudah dihapus karena tidak sesuai aturan modul terbaru;
 * template tayang dibuat lewat Upload sungguhan, mis. paket contoh-modul-orgspace di OrgSpace-Website.
 * <p>
 * Hanya profile dev dan MATI secara bawaan: nyalakan dengan {@code --app.seed.demo-templates=true}.
 * Akun provider lain tetap kosong karena data demo hanya milik akun demo. Hapus lewat
 * {@code DELETE /api/dev/demo-templates} (DevDemoController).
 */
@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.demo-templates", havingValue = "true")
// Dijalankan setelah DummyDataSeeder, karena DummyDataSeeder hanya mengisi jika tabel users masih kosong.
@Order(2)
@RequiredArgsConstructor
public class DemoTemplateSeeder implements ApplicationRunner {

    public static final String DEMO_EMAIL = "demo-provider@templateapp.test";

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRepository checkRepository;
    private final TemplateCheckIssueRepository issueRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final UploadStorage storage;
    private final TemplateChecker checker;
    private final ApplicationEventPublisher events;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmailIgnoreCase(DEMO_EMAIL).isPresent()) {
            log.info("Seeder demo provider dilewati: akun demo sudah ada");
            return;
        }
        Instant now = clock.instant();

        User user = new User("Studio Demo", DEMO_EMAIL);
        user.setOnboardingCompleted(true);
        user.setActiveMode(ActiveMode.PROVIDER);
        userRepository.save(user);
        identityRepository.save(UserIdentity.local(user, DEMO_EMAIL, passwordEncoder.encode(DummyDataSeeder.PASSWORD)));
        creatorProfileRepository.save(new CreatorProfile(user.getId()));
        ProviderProfile provider = new ProviderProfile(user.getId(), "Studio Demo", now.minus(Duration.ofDays(40)));
        provider.setBio("Akun contoh untuk melihat Dashboard Provider berisi data.");
        provider.setSpecialties(List.of("Sekolah", "UMKM"));
        providerProfileRepository.save(provider);

        UUID owner = user.getId();
        Template event = template(owner, "Landing Event", OrgCategory.LAINNYA, TemplateStatus.CHECK_FAILED, 1, now, 10, 7);
        Template organisasi = template(owner, "Organisasi Pemuda", OrgCategory.BEM_DEMA_DPM, TemplateStatus.CHECKING, 0, now, 1, 0);
        Template instansi = template(owner, "Instansi Desa", OrgCategory.PRAMUKA, TemplateStatus.DRAFT, 0, now, 3, 3);
        template(owner, "Undangan Lama", OrgCategory.LAINNYA, TemplateStatus.DISABLED, 0, now, 60, 30);

        TemplateCheck eventCheck = checkRepository.save(new TemplateCheck(event.getId(), 1, CheckStatus.FAILED, now.minus(Duration.ofDays(7))));
        TemplateCheck organisasiCheck = checkRepository.save(new TemplateCheck(organisasi.getId(), 1, CheckStatus.RUNNING, null));
        attachDemoZips(event, eventCheck, organisasi, organisasiCheck, instansi);

        notificationService.create(owner, NotificationType.TEMPLATE_CHECK_FAILED, event.getId(),
                "Landing Event tidak lolos pengecekan", "Ada 2 error yang perlu diperbaiki sebelum template bisa tayang.");
        log.info("Seeder demo provider selesai: login {} / password {}", DEMO_EMAIL, DummyDataSeeder.PASSWORD);
    }

    private Template template(UUID owner, String name, OrgCategory category, TemplateStatus status, int warnings,
                              Instant now, int createdDaysAgo, int updatedDaysAgo) {
        Template template = new Template(owner, name, category);
        template.setStatus(status);
        template.setWarningCount(warnings);
        template.setCreatedAt(now.minus(Duration.ofDays(createdDaysAgo)));
        template.setUpdatedAt(now.minus(Duration.ofDays(updatedDaysAgo)));
        if (status == TemplateStatus.PUBLISHED || status == TemplateStatus.DISABLED) {
            template.setPublishedAt(template.getCreatedAt());
        }
        return templateRepository.save(template);
    }

    /**
     * Template demo yang masih di wizard Upload diberi ZIP sungguhan, agar bisa dibuka seperti upload asli:
     * draft siap di langkah 3, upload gagal bisa diperbaiki, dan upload "sedang dicek" benar-benar dicek
     * mesin pengecekan setelah seeder selesai.
     */
    private void attachDemoZips(Template failed, TemplateCheck failedCheck, Template checking, TemplateCheck checkingCheck,
                                Template draft) {
        byte[] failedZip = DemoSite.zip(failed.getName(), true);
        storage.writeSource(failed.getId(), failedZip);
        failed.setSourceFileName("landing-event.zip");
        failed.setSourceSize((long) failedZip.length);
        failed.setWizardStep(2);
        // Daftar masalah diambil dari mesin pengecekan sungguhan, agar sama dengan isi ZIP yang bisa diperiksa provider.
        TemplateChecker.Result result = checker.check(failedZip, "landing-event.zip", stage -> { });
        for (Finding f : result.findings()) {
            issueRepository.save(new TemplateCheckIssue(failedCheck.getId(), f.rule().severity(), f.rule().name(),
                    f.rule().version(), f.message(), f.file(), f.line(), f.suggestion(), f.snippet()));
        }
        failed.setWarningCount((int) result.warningCount());

        byte[] checkingZip = DemoSite.zip(checking.getName(), false);
        storage.writeSource(checking.getId(), checkingZip);
        checking.setSourceFileName("organisasi-pemuda.zip");
        checking.setSourceSize((long) checkingZip.length);
        checking.setWizardStep(2);
        checkingCheck.setStage(CheckStage.UPLOADED);
        events.publishEvent(new UploadCompletedEvent(checking.getId(), checkingCheck.getId()));

        byte[] draftZip = DemoSite.zip(draft.getName(), false);
        storage.writeSource(draft.getId(), draftZip);
        draft.setSourceFileName("instansi-desa.zip");
        draft.setSourceSize((long) draftZip.length);
        draft.setWizardStep(3);
        draft.setTechInfo(checker.check(draftZip, "instansi-desa.zip", stage -> { }).techInfo());
    }
}
