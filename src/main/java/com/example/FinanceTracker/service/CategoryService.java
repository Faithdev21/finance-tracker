package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.CategoryDto;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.exception.ResourceNotFoundException;
import com.example.FinanceTracker.repository.CategoryRepository;
import com.example.FinanceTracker.entity.UserEntity;
import com.example.FinanceTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final UserServiceImpl userServiceImpl;
    @Autowired
    private UserRepository userRepository;

    public CategoryEntity getCategoryByIdAndUser(Long categoryId, UserEntity user) {
        return user.getCategories().stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId + " for user: " + user.getUsername()
                ));
    }

    public List<CategoryEntity> getDefaultCategories() {
        return categoryRepository.findByNameIn(Arrays.asList("Еда", "Транспорт", "Зарплата"));
    }

    @Transactional
    public CategoryEntity addCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' exists already");
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(categoryDto.getName())
                .type(categoryDto.getType())
                .build();

        return categoryRepository.save(category);
    }

    @Transactional
    public CategoryDto addCategoryToCurrentUser(CategoryDto categoryDto) {
        UserEntity user = userServiceImpl.getCurrentUser();
        CategoryEntity category = categoryRepository.findByName(categoryDto.getName())
                .orElseGet(() -> {
                    CategoryEntity newCategory = CategoryEntity.builder()
                            .name(categoryDto.getName())
                            .type(categoryDto.getType())
                            .build();
                    return categoryRepository.save(newCategory);
                });

        Set<CategoryEntity> userCategories = user.getCategories();
        if (userCategories == null) {
            userCategories = new HashSet<>();
            user.setCategories(userCategories);
        }

        if (!userCategories.contains(category)) {
            userCategories.add(category);
            userServiceImpl.save(user);
            log.info("Added category {} to user {}", category.getName(), user.getUsername());
        } else {
            log.info("Category {} is already associated with user {}", category.getName(), user.getUsername());
        }

        return CategoryDto.fromEntity(category);
    }

    @Transactional
    public List<CategoryDto> getCurrentUserCategories() {
        UserEntity user = userServiceImpl.getCurrentUser();
        log.info("Fetching categories for user: {}", user.getUsername());
        List<CategoryEntity> userCategories = categoryRepository.findCategoriesByUserId(user.getId());

        return userCategories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList() );
    }
}
