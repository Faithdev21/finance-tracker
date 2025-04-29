package com.example.financetracker.service.impl;

import com.example.financetracker.dto.CategoryDto;
import com.example.financetracker.dto.UserDto;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.repository.CategoryRepository;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.repository.UserRepository;
import com.example.financetracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

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
        log.info("Fetching default categories");
        List<CategoryEntity> categories = categoryRepository.findByNameIn(Arrays.asList("Еда", "Транспорт", "Зарплата"));
        if (categories == null) {
            log.error("categoryRepository.findByNameIn returned null");
            return new ArrayList<>();
        }
        if (categories.isEmpty()) {
            log.warn("No default categories found in the database");
        } else {
            log.info("Default categories fetched: {}", categories.size());
        }
        return categories;
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
        UserDto user = userServiceImpl.getCurrentUserDto();
        log.info("Fetching categories for user: {}", user.getUsername());
        List<CategoryEntity> userCategories = categoryRepository.findCategoriesByUserId(user.getId());

        return userCategories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList() );
    }
}
