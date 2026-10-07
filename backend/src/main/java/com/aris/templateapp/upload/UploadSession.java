package com.aris.templateapp.upload;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tabel {@code upload_sessions}: satu ZIP yang sedang dikirim per potongan (alur-fitur-upload.md bagian 5.7b).
 * Isi potongan ditulis ke file sementara di disk; database hanya mencatat sudah sampai byte ke berapa.
 */
@Entity
@Table(name = "upload_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadSession {

    public enum Status implements PersistableEnum {
        UPLOADING("uploading"),
        COMPLETED("completed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String value() {
            return value;
        }

        @Converter(autoApply = true)
        public static class JpaConverter extends PersistableEnumConverter<Status> {
            public JpaConverter() {
                super(Status.class);
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID providerId;

    // Diisi untuk "Upload file perbaikan": ZIP baru menggantikan ZIP template yang gagal pengecekan.
    private UUID templateId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private long totalSize;

    @Column(nullable = false)
    private long receivedSize;

    @Column(nullable = false)
    private Status status = Status.UPLOADING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public UploadSession(UUID providerId, UUID templateId, String fileName, long totalSize, Instant now) {
        this.providerId = providerId;
        this.templateId = templateId;
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    void fillTimestamps() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }
}
