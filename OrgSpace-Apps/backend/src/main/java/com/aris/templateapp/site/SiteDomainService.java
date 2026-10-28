package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.organization.OrganizationAccess;
import com.aris.templateapp.site.dto.AddDomainRequest;
import com.aris.templateapp.site.dto.DomainInstructionDto;
import com.aris.templateapp.site.dto.DomainResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.net.InternetDomainName;
import java.net.IDN;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Layanan manajemen custom domain website organisasi (alur-publish-website.md 7.10).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteDomainService {

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)+$");
    private static final Pattern IP_PATTERN = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
    private static final SecureRandom RANDOM = new SecureRandom();
    /** Record TXT hilang selama ini → domain dilepas (mis. domain kedaluwarsa lalu dibeli orang lain). */
    static final Duration RELEASE_AFTER = Duration.ofDays(7);

    private final SiteRepository siteRepository;
    private final SiteDomainRepository siteDomainRepository;
    private final OrganizationAccess access;
    private final AppProperties properties;
    private final Clock clock;
    private final DnsLookup dns;
    private final VercelDomainClient vercel;

    private Site findSite(UUID id) {
        return siteRepository.findById(id)
                .or(() -> siteRepository.findByProjectId(id))
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED,
                        "Website belum diterbitkan. Terbitkan website terlebih dahulu sebelum mengatur domain sendiri."));
    }

    @Transactional(readOnly = true)
    public Optional<DomainResponse> getDomain(UUID siteOrProjectId, UUID userId) {
        Site site = findSite(siteOrProjectId);
        access.require(site.getOrganizationId(), userId, OrgAction.VIEW);

        return siteDomainRepository.findFirstBySiteIdAndStatusNot(site.getId(), "removed")
                .map(this::toResponse);
    }

    @Transactional
    public DomainResponse addDomain(UUID siteOrProjectId, UUID userId, AddDomainRequest request) {
        Site site = findSite(siteOrProjectId);
        checkAdmin(site.getOrganizationId(), userId);

        String cleanHost = normalizeHostname(request.hostname());

        // 1. Hanya domain AKTIF di website lain yang menghalangi. Klaim pending orang lain belum terbukti
        //    (belum lolos cek DNS), jadi tidak boleh membuat pemilik asli tidak bisa mendaftar.
        requireNotActiveElsewhere(cleanHost, site.getId());

        // 2. Cek apakah website ini sudah punya domain aktif/pending
        Optional<SiteDomain> existing = siteDomainRepository.findFirstBySiteIdAndStatusNot(site.getId(), "removed");
        if (existing.isPresent()) {
            SiteDomain current = existing.get();
            if ("active".equalsIgnoreCase(current.getStatus())) {
                throw new ApiException(ErrorCode.DOMAIN_LIMIT_REACHED,
                        "Website sudah memiliki domain aktif (" + current.getHostname() + "). Lepas domain lama terlebih dahulu.");
            } else {
                // Jika masih pending, hapus yang lama dan ganti dengan yang baru
                siteDomainRepository.delete(current);
            }
        }

        // 3. Buat verification token acak
        String verificationToken = generateVerificationToken();

        SiteDomain domain = new SiteDomain(site.getId(), cleanHost, verificationToken);
        domain = siteDomainRepository.save(domain);

        log.info("Domain kustom {} ditambahkan untuk website {} (status pending)", cleanHost, site.getSlug());
        return toResponse(domain);
    }

    @Transactional
    public DomainResponse verifyDomain(UUID siteOrProjectId, UUID domainId, UUID userId, boolean bypassDns) {
        Site site = findSite(siteOrProjectId);
        checkAdmin(site.getOrganizationId(), userId);

        SiteDomain domain = siteDomainRepository.findById(domainId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOMAIN_NOT_FOUND, "Domain tidak ditemukan"));

        if (!domain.getSiteId().equals(site.getId())) {
            throw new ApiException(ErrorCode.DOMAIN_NOT_FOUND);
        }

        domain.setLastCheckedAt(clock.instant());

        // Lewati cek DNS hanya jika server mengizinkan (profile dev). Di server asli parameter ini diabaikan,
        // agar tidak ada yang bisa mengaktifkan domain milik orang lain tanpa bukti kepemilikan.
        boolean verified = (bypassDns && properties.sites().allowDnsBypass())
                || checkDnsTxt(domain.getHostname(), domain.getVerificationToken());

        if (!verified) {
            domain.setLastError("Record TXT verifikasi belum terdeteksi di DNS. Tunggu beberapa menit agar propagasi DNS selesai.");
            siteDomainRepository.save(domain);
            throw new ApiException(ErrorCode.DOMAIN_DNS_NOT_READY,
                    "Record TXT verifikasi belum terdeteksi di DNS. Pengaturan DNS biasanya butuh beberapa menit sampai beberapa jam. Coba periksa lagi nanti, ya.");
        }

        requireNotActiveElsewhere(domain.getHostname(), site.getId());
        domain.setStatus("active");
        domain.setActivatedAt(clock.instant());
        // Kepemilikan sudah terbukti (TXT); jika A/CNAME belum mengarah ke OrgSpace, domain tetap aktif tapi diberi
        // peringatan, agar pengguna tidak mengira domainnya sudah bisa dibuka.
        domain.setLastError(bypassDns && properties.sites().allowDnsBypass() ? null : pointingProblem(domain.getHostname()));
        domain.setFailingSince(null);
        // Hosting di Vercel hanya melayani domain yang terdaftar di project-nya. Gagal mendaftar = verifikasi dibatalkan
        // (transaksi digulung balik, domain tetap pending), agar status "aktif" tidak menyesatkan.
        String providerNote = vercel.addDomain(domain.getHostname());
        String alternate = alternateHost(domain.getHostname());
        if (alternate != null) {
            try {
                vercel.addDomain(alternate);
            } catch (ApiException e) {
                log.warn("Pasangan www {} gagal didaftarkan: {}", alternate, e.getMessage());
            }
        }
        if (providerNote != null) {
            domain.setLastError(providerNote);
        }
        domain = siteDomainRepository.save(domain);

        log.info("Domain kustom {} untuk website {} BERHASIL diverifikasi dan aktif!", domain.getHostname(), site.getSlug());
        return toResponse(domain);
    }

    @Transactional
    public void removeDomain(UUID siteOrProjectId, UUID domainId, UUID userId) {
        Site site = findSite(siteOrProjectId);
        checkAdmin(site.getOrganizationId(), userId);

        SiteDomain domain = siteDomainRepository.findById(domainId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOMAIN_NOT_FOUND));

        if (!domain.getSiteId().equals(site.getId())) {
            throw new ApiException(ErrorCode.DOMAIN_NOT_FOUND);
        }

        siteDomainRepository.delete(domain);
        if ("active".equals(domain.getStatus())) {
            releaseFromHosting(domain.getHostname());
        }
        log.info("Domain kustom {} untuk website {} telah dilepas", domain.getHostname(), site.getSlug());
    }

    private void releaseFromHosting(String hostname) {
        vercel.removeDomain(hostname);
        String alternate = alternateHost(hostname);
        if (alternate != null) {
            vercel.removeDomain(alternate);
        }
    }

    /** Pasangan www / tanpa www untuk domain utama dan www.domain-utama (sama dengan instruksi DNS opsional). */
    static String alternateHost(String hostname) {
        String zone = InternetDomainName.from(hostname).topPrivateDomain().toString();
        if (hostname.equals(zone)) {
            return "www." + zone;
        }
        return hostname.equals("www." + zone) ? zone : null;
    }

    private void requireNotActiveElsewhere(String hostname, UUID siteId) {
        siteDomainRepository.findByHostnameAndStatus(hostname, "active")
                .filter(active -> !active.getSiteId().equals(siteId))
                .ifPresent(active -> {
                    throw new ApiException(ErrorCode.DOMAIN_TAKEN, "Domain '" + hostname + "' sudah aktif di website lain.");
                });
    }

    private void checkAdmin(UUID orgId, UUID userId) {
        OrganizationAccess.Membership membership = access.require(orgId, userId, OrgAction.VIEW);
        if (membership.role() != MemberRole.OWNER && membership.role() != MemberRole.ADMIN) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Hanya Admin dan Pemilik yang dapat mengatur domain sendiri.");
        }
    }

    public String normalizeHostname(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Nama domain tidak boleh kosong.");
        }
        String clean = raw.trim().toLowerCase(Locale.ROOT);
        clean = clean.replaceFirst("^https?://", "");
        clean = clean.split("[/?#:]")[0]; // buang path, query, port
        clean = clean.replaceAll("/+$", "");

        try {
            clean = IDN.toASCII(clean);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Format nama domain tidak valid.");
        }

        if (clean.length() < 3 || clean.length() > 253) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Panjang domain harus antara 3 hingga 253 karakter.");
        }

        if (clean.equals("localhost") || clean.endsWith(".localhost")) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Domain localhost tidak diizinkan.");
        }

        if (!clean.contains(".")) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Domain harus memiliki ekstensi (mis. .id, .com, .my.id).");
        }

        if (IP_PATTERN.matcher(clean).matches()) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Alamat IP tidak dapat dijadikan domain.");
        }

        if (!HOSTNAME_PATTERN.matcher(clean).matches()) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Nama domain mengandung karakter yang tidak diizinkan.");
        }

        // Domain harus berada di bawah akhiran publik (.id, .sch.id, .my.id, .com, ...), bukan akhirannya sendiri.
        if (!InternetDomainName.isValid(clean) || !InternetDomainName.from(clean).isUnderPublicSuffix()) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Masukkan domain lengkap, mis. osis-smansa.sch.id atau www.osis.id.");
        }

        String sysDomain = properties.sites().domain().toLowerCase(Locale.ROOT).split(":")[0];
        if (clean.endsWith("." + sysDomain) || clean.equals(sysDomain)
                || clean.endsWith(".orgspace.app") || clean.equals("orgspace.app")
                || clean.endsWith(".orgspace.site") || clean.equals("orgspace.site")
                || clean.endsWith(".orgspace.id") || clean.equals("orgspace.id")) {
            throw new ApiException(ErrorCode.DOMAIN_INVALID, "Domain tidak boleh menggunakan domain internal OrgSpace.");
        }

        return clean;
    }

    private String generateVerificationToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean checkDnsTxt(String hostname, String expectedToken) {
        return dns.txt(properties.sites().verifyTxtPrefix() + "." + hostname).stream()
                .anyMatch(expectedToken::equalsIgnoreCase);
    }

    /** Pesan jika domain belum mengarah ke server OrgSpace (IP publik atau tujuan CNAME), atau null jika sudah. */
    String pointingProblem(String hostname) {
        Set<String> actual = dns.addresses(hostname);
        if (actual.isEmpty()) {
            return "Domain belum mengarah ke server OrgSpace. Pasang record A/CNAME sesuai petunjuk, lalu tunggu "
                    + "propagasi DNS (bisa sampai beberapa jam).";
        }
        Set<String> ours = new HashSet<>(dns.addresses(properties.sites().customTarget()));
        ours.add(properties.sites().publicIp());
        if (actual.stream().noneMatch(ours::contains)) {
            return "Domain masih mengarah ke " + String.join(", ", actual) + ", belum ke server OrgSpace ("
                    + properties.sites().publicIp() + "). Periksa record A/CNAME di pengaturan DNS.";
        }
        return null;
    }

    /**
     * Pengecekan berkala domain aktif ({@link SiteDomainMonitor}). Record TXT yang hilang lebih dari
     * {@link #RELEASE_AFTER} membuat domain dilepas (kembali pending), agar domain yang sudah berpindah pemilik tidak
     * terkunci di website lama. DNS yang tidak mengarah ke OrgSpace hanya dicatat sebagai peringatan.
     */
    @Transactional
    public void recheck(UUID domainId) {
        SiteDomain domain = siteDomainRepository.findById(domainId).orElse(null);
        if (domain == null || !"active".equals(domain.getStatus())) {
            return;
        }
        Instant now = clock.instant();
        domain.setLastCheckedAt(now);
        if (checkDnsTxt(domain.getHostname(), domain.getVerificationToken())) {
            domain.setFailingSince(null);
            domain.setLastError(pointingProblem(domain.getHostname()));
        } else {
            if (domain.getFailingSince() == null) {
                domain.setFailingSince(now);
            }
            Instant releaseAt = domain.getFailingSince().plus(RELEASE_AFTER);
            if (!now.isBefore(releaseAt)) {
                domain.setStatus("pending");
                domain.setActivatedAt(null);
                releaseFromHosting(domain.getHostname());
                domain.setLastError("Domain dilepas otomatis karena record TXT verifikasi hilang lebih dari "
                        + RELEASE_AFTER.toDays() + " hari. Pasang lagi record TXT, lalu tekan Verifikasi.");
                log.warn("Domain kustom {} dilepas: record TXT hilang sejak {}", domain.getHostname(), domain.getFailingSince());
            } else {
                domain.setLastError("Record TXT verifikasi tidak lagi ditemukan. Pasang lagi sebelum "
                        + DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.of("id", "ID"))
                        .format(releaseAt.atZone(ZoneId.of(properties.timezone())))
                        + ", atau domain akan dilepas dari website ini.");
            }
        }
        siteDomainRepository.save(domain);
    }

    public DomainResponse toResponse(SiteDomain domain) {
        String cnameTarget = properties.sites().customTarget();
        String aTarget = properties.sites().publicIp();
        String verifyPrefix = properties.sites().verifyTxtPrefix();

        // Nama host ditulis relatif terhadap domain yang dibeli user (zona DNS di panel registrar), mis. untuk
        // web.osis.sch.id zonanya osis.sch.id → host "web". Akhiran publik (.sch.id, .my.id) dikenali Guava,
        // jadi osis.sch.id dianggap domain utama, bukan subdomain.
        String zone = InternetDomainName.from(domain.getHostname()).topPrivateDomain().toString();
        String sub = domain.getHostname().equals(zone) ? null
                : domain.getHostname().substring(0, domain.getHostname().length() - zone.length() - 1);

        List<DomainInstructionDto> instructions = new ArrayList<>();
        if (sub != null) {
            instructions.add(new DomainInstructionDto("CNAME", sub, cnameTarget,
                    "Di pengaturan DNS " + zone + ", arahkan subdomain ke server OrgSpace"));
        } else {
            instructions.add(new DomainInstructionDto("A", "@", aTarget,
                    "Di pengaturan DNS " + zone + ", arahkan domain utama ke IP server OrgSpace"));
        }
        instructions.add(new DomainInstructionDto("TXT", sub == null ? verifyPrefix : verifyPrefix + "." + sub,
                domain.getVerificationToken(), "Wajib: bukti bahwa domain ini milikmu"));
        // osis.id dan www.osis.id dilayani sebagai satu website; record pasangannya opsional.
        if (sub == null) {
            instructions.add(new DomainInstructionDto("CNAME", "www", cnameTarget,
                    "Opsional: agar www." + zone + " juga membuka website ini"));
        } else if ("www".equals(sub)) {
            instructions.add(new DomainInstructionDto("A", "@", aTarget,
                    "Opsional: agar " + zone + " (tanpa www) juga membuka website ini"));
        }

        String slug = siteRepository.findById(domain.getSiteId()).map(Site::getSlug).orElse(null);
        return new DomainResponse(
                domain.getId(),
                domain.getSiteId(),
                domain.getHostname(),
                domain.getStatus(),
                domain.getVerificationToken(),
                cnameTarget,
                aTarget,
                domain.getActivatedAt(),
                instructions,
                domain.getLastError(),
                slug == null ? null : properties.sites().urlOf(slug)
        );
    }
}
