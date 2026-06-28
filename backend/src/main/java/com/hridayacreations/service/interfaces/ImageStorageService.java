package com.hridayacreations.service.interfaces;

import com.hridayacreations.entity.StoredImage;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded images in the database and serves them back. Used as the image backend when an
 * external CDN is not configured.
 */
public interface ImageStorageService {

    /** Public URL path prefix under which stored images are served. */
    String IMAGE_URL_PREFIX = "/api/v1/images/";

    /** Reference to a stored image: its public URL and storage id. */
    record StoredRef(String url, String id) {
    }

    /** Validate and persist an uploaded image; returns its public URL + id. */
    StoredRef store(MultipartFile file);

    /** Fetch a stored image by id (throws if not found). */
    StoredImage get(String id);

    /** Delete a stored image by id (no-op if it does not exist). */
    void delete(String id);

    /** If the given URL points at a DB-stored image, return its id, else {@code null}. */
    static String idFromUrl(String url) {
        if (url != null && url.startsWith(IMAGE_URL_PREFIX)) {
            return url.substring(IMAGE_URL_PREFIX.length());
        }
        return null;
    }
}
