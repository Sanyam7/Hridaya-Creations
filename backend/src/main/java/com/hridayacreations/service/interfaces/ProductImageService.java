package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin management of product images backed by Cloudinary.
 */
public interface ProductImageService {

    ProductImageResponse uploadImage(Long productId, MultipartFile file, boolean primary);

    ProductImageResponse replaceImage(Long productId, Long imageId, MultipartFile file);

    void deleteImage(Long productId, Long imageId);

    List<ProductImageResponse> getProductImages(Long productId);
}
