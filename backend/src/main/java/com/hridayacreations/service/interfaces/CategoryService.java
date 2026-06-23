package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.CreateCategoryRequest;
import com.hridayacreations.dto.request.UpdateCategoryRequest;
import com.hridayacreations.dto.response.CategoryResponse;
import com.hridayacreations.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

/**
 * Catalog category management (admin writes, public reads).
 */
public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    CategoryResponse getCategoryById(Long id);

    PagedResponse<CategoryResponse> getAllCategories(String keyword, Boolean activeOnly, Pageable pageable);
}
