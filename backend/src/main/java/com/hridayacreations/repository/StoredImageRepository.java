package com.hridayacreations.repository;

import com.hridayacreations.entity.StoredImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence for {@link StoredImage} binary blobs.
 */
public interface StoredImageRepository extends JpaRepository<StoredImage, String> {
}
