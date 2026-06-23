package com.hridayacreations.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hridayacreations.config.AppProperties;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.exception.FileStorageException;
import com.hridayacreations.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cloudinary-backed image storage. Validates content type and size, uploads into the configured
 * folder, and degrades gracefully to a placeholder URL when disabled.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB

    private final Cloudinary cloudinary;
    private final AppProperties appProperties;

    @Override
    public UploadResult upload(MultipartFile file) {
        validate(file);

        if (!appProperties.getCloudinary().isEnabled()) {
            String publicId = appProperties.getCloudinary().getFolder() + "/local-" + UUID.randomUUID();
            String url = "https://placehold.co/600x600?text=" +
                    StringUtils.replace(StringUtils.cleanPath(file.getOriginalFilename()), " ", "+");
            log.info("[CLOUDINARY DISABLED] Skipping real upload; returning placeholder for {}", file.getOriginalFilename());
            return new UploadResult(url, publicId);
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", appProperties.getCloudinary().getFolder(),
                    "resource_type", "image",
                    "unique_filename", true,
                    "overwrite", false));
            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            log.debug("Uploaded image to Cloudinary: {}", publicId);
            return new UploadResult(url, publicId);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to upload image to Cloudinary", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        if (!StringUtils.hasText(publicId) || !appProperties.getCloudinary().isEnabled()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            log.debug("Deleted Cloudinary asset: {}", publicId);
        } catch (IOException ex) {
            // Deletion failures should not break the business flow; surface as a warning.
            log.warn("Failed to delete Cloudinary asset {}: {}", publicId, ex.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required and must not be empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Image exceeds the maximum allowed size of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported image type. Allowed: JPEG, PNG, WEBP, GIF");
        }
    }
}
