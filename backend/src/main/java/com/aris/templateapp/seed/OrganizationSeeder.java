package com.aris.templateapp.seed;

import com.aris.templateapp.organization.ActivityService;
import com.aris.templateapp.organization.InvitationCodes;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.organization.OrgLevel;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationInvitation;
import com.aris.templateapp.organization.OrganizationInvitationRepository;
import com.aris.templateapp.organization.OrganizationMember;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.organization.OrganizationRepository;
import com.aris.templateapp.organization.OrganizationType;
import com.aris.templateapp.security.TokenGenerator;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Data ruang kerja dummy (revisi-fondasi-orgspace.md 4.3), HANYA jika tabel organizations masih kosong:
 * 3 organisasi dengan anggota berperan campuran, 2 ruang pribadi, 2 undangan aktif + 1 kedaluwarsa.
 * Memakai user dummy dari {@link DummyDataSeeder}, jadi database lama (users sudah terisi) tetap ikut diisi.
 * Kode undangan sengaja tetap agar bisa ditulis di README.
 */
@Slf4j
@Component
@Profile("dev")
@Order(3)
@RequiredArgsConstructor
public class OrganizationSeeder implements ApplicationRunner {

    public static final String OSIS_INVITE_CODE = "OSIS-7K2P";
    public static final String BEM_INVITE_CODE = "BEM4-HMJ8";
    public static final String EXPIRED_INVITE_CODE = "PRAM-KA26";

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationInvitationRepository invitationRepository;
    private final ActivityService activityService;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (organizationRepository.count() > 0) {
            log.info("Seeder organisasi dilewati: tabel organizations sudah berisi data");
            return;
        }
        if (userRepository.findByEmailIgnoreCase(dummy(9)).isEmpty()) {
            log.info("Seeder organisasi dilewati: user dummy belum ada");
            return;
        }
        Instant now = clock.instant();

        Organization osis = organization("OSIS SMA Negeri 1 Bandung", "OSIS Smansa", OrgCategory.OSIS_MPK, OrgLevel.SMA,
                "SMA Negeri 1 Bandung", "Bandung", 3);
        osis.setInstagram("osis.smansa.bdg");
        osis.setWhatsapp("6281234567890");
        osis.setEmail("osis@sman1bdg.sch.id");
        osis.setAddress("Jl. Ir. H. Juanda No. 93, Bandung");
        member(osis, 3, MemberRole.OWNER, "Ketua", now);
        member(osis, 4, MemberRole.ADMIN, "Sekretaris", now);
        member(osis, 5, MemberRole.EDITOR, "Humas", now);
        member(osis, 6, MemberRole.EDITOR, null, now);

        Organization bem = organization("BEM Universitas Nusantara", "BEM UNU", OrgCategory.BEM_DEMA_DPM,
                OrgLevel.PERGURUAN_TINGGI, "Universitas Nusantara", "Yogyakarta", 7);
        bem.setInstagram("bem.unu");
        bem.setYoutube("@bemunu");
        member(bem, 7, MemberRole.OWNER, "Presiden Mahasiswa", now);
        member(bem, 8, MemberRole.ADMIN, "Menteri Kominfo", now);
        member(bem, 4, MemberRole.EDITOR, "Staf Media", now);

        Organization pramuka = organization("Pramuka SMP Negeri 2 Malang", null, OrgCategory.PRAMUKA, OrgLevel.SMP,
                "SMP Negeri 2 Malang", "Malang", 9);
        member(pramuka, 9, MemberRole.OWNER, "Pradana", now);
        member(pramuka, 5, MemberRole.ADMIN, null, now);
        member(pramuka, 3, MemberRole.EDITOR, "Dokumentasi", now);

        personal(6, now);
        personal(7, now);

        invitation(osis, OSIS_INVITE_CODE, MemberRole.EDITOR, 3, now.plus(Duration.ofDays(7)));
        invitation(bem, BEM_INVITE_CODE, MemberRole.ADMIN, 7, now.plus(Duration.ofDays(7)));
        invitation(pramuka, EXPIRED_INVITE_CODE, MemberRole.EDITOR, 9, now.minus(Duration.ofDays(1)));

        activityService.log(osis.getId(), user(4).getId(), "org.updated");
        activityService.log(osis.getId(), user(3).getId(), "member.role_changed", "user", user(4).getId(),
                Map.of("memberName", user(4).getDisplayName(), "from", "editor", "to", "admin"));

        // User yang belum punya ruang pribadi membuka organisasi pertama yang ia ikuti.
        for (int i : List.of(3, 4, 5, 8, 9)) {
            User user = user(i);
            if (user.getLastOrganizationId() == null) {
                user.setLastOrganizationId(memberRepository.findAll().stream()
                        .filter(m -> m.getUserId().equals(user.getId())).findFirst().orElseThrow().getOrganizationId());
            }
        }
        log.info("Seeder organisasi selesai: 3 organisasi, 2 ruang pribadi, kode undangan {} dan {}",
                OSIS_INVITE_CODE, BEM_INVITE_CODE);
    }

    private Organization organization(String name, String shortName, OrgCategory category, OrgLevel level,
                                      String institution, String city, int ownerIndex) {
        Organization organization = new Organization(OrganizationType.ORGANIZATION, name, user(ownerIndex).getId());
        organization.setShortName(shortName);
        organization.setCategory(category);
        organization.setLevel(level);
        organization.setInstitutionName(institution);
        organization.setCity(city);
        organization.setPeriodStartYear((short) 2026);
        organization.setPeriodEndYear((short) 2027);
        organizationRepository.save(organization);
        activityService.log(organization.getId(), user(ownerIndex).getId(), "org.created");
        return organization;
    }

    private void member(Organization organization, int userIndex, MemberRole role, String position, Instant now) {
        User user = user(userIndex);
        memberRepository.save(new OrganizationMember(organization.getId(), user.getId(), role, position, now));
        if (role != MemberRole.OWNER) {
            activityService.log(organization.getId(), user.getId(), "member.joined", null, null,
                    Map.of("role", role.value()));
        }
    }

    private void personal(int userIndex, Instant now) {
        User user = user(userIndex);
        Organization space = new Organization(OrganizationType.PERSONAL, "Ruang pribadi " + user.getDisplayName(), user.getId());
        organizationRepository.save(space);
        memberRepository.save(new OrganizationMember(space.getId(), user.getId(), MemberRole.OWNER, null, now));
        user.setLastOrganizationId(space.getId());
    }

    private void invitation(Organization organization, String code, MemberRole role, int creatorIndex, Instant expiresAt) {
        invitationRepository.save(new OrganizationInvitation(organization.getId(),
                tokenGenerator.hash(InvitationCodes.normalize(code)), role, user(creatorIndex).getId(), expiresAt));
    }

    private User user(int index) {
        return userRepository.findByEmailIgnoreCase(dummy(index)).orElseThrow();
    }

    private static String dummy(int index) {
        return "dummy" + index + DummyDataSeeder.EMAIL_DOMAIN;
    }
}
