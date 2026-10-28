package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SiteDomainRepository extends JpaRepository<SiteDomain, UUID> {

    List<SiteDomain> findBySiteId(UUID siteId);

    Optional<SiteDomain> findFirstBySiteIdAndStatusNot(UUID siteId, String status);

    Optional<SiteDomain> findFirstBySiteIdAndStatus(UUID siteId, String status);

    Optional<SiteDomain> findByHostname(String hostname);

    Optional<SiteDomain> findByHostnameAndStatus(String hostname, String status);

    List<SiteDomain> findByStatus(String status);

    /**
     * Domain aktif untuk host yang dibuka pengunjung. {@code osis.id} dan {@code www.osis.id} dianggap satu website:
     * pengguna mendaftarkan salah satu, pengunjung sering mengetik yang lain.
     */
    default Optional<SiteDomain> findActiveForHost(String host) {
        Optional<SiteDomain> exact = findByHostnameAndStatus(host, "active");
        if (exact.isPresent()) {
            return exact;
        }
        String alternate = host.startsWith("www.") ? host.substring(4) : "www." + host;
        return findByHostnameAndStatus(alternate, "active");
    }

    boolean existsByHostnameAndStatusNot(String hostname, String status);

    boolean existsBySiteIdAndStatusNot(UUID siteId, String status);

    void deleteBySiteIdAndId(UUID siteId, UUID id);
}
