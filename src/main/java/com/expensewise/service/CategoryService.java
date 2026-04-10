package com.expensewise.service;

import com.expensewise.domain.entity.Category;
import com.expensewise.domain.repository.CategoryRepository;
import com.expensewise.dto.category.CategoryRequest;
import com.expensewise.dto.category.CategoryResponse;
import com.expensewise.exception.DuplicateResourceException;
import com.expensewise.exception.ResourceNotFoundException;
import com.expensewise.security.SecurityUserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SecurityUserContext securityUserContext;

    public CategoryService(CategoryRepository categoryRepository, SecurityUserContext securityUserContext) {
        this.categoryRepository = categoryRepository;
        this.securityUserContext = securityUserContext;
    }

    public List<CategoryResponse> getAllCategories() {
        Long userId = securityUserContext.getCurrentUserId();
        return categoryRepository.findAllByUserIdOrDefault(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Long userId = securityUserContext.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserIdOrDefault(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        var user = securityUserContext.getCurrentUser();
        if (categoryRepository.existsByNameAndUserId(request.name(), user.getId())) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }

        Category category = Category.builder()
                .name(request.name())
                .icon(request.icon())
                .user(user)
                .isDefault(false)
                .build();

        category = categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Long userId = securityUserContext.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.isDefault()) {
            throw new IllegalArgumentException("Default categories cannot be modified");
        }

        category.setName(request.name());
        category.setIcon(request.icon());

        category = categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Long userId = securityUserContext.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.isDefault()) {
            throw new IllegalArgumentException("Default categories cannot be deleted");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.isDefault(),
                category.getCreatedAt()
        );
    }
}
