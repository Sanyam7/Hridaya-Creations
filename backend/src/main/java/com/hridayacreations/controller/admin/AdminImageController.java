package com.hridayacreations.controller.admin;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.ProductImageResponse;
import com.hridayacreations.service.interfaces.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin product image management backed by Cloudinary (upload, replace, delete, list).
 */
@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Product Images", description = "Administrative product image management (ADMIN only)")
public class AdminImageController {

    private final ProductImageService productImageService;

    @GetMapping
    @Operation(summary = "List a product's images")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> list(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_FETCHED,
                productImageService.getProductImages(productId)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a product image")
    public ResponseEntity<ApiResponse<ProductImageResponse>> upload(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean primary) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.IMAGE_UPLOADED,
                        productImageService.uploadImage(productId, file, primary)));
    }

    @PutMapping(path = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Replace an existing product image")
    public ResponseEntity<ApiResponse<ProductImageResponse>> replace(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.IMAGE_REPLACED,
                productImageService.replaceImage(productId, imageId, file)));
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete a product image")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long productId, @PathVariable Long imageId) {
        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.IMAGE_DELETED));
    }
}
