package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.CategoryMapper;
import com.hridayacreations.dto.request.CreateCategoryRequest;
import com.hridayacreations.dto.request.UpdateCategoryRequest;
import com.hridayacreations.dto.response.CategoryResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.entity.enums.CategoryStatus;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.CategoryService;
import com.hridayacreations.service.interfaces.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default category management implementation. Enforces unique category names and prevents deletion
 * of categories that still contain products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final AuditLogService auditLogService;
    private final ImageStorageService imageStorageService;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = request.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category", "name", name);
        }

        Category category = categoryMapper.toEntity(request);
        category.setCategoryName(name);
        category.setStatus(request.getStatus() != null ? request.getStatus() : CategoryStatus.ACTIVE);

        Category saved = categoryRepository.save(category);
        auditLogService.log(AuditAction.CATEGORY_CREATED, "Category", String.valueOf(saved.getId()),
                "Created category: " + saved.getCategoryName());
        return toResponseWithCount(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = findCategory(id);
        String name = request.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Category", "name", name);
        }

        categoryMapper.updateEntity(request, category);
        category.setCategoryName(name);
        Category saved = categoryRepository.save(category);
        auditLogService.log(AuditAction.CATEGORY_UPDATED, "Category", String.valueOf(saved.getId()),
                "Updated category: " + saved.getCategoryName());
        return toResponseWithCount(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        long productCount = productRepository.countByCategory_Id(id);
        if (productCount > 0) {
            throw new BusinessRuleException(
                    "Cannot delete category '%s' because it has %d product(s). Reassign or remove them first."
                            .formatted(category.getCategoryName(), productCount));
        }
        categoryRepository.delete(category);
        auditLogService.log(AuditAction.CATEGORY_DELETED, "Category", String.valueOf(id),
                "Deleted category: " + category.getCategoryName());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return toResponseWithCount(findCategory(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAllCategories(String keyword, Boolean activeOnly, Pageable pageable) {
        Page<Category> page;
        if (keyword != null && !keyword.isBlank()) {
            page = categoryRepository.findByCategoryNameContainingIgnoreCase(keyword.trim(), pageable);
        } else if (Boolean.TRUE.equals(activeOnly)) {
            page = categoryRepository.findByStatus(CategoryStatus.ACTIVE, pageable);
        } else {
            page = categoryRepository.findAll(pageable);
        }
        return PagedResponse.from(page, this::toResponseWithCount);
    }

    @Override
    @Transactional
    public CategoryResponse setCategoryImage(Long id, MultipartFile file) {
        Category category = findCategory(id);
        deleteStoredImageIfAny(category.getImageUrl());
        ImageStorageService.StoredRef ref = imageStorageService.store(file);
        category.setImageUrl(ref.url());
        Category saved = categoryRepository.save(category);
        auditLogService.log(AuditAction.CATEGORY_UPDATED, "Category", String.valueOf(id),
                "Updated category image: " + saved.getCategoryName());
        return toResponseWithCount(saved);
    }

    @Override
    @Transactional
    public CategoryResponse removeCategoryImage(Long id) {
        Category category = findCategory(id);
        deleteStoredImageIfAny(category.getImageUrl());
        category.setImageUrl(null);
        Category saved = categoryRepository.save(category);
        auditLogService.log(AuditAction.CATEGORY_UPDATED, "Category", String.valueOf(id),
                "Removed category image: " + saved.getCategoryName());
        return toResponseWithCount(saved);
    }

    /** Delete the backing DB image if the URL points to one (ignores external URLs). */
    private void deleteStoredImageIfAny(String imageUrl) {
        String storedId = ImageStorageService.idFromUrl(imageUrl);
        if (storedId != null) {
            imageStorageService.delete(storedId);
        }
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse toResponseWithCount(Category category) {
        CategoryResponse response = categoryMapper.toResponse(category);
        response.setProductCount(productRepository.countByCategory_Id(category.getId()));
        return response;
    }
}
