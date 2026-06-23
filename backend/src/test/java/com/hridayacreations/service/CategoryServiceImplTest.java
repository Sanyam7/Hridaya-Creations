package com.hridayacreations.service;

import com.hridayacreations.dto.mapper.CategoryMapper;
import com.hridayacreations.dto.request.CreateCategoryRequest;
import com.hridayacreations.dto.response.CategoryResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.service.impl.CategoryServiceImpl;
import com.hridayacreations.service.interfaces.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_persistsAndReturnsResponse() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .categoryName("Personalized Mugs")
                .description("Custom mugs")
                .build();

        when(categoryRepository.existsByCategoryNameIgnoreCase("Personalized Mugs")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(Category.builder().build());
        Category saved = Category.builder().categoryName("Personalized Mugs").build();
        saved.setId(1L);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(productRepository.countByCategory_Id(1L)).thenReturn(0L);
        when(categoryMapper.toResponse(saved))
                .thenReturn(CategoryResponse.builder().id(1L).categoryName("Personalized Mugs").build());

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductCount()).isZero();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_duplicateName_throws() {
        CreateCategoryRequest request = CreateCategoryRequest.builder().categoryName("Mugs").build();
        when(categoryRepository.existsByCategoryNameIgnoreCase("Mugs")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_withProducts_throwsBusinessRule() {
        Category category = Category.builder().categoryName("Mugs").build();
        category.setId(5L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productRepository.countByCategory_Id(5L)).thenReturn(3L);

        assertThatThrownBy(() -> categoryService.deleteCategory(5L))
                .isInstanceOf(BusinessRuleException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void getCategoryById_notFound_throws() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
