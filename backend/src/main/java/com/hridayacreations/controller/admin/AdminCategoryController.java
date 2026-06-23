package com.hridayacreations.controller.admin;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.CreateCategoryRequest;
import com.hridayacreations.dto.request.UpdateCategoryRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.CategoryResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.service.interfaces.CategoryService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin category management (create, update, delete, list).
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Categories", description = "Administrative category management (ADMIN only)")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.CATEGORY_CREATED, categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CATEGORY_UPDATED,
                categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CATEGORY_DELETED));
    }

    @GetMapping
    @Operation(summary = "List all categories including inactive (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CATEGORIES_FETCHED,
                categoryService.getAllCategories(keyword, false, pageable)));
    }
}
