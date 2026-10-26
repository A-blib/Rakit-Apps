package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.organization.InvitationService;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.MemberService;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.organization.dto.InvitationCreatedResponse;
import com.aris.templateapp.organization.dto.InvitationResponse;
import com.aris.templateapp.organization.dto.MemberUpdateRequest;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pengurus & hak akses dari Portal. Mengubah peran dan mengeluarkan anggota memakai {@link MemberService} yang sama
 * dengan app: satu aturan (termasuk "Pemilik terakhir tidak boleh hilang"), dikunci saat diubah, dan tercatat di log
 * aktivitas. Dulu Portal menulis langsung ke repository dengan aturannya sendiri.
 */
@RestController
@RequiredArgsConstructor
public class PortalMemberController {

    private static final int POSITION_TITLE_MAX = 60;

    private final PortalAccess portal;
    private final MemberService memberService;
    private final InvitationService invitationService;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    @GetMapping("/api/public/sites/{slug}/admin/members")
    public List<Map<String, Object>> getSiteMembers(@PathVariable String slug,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireManager(authHeader, site);

        return memberRepository.findByOrganizationId(site.getOrganizationId())
                .stream()
                .map(m -> {
                    User u = userRepository.findById(m.getUserId()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", m.getUserId());
                    map.put("role", m.getRole().value());
                    map.put("positionTitle", m.getPositionTitle());
                    map.put("joinedAt", m.getJoinedAt());
                    map.put("name", u != null && u.getDisplayName() != null ? u.getDisplayName() : (u != null ? u.getEmail() : "Pengurus"));
                    map.put("email", u != null ? u.getEmail() : "-");
                    return map;
                })
                .toList();
    }

    /**
     * Menambah pengurus dari Portal lewat kode undangan, sama dengan app: penerima memasukkan kode di app OrgSpace
     * (Gabung organisasi) dan baru menjadi anggota setelah ia sendiri menerimanya. Dulu akun mana pun langsung
     * dimasukkan lewat email tanpa persetujuan, dan pesan "email belum punya akun" membocorkan email yang terdaftar.
     */
    @PostMapping("/api/public/sites/{slug}/admin/invitations")
    public InvitationCreatedResponse createInvitation(@PathVariable String slug,
                                                      @RequestBody Map<String, String> payload,
                                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        UUID userId = portal.requireManager(authHeader, site);
        MemberRole role = "admin".equalsIgnoreCase(payload.get("role")) ? MemberRole.ADMIN : MemberRole.EDITOR;
        return invitationService.create(site.getOrganizationId(), userId, role);
    }

    @GetMapping("/api/public/sites/{slug}/admin/invitations")
    public List<InvitationResponse> listInvitations(@PathVariable String slug,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        return invitationService.listActive(site.getOrganizationId(), portal.requireManager(authHeader, site));
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/invitations/{invitationId}")
    public Map<String, Object> revokeInvitation(@PathVariable String slug, @PathVariable UUID invitationId,
                                                @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        invitationService.revoke(site.getOrganizationId(), portal.requireManager(authHeader, site), invitationId);
        return Map.of("success", true, "message", "Undangan dicabut");
    }

    @PutMapping("/api/public/sites/{slug}/admin/members/{targetUserId}")
    public Map<String, Object> updateMemberRole(@PathVariable String slug,
                                                @PathVariable UUID targetUserId,
                                                @RequestBody Map<String, String> payload,
                                                @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        UUID actorId = portal.requireManager(authHeader, site);
        String positionTitle = payload.get("positionTitle");
        if (positionTitle != null && positionTitle.length() > POSITION_TITLE_MAX) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Jabatan maksimal " + POSITION_TITLE_MAX + " karakter");
        }
        memberService.update(site.getOrganizationId(), actorId, targetUserId,
                new MemberUpdateRequest(role(payload.get("role")), positionTitle));
        return Map.of("success", true, "message", "Peran pengurus berhasil diperbarui");
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/members/{targetUserId}")
    public Map<String, Object> removeMember(@PathVariable String slug,
                                            @PathVariable UUID targetUserId,
                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        memberService.remove(site.getOrganizationId(), portal.requireManager(authHeader, site), targetUserId);
        return Map.of("success", true, "message", "Akses pengurus berhasil dicabut");
    }

    /** Portal hanya bisa memberi peran Admin/Editor; tanpa isian peran = peran tidak diubah (dulu jatuh ke Editor). */
    private static MemberRole role(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("admin".equalsIgnoreCase(value)) {
            return MemberRole.ADMIN;
        }
        if ("editor".equalsIgnoreCase(value)) {
            return MemberRole.EDITOR;
        }
        throw new ApiException(ErrorCode.VALIDATION_ERROR, "Peran harus admin atau editor");
    }
}
