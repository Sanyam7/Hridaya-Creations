package com.hridayacreations.service.impl;

import com.hridayacreations.entity.StoredImage;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.exception.FileStorageException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.StoredImageRepository;
import com.hridayacreations.service.interfaces.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Database-backed image storage. Validates content type/size and persists the bytes as a
 * {@link StoredImage}, returning a public URL ({@code /api/v1/images/{id}}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB

    private final StoredImageRepository repository;

    @Override
    @Transactional
    public StoredRef store(MultipartFile file) {
        validate(file);
        try {
            StoredImage image = new StoredImage();
            image.setContentType(file.getContentType().toLowerCase());
            image.setFileName(StringUtils.cleanPath(Objects.toString(file.getOriginalFilename(), "image")));
            image.setSizeBytes(file.getSize());
            image.setData(file.getBytes());
            StoredImage saved = repository.save(image);
            log.debug("Stored image {} ({} bytes)", saved.getId(), saved.getSizeBytes());
            return new StoredRef(IMAGE_URL_PREFIX + saved.getId(), saved.getId());
        } catch (IOException ex) {
            throw new FileStorageException("Failed to read the uploaded image", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StoredImage get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", id));
    }

    @Override
    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
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
