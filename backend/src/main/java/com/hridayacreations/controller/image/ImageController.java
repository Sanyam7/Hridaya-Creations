package com.hridayacreations.controller.image;

import com.hridayacreations.entity.StoredImage;
import com.hridayacreations.service.interfaces.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Public endpoint that serves database-stored images by id. Returns the raw image bytes (not the
 * standard ApiResponse envelope) so the URL can be used directly in {@code <img src>}.
 */
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Public image serving")
public class ImageController {

    private final ImageStorageService imageStorageService;

    @GetMapping("/{id}")
    @Operation(summary = "Serve a stored image by id")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        StoredImage image = imageStorageService.get(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .eTag(image.getId())
                .body(image.getData());
    }
}
