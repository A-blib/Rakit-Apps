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
import java.util.Optional;
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
    private SiteReleaseRepository siteReleaseRepository;

    @Mock
    private SiteDomainRepository siteDomainRepository;

    @Mock
    private OrganizationAccess access;

    @Mock
    private SiteStorage siteStorage;

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
                "https://orgspace.app"
        );
        properties = new AppProperties(null, null, null, "templateapp", "Asia/Jakarta", null, null, null, sitesProp, null, null);
        service = new SiteDomainService(siteRepository, siteReleaseRepository, siteDomainRepository, access, siteStorage, properties, clock);

        site = new Site(UUID.randomUUID(), orgId, "osis-smansa");
        site.setId(siteId);
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

        when(siteDomainRepository.findByHostname("www.osissmansa.my.id")).thenReturn(Optional.empty());
        when(siteDomainRepository.findFirstBySiteIdAndStatusNot(siteId, "removed")).thenReturn(Optional.empty());
        when(siteDomainRepository.save(any(SiteDomain.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainResponse response = service.addDomain(siteId, userId, new AddDomainRequest("www.osissmansa.my.id"));

        assertThat(response.hostname()).isEqualTo("www.osissmansa.my.id");
        assertThat(response.status()).isEqualTo("pending");
        assertThat(response.instructions()).hasSize(2);
        assertThat(response.instructions().get(0).type()).isEqualTo("CNAME");
        assertThat(response.instructions().get(0).value()).isEqualTo("custom.orgspace.site");
        assertThat(response.instructions().get(1).type()).isEqualTo("TXT");
        assertThat(response.instructions().get(1).host()).isEqualTo("_orgspace-verify.www.osissmansa.my.id");
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

        DomainResponse response = service.verifyDomain(siteId, domainId, userId, true);

        assertThat(response.status()).isEqualTo("active");
        assertThat(response.activatedAt()).isNotNull();
    }
}
