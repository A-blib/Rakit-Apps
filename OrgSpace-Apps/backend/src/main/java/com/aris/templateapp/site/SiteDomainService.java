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

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import com.google.common.net.InternetDomainName;
import java.net.IDN;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    private final SiteRepository siteRepository;
    private final SiteReleaseRepository siteReleaseRepository;
    private final SiteDomainRepository siteDomainRepository;
    private final OrganizationAccess access;
    private final SiteStorage siteStorage;
    private final AppProperties properties;
    private final Clock clock;

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
        domain.setLastError(null);
        domain.setFailingSince(null);
        domain = siteDomainRepository.save(domain);

        // Pasang symlink host jika website sudah memiliki rilis aktif
        setupHostSymlink(site, domain.getHostname());

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

        // Hapus symlink host jika ada
        removeHostSymlink(domain.getHostname());

        siteDomainRepository.delete(domain);
        log.info("Domain kustom {} untuk website {} telah dilepas", domain.getHostname(), site.getSlug());
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
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "2500");
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            InitialDirContext ctx = new InitialDirContext(env);
            String txtHost = properties.sites().verifyTxtPrefix() + "." + hostname;
            Attributes attrs = ctx.getAttributes(txtHost, new String[]{"TXT"});
            Attribute txtAttr = attrs.get("TXT");
            if (txtAttr != null) {
                for (int i = 0; i < txtAttr.size(); i++) {
                    String val = txtAttr.get(i).toString().replace("\"", "").trim();
                    if (expectedToken.equalsIgnoreCase(val)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Pemeriksaan DNS TXT untuk {} gagal: {}", hostname, e.getMessage());
        }
        return false;
    }

    private void setupHostSymlink(Site site, String hostname) {
        if (site.getCurrentReleaseId() == null) {
            return;
        }
        siteReleaseRepository.findById(site.getCurrentReleaseId()).ifPresent(release -> {
            Path hostsDir = Path.of(properties.sites().storageRootDir()).resolve("hosts");
            try {
                Files.createDirectories(hostsDir);
                Path symlink = hostsDir.resolve(hostname);
                Path target = siteStorage.releasePath(site.getId(), release.getVersion());
                Files.deleteIfExists(symlink);
                try {
                    Files.createSymbolicLink(symlink, target);
                } catch (UnsupportedOperationException | IOException ignored) {
                }
            } catch (IOException ignored) {
            }
        });
    }

    private void removeHostSymlink(String hostname) {
        Path hostsDir = Path.of(properties.sites().storageRootDir()).resolve("hosts");
        Path symlink = hostsDir.resolve(hostname);
        try {
            Files.deleteIfExists(symlink);
        } catch (IOException ignored) {
        }
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
