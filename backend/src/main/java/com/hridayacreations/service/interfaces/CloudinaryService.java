package com.hridayacreations.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over the Cloudinary asset store. When Cloudinary is disabled
 * ({@code app.cloudinary.enabled=false}) a deterministic placeholder is returned so that flows work
 * end-to-end without external credentials (useful for local dev and tests).
 */
public interface CloudinaryService {

    /**
     * Uploads an image and returns its hosted URL and public id.
     *
     * @param file the multipart image to upload
     * @return the upload result containing the secure URL and public id
     */
    UploadResult upload(MultipartFile file);

    /**
     * Deletes an asset by its public id. No-op when the id is null/blank or Cloudinary is disabled.
     */
    void delete(String publicId);

    /**
     * Result of an upload: the public delivery URL and the public id used for later deletion.
     */
    record UploadResult(String url, String publicId) {
    }
}
