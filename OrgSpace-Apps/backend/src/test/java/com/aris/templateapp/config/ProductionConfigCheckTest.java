package com.aris.templateapp.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductionConfigCheckTest {

    private static AppProperties properties(String hostingUrl, String domain, String publicIp, String customTarget,
                                            String githubRedirect) {
        AppProperties properties = mock(AppProperties.class);
        AppProperties.Sites sites = mock(AppProperties.Sites.class);
        when(properties.sites()).thenReturn(sites);
        when(sites.hostingUrl()).thenReturn(hostingUrl);
        when(sites.domain()).thenReturn(domain);
        when(sites.publicIp()).thenReturn(publicIp);
        when(sites.customTarget()).thenReturn(customTarget);
        when(properties.github()).thenReturn(new AppProperties.GitHub("id", "secret", githubRedirect));
        return properties;
    }

    @Test
    void productionAddressesPass() {
        AppProperties ok = properties("https://orgspace.app", "orgspace.app", "203.0.113.10", "custom.orgspace.app",
                "https://api.orgspace.app/api/auth/github/callback");
        assertThat(ProductionConfigCheck.problems(ok)).isEmpty();
    }

    @Test
    void unresolvedPlaceholdersAndLocalhostAreRefusedByName() {
        // "${...}" = variabel tidak diisi (Spring Boot membiarkan placeholder sebagai teks).
        AppProperties bad = properties("${SITES_HOSTING_URL}", "localhost:8080", "127.0.0.1", "custom.orgspace.app",
                "http://localhost:8080/api/auth/github/callback");
        assertThat(ProductionConfigCheck.problems(bad)).containsExactly(
                "SITES_HOSTING_URL (kosong)",
                "SITES_DOMAIN (masih localhost:8080)",
                "SITES_PUBLIC_IP (masih 127.0.0.1)",
                "GITHUB_REDIRECT_URI (masih http://localhost:8080/api/auth/github/callback)");
        assertThatThrownBy(() -> new ProductionConfigCheck(bad))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SITES_HOSTING_URL");
    }
}
