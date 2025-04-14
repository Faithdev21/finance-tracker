package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.CategoryDto;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.exception.ResourceNotFoundException;
import com.example.FinanceTracker.repository.CategoryRepository;
import com.example.FinanceTracker.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final UserServiceImpl userServiceImpl;

    public CategoryEntity getCategoryByIdAndUser(Long categoryId, UserEntity user) {
        return user.getCategories().stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId + " for user: " + user.getEmail()
                ));
    }

    public List<CategoryEntity> getDefaultCategories() {
        return categoryRepository.findByNameIn(Arrays.asList("Еда", "Транспорт", "Зарплата"));
    }

    public CategoryEntity addCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Категория с именем '" + categoryDto.getName() + "' уже существует");
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(categoryDto.getName())
                .type(categoryDto.getType())
                .build();

        return categoryRepository.save(category);
    }
    public CategoryEntity addCategoryToCurrentUser(CategoryDto categoryDto) {
        UserEntity user = userServiceImpl.getCurrentUser();

        boolean alreadyHas = user.getCategories().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(categoryDto.getName()));
        if (alreadyHas) {
            throw new IllegalArgumentException("Категория уже добавлена пользователю");
        }

        CategoryEntity category = categoryRepository.findByName(categoryDto.getName())
                .orElseGet(() -> {
                    CategoryEntity newCategory = CategoryEntity.builder()
                            .name(categoryDto.getName())
                            .type(categoryDto.getType())
                            .build();
                    return categoryRepository.save(newCategory);
                });

        user.getCategories().add(category);
        userServiceImpl.save(user);

        return category;
    }

    public Set<CategoryEntity> getCurrentUserCategories() {
        UserEntity user = userServiceImpl.getCurrentUser();
        return user.getCategories();
    }
}
