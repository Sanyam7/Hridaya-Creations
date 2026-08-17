package com.hridayacreations.controller.image;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.UploadedImageResponse;
import com.hridayacreations.entity.StoredImage;
import com.hridayacreations.service.interfaces.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Stores an image a customer is attaching to a personalised product and returns its reference.
     *
     * <p>Requires authentication. Whether the image is actually allowed on a given product is not
     * decided here but at add-to-cart, where the URL is checked against that product's enabled
     * customization options — uploading is deliberately independent of any one product so the
     * customer can swap their photo before committing to the cart.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image for product customization")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UploadedImageResponse>> upload(@RequestParam("file") MultipartFile file) {
        ImageStorageService.StoredRef ref = imageStorageService.store(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                MessageConstants.IMAGE_UPLOADED,
                UploadedImageResponse.builder().id(ref.id()).url(ref.url()).build()));
    }

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
