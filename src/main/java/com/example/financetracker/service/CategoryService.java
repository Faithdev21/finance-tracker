package com.example.financetracker.service;

import java.util.List;

import com.example.financetracker.dto.CategoryDto;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryService {
    CategoryEntity getCategoryByIdAndUser(Long categoryId, UserEntity user);

    List<CategoryEntity> getDefaultCategories();

    @Transactional
    CategoryEntity addCategory(CategoryDto categoryDto);

    @Transactional
    CategoryDto addCategoryToCurrentUser(CategoryDto categoryDto);

    @Transactional
    List<CategoryDto> getCurrentUserCategories();
}
