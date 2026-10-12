package com.aris.templateapp.upload;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadSessionRepository extends JpaRepository<UploadSession, UUID> {

    /**
     * Dikunci (SELECT ... FOR UPDATE) agar dua potongan yang terkirim bersamaan (mis. app mengulang request)
     * tidak menulis ke posisi yang sama.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UploadSession s where s.id = :id and s.providerId = :providerId")
    Optional<UploadSession> lockOwned(UUID id, UUID providerId);

    Optional<UploadSession> findByIdAndProviderId(UUID id, UUID providerId);

    List<UploadSession> findByStatusAndUpdatedAtBefore(UploadSession.Status status, Instant before);
}
