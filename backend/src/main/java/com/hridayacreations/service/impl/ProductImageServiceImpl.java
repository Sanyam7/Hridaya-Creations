package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.ProductMapper;
import com.hridayacreations.dto.response.ProductImageResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductImage;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.ProductImageRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.CloudinaryService;
import com.hridayacreations.service.interfaces.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Manages product images: upload, replace and delete, keeping the Cloudinary asset and the database
 * record in sync and maintaining a single primary image per product.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file, boolean primary) {
        Product product = findProduct(productId);
        CloudinaryService.UploadResult uploaded = cloudinaryService.upload(file);

        boolean makePrimary = primary || product.getImages().isEmpty();
        if (makePrimary) {
            productImageRepository.clearPrimaryFlagForProduct(productId);
            product.getImages().forEach(img -> img.setPrimaryImage(false));
        }

        ProductImage image = ProductImage.builder()
                .imageUrl(uploaded.url())
                .publicId(uploaded.publicId())
                .primaryImage(makePrimary)
                .displayOrder(product.getImages().size())
                .build();
        product.addImage(image);
        productRepository.save(product);

        auditLogService.log(AuditAction.IMAGE_UPLOADED, "Product", String.valueOf(productId),
                "Uploaded image " + uploaded.publicId());
        return productMapper.toImageResponse(image);
    }

    @Override
    @Transactional
    public ProductImageResponse replaceImage(Long productId, Long imageId, MultipartFile file) {
        findProduct(productId);
        ProductImage image = findImage(imageId, productId);

        String oldPublicId = image.getPublicId();
        CloudinaryService.UploadResult uploaded = cloudinaryService.upload(file);
        image.setImageUrl(uploaded.url());
        image.setPublicId(uploaded.publicId());
        productImageRepository.save(image);

        cloudinaryService.delete(oldPublicId);
        auditLogService.log(AuditAction.IMAGE_UPLOADED, "Product", String.valueOf(productId),
                "Replaced image " + imageId);
        return productMapper.toImageResponse(image);
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        Product product = findProduct(productId);
        ProductImage image = findImage(imageId, productId);
        boolean wasPrimary = image.isPrimaryImage();
        String publicId = image.getPublicId();

        product.removeImage(image);
        productRepository.save(product);

        // Promote a new primary image if the deleted one was primary.
        if (wasPrimary && !product.getImages().isEmpty()) {
            product.getImages().get(0).setPrimaryImage(true);
            productRepository.save(product);
        }

        cloudinaryService.delete(publicId);
        auditLogService.log(AuditAction.IMAGE_DELETED, "Product", String.valueOf(productId),
                "Deleted image " + imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {
        findProduct(productId);
        return productImageRepository.findByProduct_IdOrderByDisplayOrderAsc(productId).stream()
                .map(productMapper::toImageResponse)
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private ProductImage findImage(Long imageId, Long productId) {
        return productImageRepository.findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));
    }
}
