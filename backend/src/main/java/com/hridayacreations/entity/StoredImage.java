package com.hridayacreations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A binary image stored directly in the database. Used as the storage backend for product and
 * category images when an external image CDN (Cloudinary) is not configured, so uploads persist
 * across restarts without an external dependency. Served publicly via {@code /api/v1/images/{id}}.
 */
@Entity
@Table(name = "stored_images")
@Getter
@Setter
@NoArgsConstructor
public class StoredImage {

    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** Raw image bytes (maps to PostgreSQL {@code bytea}). */
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
