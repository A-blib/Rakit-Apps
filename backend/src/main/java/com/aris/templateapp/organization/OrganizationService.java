package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.dto.CreateOrganizationRequest;
import com.aris.templateapp.organization.dto.OrganizationResponse;
import com.aris.templateapp.organization.dto.OrganizationSummaryResponse;
import com.aris.templateapp.organization.dto.UpdateOrganizationRequest;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

/** Membuat, membaca, mengubah, dan menghapus ruang kerja (revisi-fondasi-orgspace.md bagian 4.4). */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final OrganizationAccess access;
    private final ActivityService activityService;
    private final OrgLogoStorage logoStorage;
    private final AppProperties properties;
    private final Clock clock;

    /** Pembuat langsung menjadi Pemilik, dan ruang kerja baru menjadi ruang kerja terakhir yang dibuka. */
    @Transactional
    public OrganizationResponse create(UUID userId, CreateOrganizationRequest request) {
        // Baris user dikunci agar dua request "buat ruang pribadi" bersamaan tidak sama-sama lolos.
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        Organization organization = new Organization(request.type(), request.name().trim(), userId);
        organization.setShortName(blankToNull(request.shortName()));

        if (organization.isPersonal()) {
            if (organizationRepository.existsByCreatedByAndType(userId, OrganizationType.PERSONAL)) {
                throw new ApiException(ErrorCode.PERSONAL_SPACE_EXISTS);
            }
        } else {
            applyOrgFields(organization, request.category(), request.level(), request.institutionName());
        }
        organization.setCity(blankToNull(request.city()));
        applyPeriod(organization, request.periodStartYear(), request.periodEndYear());
        organizationRepository.save(organization);

        memberRepository.save(new OrganizationMember(organization.getId(), userId, MemberRole.OWNER,
                organization.isPersonal() ? null : blankToNull(request.positionTitle()), clock.instant()));
        user.setLastOrganizationId(organization.getId());
        activityService.log(organization.getId(), userId, "org.created");
        return toResponse(organization, MemberRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<OrganizationSummaryResponse> list(UUID userId) {
        return organizationRepository.findAllForUser(userId).stream()
                .map(row -> new OrganizationSummaryResponse(row.getOrganization().getId(), row.getOrganization().getType(),
                        row.getOrganization().getName(), row.getOrganization().getShortName(),
                        row.getOrganization().getLogoUrl(), row.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse get(UUID organizationId, UUID userId) {
        OrganizationAccess.Membership membership = access.require(organizationId, userId, OrgAction.VIEW);
        return toResponse(membership.organization(), membership.role());
    }

    @Transactional
    public OrganizationResponse update(UUID organizationId, UUID userId, UpdateOrganizationRequest request) {
        OrganizationAccess.Membership membership = access.require(organizationId, userId, OrgAction.EDIT_PROFILE);
        Organization organization = membership.organization();
        organization.setName(request.name().trim());
        organization.setShortName(blankToNull(request.shortName()));
        if (!organization.isPersonal()) {
            applyOrgFields(organization, request.category(), request.level(), request.institutionName());
        }
        organization.setCity(blankToNull(request.city()));
        applyPeriod(organization, request.periodStartYear(), request.periodEndYear());
        organization.setAddress(blankToNull(request.address()));
        String email = blankToNull(request.email());
        organization.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));
        organization.setWhatsapp(social(request.whatsapp(), SocialLinks::whatsapp, "Nomor WhatsApp tidak valid."));
        organization.setInstagram(social(request.instagram(), SocialLinks::instagram, "Username Instagram tidak valid."));
        organization.setTiktok(social(request.tiktok(), SocialLinks::tiktok, "Username TikTok tidak valid."));
        organization.setYoutube(social(request.youtube(), SocialLinks::youtube, "Link YouTube tidak valid."));
        activityService.log(organizationId, userId, "org.updated");
        return toResponse(organization, membership.role());
    }

    @Transactional
    public OrganizationResponse uploadLogo(UUID organizationId, UUID userId, byte[] bytes) {
        OrganizationAccess.Membership membership = access.require(organizationId, userId, OrgAction.EDIT_PROFILE);
        logoStorage.save(organizationId, bytes);
        // ?v= berubah setiap logo diganti, sehingga cache gambar di HP tidak menampilkan logo lama.
        membership.organization().setLogoUrl("/api/organizations/" + organizationId + "/logo?v=" + clock.millis());
        activityService.log(organizationId, userId, "org.logo_changed");
        return toResponse(membership.organization(), membership.role());
    }

    /** Logo boleh dilihat siapa saja (tampil di pratinjau undangan dan nanti di website organisasi). */
    @Transactional(readOnly = true)
    public Path logoFile(UUID organizationId) {
        Path file = organizationRepository.existsById(organizationId) ? logoStorage.file(organizationId) : null;
        if (file == null) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        return file;
    }

    /**
     * Hapus permanen (F5). Penghapusan website online ({@code ORG_HAS_ONLINE_SITES}) ditambahkan bersama fitur
     * Terbitkan, karena tabel website belum ada.
     */
    @Transactional
    public void delete(UUID organizationId, UUID userId, String confirmName) {
        OrganizationAccess.Membership membership = access.require(organizationId, userId, OrgAction.DELETE_ORGANIZATION);
        if (!membership.organization().getName().trim().equalsIgnoreCase(confirmName.trim())) {
            throw new ApiException(ErrorCode.CONFIRM_NAME_MISMATCH);
        }
        // Anggota, undangan, dan riwayat ikut terhapus lewat ON DELETE CASCADE; last_organization_id menjadi null.
        organizationRepository.delete(membership.organization());
        logoStorage.delete(organizationId);
    }

    @Transactional
    public void setLastOrganization(UUID userId, UUID organizationId) {
        access.require(organizationId, userId, OrgAction.VIEW);
        userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED))
                .setLastOrganizationId(organizationId);
    }

    OrganizationResponse toResponse(Organization o, MemberRole myRole) {
        return new OrganizationResponse(o.getId(), o.getType(), o.getName(), o.getShortName(), o.getCategory(),
                o.getLevel(), o.getInstitutionName(), o.getCity(), toInt(o.getPeriodStartYear()),
                toInt(o.getPeriodEndYear()), o.getLogoUrl(), o.getAddress(), o.getEmail(), o.getWhatsapp(),
                o.getInstagram(), o.getTiktok(), o.getYoutube(), myRole,
                memberRepository.countByOrganizationId(o.getId()), properties.org().maxMembers(), o.getCreatedAt());
    }

    /** Kategori, jenjang, dan instansi wajib untuk organisasi, dan kategorinya harus cocok dengan jenjang. */
    private static void applyOrgFields(Organization organization, OrgCategory category, OrgLevel level, String institution) {
        if (level == null) {
            throw validation("level", "Pilih jenjang.");
        }
        if (category == null) {
            throw validation("category", "Pilih kategori.");
        }
        if (blankToNull(institution) == null) {
            throw validation("institutionName", "Nama instansi wajib diisi.");
        }
        if (!category.allows(level)) {
            throw new ApiException(ErrorCode.CATEGORY_LEVEL_MISMATCH);
        }
        organization.setCategory(category);
        organization.setLevel(level);
        organization.setInstitutionName(institution.trim());
    }

    private static void applyPeriod(Organization organization, Integer start, Integer end) {
        if (start != null && end != null && end < start) {
            throw validation("periodEndYear", "Tahun selesai tidak boleh sebelum tahun mulai.");
        }
        organization.setPeriodStartYear(start == null ? null : start.shortValue());
        organization.setPeriodEndYear(end == null ? null : end.shortValue());
    }

    private static String social(String input, UnaryOperator<String> normalizer, String error) {
        String value = blankToNull(input);
        if (value == null) {
            return null;
        }
        String normalized = normalizer.apply(value);
        if (normalized == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, error);
        }
        return normalized;
    }

    private static ApiException validation(String field, String message) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, message, Map.of(field, message));
    }

    private static Integer toInt(Short value) {
        return value == null ? null : value.intValue();
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
