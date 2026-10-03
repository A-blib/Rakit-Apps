package com.aris.templateapp.seed;

import com.aris.templateapp.auth.UserIdentity;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.provider.ProviderProfile;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.provider.ProviderStatus;
import com.aris.templateapp.user.ActiveMode;
import com.aris.templateapp.user.CreatorProfile;
import com.aris.templateapp.user.CreatorProfileRepository;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import com.aris.templateapp.user.WebsitePurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Random;

/**
 * Mengisi 10 user dummy saat backend start dengan profile {@code dev}, HANYA jika tabel users masih kosong.
 * Semua user memakai password yang sama ({@link #PASSWORD}, juga tertulis di README) supaya mudah dicoba.
 * <p>
 * Datafaker belum punya data locale Indonesia, jadi nama diambil acak (lewat Datafaker) dari daftar nama
 * Indonesia di bawah. Seed acak tetap (42) membuat hasilnya sama setiap kali database dikosongkan lalu diisi ulang.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DummyDataSeeder implements ApplicationRunner {

    public static final String PASSWORD = "password123";
    public static final int USER_COUNT = 10;
    static final String EMAIL_DOMAIN = "@templateapp.test";

    private static final String[] FIRST_NAMES = {
            "Budi", "Siti", "Agus", "Dewi", "Rizky", "Putri", "Fajar", "Nur", "Andi", "Rina", "Hendra", "Wulan"};
    private static final String[] LAST_NAMES = {
            "Santoso", "Rahmawati", "Pratama", "Lestari", "Hidayat", "Saputra", "Wijaya", "Nugroho", "Kurniawan"};
    private static final String[] ORGANIZATIONS = {
            "SMK Negeri 1 Malang", "Karang Taruna Melati", "Warung Kopi Senja", "Dinas Pariwisata", "Toko Batik Ayu"};
    private static final String[] SPECIALTIES = {"Landing page", "Sekolah", "UMKM", "Portofolio", "Organisasi"};

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Seeder dilewati: tabel users sudah berisi data");
            return;
        }

        Faker faker = new Faker(new Random(42));
        // BCrypt sengaja lambat, jadi hash dibuat sekali lalu dipakai bersama oleh semua user dummy.
        String passwordHash = passwordEncoder.encode(PASSWORD);
        WebsitePurpose[] purposes = WebsitePurpose.values();
        // Tiga user terakhir menjadi provider dengan status berbeda.
        List<ProviderStatus> providerStatuses = List.of(
                ProviderStatus.PENDING, ProviderStatus.APPROVED, ProviderStatus.REJECTED);

        for (int i = 1; i <= USER_COUNT; i++) {
            String name = faker.options().option(FIRST_NAMES) + " " + faker.options().option(LAST_NAMES);
            String email = "dummy" + i + EMAIL_DOMAIN;

            User user = userRepository.save(new User(name, email));
            identityRepository.save(UserIdentity.local(user, email, passwordHash));

            // User 1–2 sengaja belum onboarding, untuk mencoba alur onboarding.
            if (i <= 2) {
                continue;
            }
            CreatorProfile creator = new CreatorProfile(user.getId());
            creator.setWebsitePurpose(faker.options().option(purposes));
            creator.setOrganizationName(faker.bool().bool() ? faker.options().option(ORGANIZATIONS) : null);
            creatorProfileRepository.save(creator);
            user.setOnboardingCompleted(true);

            int providerIndex = i - (USER_COUNT - providerStatuses.size()) - 1;
            if (providerIndex >= 0) {
                ProviderStatus status = providerStatuses.get(providerIndex);
                ProviderProfile provider = new ProviderProfile(user.getId(), name, clock.instant());
                provider.setBio("Pembuat template website untuk " + faker.options().option(SPECIALTIES).toLowerCase() + ".");
                provider.setSpecialties(List.of(faker.options().option(SPECIALTIES)));
                provider.setStatus(status);
                if (status == ProviderStatus.REJECTED) {
                    provider.setRejectionReason("Contoh template belum memenuhi standar.");
                }
                providerProfileRepository.save(provider);
                user.setActiveMode(ActiveMode.PROVIDER);
            }
        }
        log.info("Seeder selesai: {} user dummy dibuat (email dummy1..{}{}, password di README)",
                USER_COUNT, USER_COUNT, EMAIL_DOMAIN);
    }
}
