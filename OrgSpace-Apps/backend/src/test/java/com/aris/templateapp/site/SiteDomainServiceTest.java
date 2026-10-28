package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationAccess;
import com.aris.templateapp.organization.OrganizationMember;
import com.aris.templateapp.organization.OrganizationType;
import com.aris.templateapp.site.dto.AddDomainRequest;
import com.aris.templateapp.site.dto.DomainResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteDomainServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SiteDomainRepository siteDomainRepository;

    @Mock
    private OrganizationAccess access;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private DnsLookup dns;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private VercelDomainClient vercel;

    private AppProperties properties;
    private Clock clock;
    private SiteDomainService service;

    private final UUID siteId = UUID.randomUUID();
    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private Site site;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-17T12:00:00Z"), ZoneOffset.UTC);
        service = serviceWith(false);

        site = new Site(UUID.randomUUID(), orgId, "osis-smansa");
        site.setId(siteId);
    }

    private SiteDomainService serviceWith(boolean allowDnsBypass) {
        AppProperties.Sites sitesProp = new AppProperties.Sites(
                "orgspace.site",
                "https",
                5,
                5,
                "./data/sites",
                120,
                50,
                500,
                3,
                "custom.orgspace.site",
                "103.175.216.42",
                "_orgspace-verify",
                "https://orgspace.app",
                allowDnsBypass
        );
        properties = new AppProperties(null, null, null, "templateapp", "Asia/Jakarta", null, null, null, sitesProp, null, null);
        return new SiteDomainService(siteRepository, siteDomainRepository, access, properties, clock, dns, vercel);
    }

    private void asOwner() {
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        Organization org = new Organization(OrganizationType.ORGANIZATION, "OSIS Smansa", userId);
        OrganizationMember member = new OrganizationMember(orgId, userId, MemberRole.OWNER, null, clock.instant());
        when(access.require(eq(orgId), eq(userId), eq(OrgAction.VIEW)))
                .thenReturn(new OrganizationAccess.Membership(org, member));
    }

    @Test
    void normalizeHostname_cleanDomain() {
        assertThat(service.normalizeHostname("https://www.OSIS-smansa.my.id/")).isEqualTo("www.osis-smansa.my.id");
        assertThat(service.normalizeHostname("  webUser.id:8080/path  ")).isEqualTo("webuser.id");
    }

    @Test
    void normalizeHostname_rejectsLocalhostAndSystemDomain() {
        assertThatThrownBy(() -> service.normalizeHostname("localhost"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("tidak diizinkan");

        assertThatThrownBy(() -> service.normalizeHostname("192.168.1.1"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Alamat IP");

        assertThatThrownBy(() -> service.normalizeHostname("osis.orgspace.app"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("internal OrgSpace");
    }

    @Test
    void addDomain_createsPendingDomainWithInstructions() {
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        Organization org = new Organization(OrganizationType.ORGANIZATION, "OSIS Smansa", userId);
        OrganizationMember member = new OrganizationMember(orgId, userId, MemberRole.OWNER, null, clock.instant());
        when(access.require(eq(orgId), eq(userId), eq(OrgAction.VIEW)))
                .thenReturn(new OrganizationAccess.Membership(org, member));

        when(siteDomainRepository.findFirstBySiteIdAndStatusNot(siteId, "removed")).thenReturn(Optional.empty());
        when(siteDomainRepository.save(any(SiteDomain.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainResponse response = service.addDomain(siteId, userId, new AddDomainRequest("www.osissmansa.my.id"));

        assertThat(response.hostname()).isEqualTo("www.osissmansa.my.id");
        assertThat(response.status()).isEqualTo("pending");
        assertThat(response.instructions()).hasSize(3);
        assertThat(response.instructions().get(0).type()).isEqualTo("CNAME");
        assertThat(response.instructions().get(0).value()).isEqualTo("custom.orgspace.site");
        assertThat(response.instructions().get(1).type()).isEqualTo("TXT");
        assertThat(response.instructions().get(0).host()).isEqualTo("www");
        assertThat(response.instructions().get(1).host()).isEqualTo("_orgspace-verify.www");
        assertThat(response.defaultUrl()).isEqualTo("https://orgspace.app/osis-smansa");
        // www.X didaftarkan: X (tanpa www) ikut dilayani jika record A-nya dipasang (opsional).
        assertThat(response.instructions().get(2).type()).isEqualTo("A");
        assertThat(response.instructions().get(2).host()).isEqualTo("@");
        assertThat(response.instructions().get(2).note()).startsWith("Opsional");
    }

    @Test
    void addDomain_apexUnderIndonesianSuffixGetsARecord() {
        asOwner();
        when(siteDomainRepository.findFirstBySiteIdAndStatusNot(siteId, "removed")).thenReturn(Optional.empty());
        when(siteDomainRepository.save(any(SiteDomain.class))).thenAnswer(inv -> inv.getArgument(0));

        // osis-smansa.sch.id adalah domain utama (.sch.id akhiran publik), bukan subdomain "osis-smansa".
        DomainResponse response = service.addDomain(siteId, userId, new AddDomainRequest("osis-smansa.sch.id"));

        assertThat(response.instructions().get(0).type()).isEqualTo("A");
        assertThat(response.instructions().get(0).host()).isEqualTo("@");
        assertThat(response.instructions().get(1).host()).isEqualTo("_orgspace-verify");
        assertThat(response.instructions().get(2).type()).isEqualTo("CNAME");
        assertThat(response.instructions().get(2).host()).isEqualTo("www");
    }

    @Test
    void normalizeHostname_rejectsBarePublicSuffix() {
        assertThatThrownBy(() -> service.normalizeHostname("sch.id"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("domain lengkap");
    }

    @Test
    void addDomain_activeDomainOnOtherSiteBlocks() {
        asOwner();
        SiteDomain other = new SiteDomain(UUID.randomUUID(), "osis.id", "x");
        other.setStatus("active");
        when(siteDomainRepository.findByHostnameAndStatus("osis.id", "active")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.addDomain(siteId, userId, new AddDomainRequest("osis.id")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("sudah aktif");
    }

    @Test
    void verifyDomain_bypassIgnoredWhenServerDoesNotAllowIt() {
        asOwner();
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "domain-yang-tidak-ada-orgspace-uji.my.id", "token123");
        domain.setId(domainId);
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        // Tanpa izin server, bypassDns=true tetap wajib lolos cek DNS → gagal, domain tetap pending.
        assertThatThrownBy(() -> service.verifyDomain(siteId, domainId, userId, true))
                .isInstanceOf(ApiException.class);
        assertThat(domain.getStatus()).isEqualTo("pending");
    }

    @Test
    void verifyDomain_withBypassActivatesDomain() {
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "www.osissmansa.my.id", "token123");
        domain.setId(domainId);

        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        Organization org = new Organization(OrganizationType.ORGANIZATION, "OSIS Smansa", userId);
        OrganizationMember member = new OrganizationMember(orgId, userId, MemberRole.ADMIN, null, clock.instant());
        when(access.require(eq(orgId), eq(userId), eq(OrgAction.VIEW)))
                .thenReturn(new OrganizationAccess.Membership(org, member));
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(siteDomainRepository.save(any(SiteDomain.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainResponse response = serviceWith(true).verifyDomain(siteId, domainId, userId, true);

        assertThat(response.status()).isEqualTo("active");
        assertThat(response.activatedAt()).isNotNull();
    }

    @Test
    void verifyDomain_warnsWhenDnsDoesNotPointToOrgSpace() {
        asOwner();
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "osis.id", "token123");
        domain.setId(domainId);
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(siteDomainRepository.save(any(SiteDomain.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dns.txt("_orgspace-verify.osis.id")).thenReturn(List.of("token123"));
        when(dns.addresses("osis.id")).thenReturn(Set.of("1.2.3.4"));

        DomainResponse response = service.verifyDomain(siteId, domainId, userId, false);

        // Kepemilikan terbukti → aktif, tapi pengguna diberi tahu DNS-nya belum mengarah ke OrgSpace.
        assertThat(response.status()).isEqualTo("active");
        assertThat(response.lastError()).contains("1.2.3.4").contains("103.175.216.42");

        when(dns.addresses("osis.id")).thenReturn(Set.of("103.175.216.42"));
        domain.setStatus("pending");
        assertThat(service.verifyDomain(siteId, domainId, userId, false).lastError()).isNull();
    }

    @Test
    void recheck_releasesDomainWhoseTxtIsGoneLongerThanSevenDays() {
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "osis.id", "token123");
        domain.setId(domainId);
        domain.setStatus("active");
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(dns.txt("_orgspace-verify.osis.id")).thenReturn(List.of());

        // Hari pertama TXT hilang: masih aktif, diberi peringatan dan tanggal batas.
        service.recheck(domainId);
        assertThat(domain.getStatus()).isEqualTo("active");
        assertThat(domain.getFailingSince()).isEqualTo(clock.instant());
        assertThat(domain.getLastError()).contains("24 Oktober 2026");

        // Masih hilang setelah 7 hari: dilepas, sehingga pemilik baru domain bisa memakainya.
        domain.setFailingSince(clock.instant().minus(SiteDomainService.RELEASE_AFTER));
        service.recheck(domainId);
        assertThat(domain.getStatus()).isEqualTo("pending");
        assertThat(domain.getActivatedAt()).isNull();
    }

    @Test
    void recheck_txtBackClearsFailure() {
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "osis.id", "token123");
        domain.setId(domainId);
        domain.setStatus("active");
        domain.setFailingSince(clock.instant().minusSeconds(3600));
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(dns.txt("_orgspace-verify.osis.id")).thenReturn(List.of("token123"));
        when(dns.addresses("osis.id")).thenReturn(Set.of("103.175.216.42"));

        service.recheck(domainId);

        assertThat(domain.getStatus()).isEqualTo("active");
        assertThat(domain.getFailingSince()).isNull();
        assertThat(domain.getLastError()).isNull();
    }

    @Test
    void verifyRegistersDomainAndItsWwwPairAtHosting() {
        asOwner();
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "osis.id", "token123");
        domain.setId(domainId);
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(siteDomainRepository.save(any(SiteDomain.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dns.txt("_orgspace-verify.osis.id")).thenReturn(List.of("token123"));
        when(dns.addresses("osis.id")).thenReturn(Set.of("103.175.216.42"));
        when(vercel.addDomain("osis.id")).thenReturn("Server hosting meminta bukti tambahan: TXT _vercel.osis.id");

        DomainResponse response = service.verifyDomain(siteId, domainId, userId, false);

        verify(vercel).addDomain("osis.id");
        verify(vercel).addDomain("www.osis.id");
        // Catatan Vercel diteruskan ke pengguna lewat lastError (tampil di app).
        assertThat(response.lastError()).contains("_vercel.osis.id");
    }

    @Test
    void verifyFailsWhenHostingRejectsTheDomain() {
        asOwner();
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "osis.id", "token123");
        domain.setId(domainId);
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(dns.txt("_orgspace-verify.osis.id")).thenReturn(List.of("token123"));
        when(vercel.addDomain("osis.id")).thenThrow(new ApiException(com.aris.templateapp.common.exception.ErrorCode.DOMAIN_PROVIDER_FAILED));

        assertThatThrownBy(() -> service.verifyDomain(siteId, domainId, userId, false)).isInstanceOf(ApiException.class);
    }

    @Test
    void removingActiveDomainReleasesItAtHosting() {
        asOwner();
        UUID domainId = UUID.randomUUID();
        SiteDomain domain = new SiteDomain(siteId, "www.osis.id", "token123");
        domain.setId(domainId);
        domain.setStatus("active");
        when(siteDomainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        service.removeDomain(siteId, domainId, userId);

        verify(vercel).removeDomain("www.osis.id");
        verify(vercel).removeDomain("osis.id");
    }

    @Test
    void alternateHostOnlyForApexAndWww() {
        assertThat(SiteDomainService.alternateHost("osis.sch.id")).isEqualTo("www.osis.sch.id");
        assertThat(SiteDomainService.alternateHost("www.osis.sch.id")).isEqualTo("osis.sch.id");
        assertThat(SiteDomainService.alternateHost("web.osis.sch.id")).isNull();
    }
}
