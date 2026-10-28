package com.aris.templateapp.site;

import com.aris.templateapp.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Memeriksa ulang domain kustom aktif secara berkala (lihat {@link SiteDomainService#recheck}). */
@Component
@RequiredArgsConstructor
public class SiteDomainMonitor {

    private final SiteDomainRepository siteDomainRepository;
    private final SiteDomainService siteDomainService;
    private final AppProperties properties;

    @Scheduled(cron = "0 15 */6 * * *", zone = "${app.timezone}")
    public void recheckActiveDomains() {
        // Di dev domain diaktifkan tanpa DNS (bypass), jadi pasti "gagal" dan akan dilepas: tidak dicek.
        if (properties.sites().allowDnsBypass()) {
            return;
        }
        for (SiteDomain domain : siteDomainRepository.findByStatus("active")) {
            siteDomainService.recheck(domain.getId());
        }
    }
}
