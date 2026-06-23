package com.hridayacreations.repository;

import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCaseAndIdNot(String categoryName, Long id);

    Page<Category> findByStatus(CategoryStatus status, Pageable pageable);

    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);
}
