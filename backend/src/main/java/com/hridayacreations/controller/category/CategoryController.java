package com.hridayacreations.controller.category;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.CategoryResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.service.interfaces.CategoryService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public category browsing endpoints.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Public category browsing")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List categories (paginated). By default only active categories are returned")
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CATEGORIES_FETCHED,
                categoryService.getAllCategories(keyword, activeOnly, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by id")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CATEGORY_FETCHED,
                categoryService.getCategoryById(id)));
    }
}
