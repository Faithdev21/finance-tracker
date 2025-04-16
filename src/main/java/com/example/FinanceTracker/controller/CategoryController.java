package com.example.FinanceTracker.controller;

import com.example.FinanceTracker.dto.CategoryDto;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/add_category")
    public ResponseEntity<CategoryDto> createNewCategory(@RequestBody CategoryDto categoryDto) {
        CategoryEntity category = categoryService.addCategory(categoryDto);
        return ResponseEntity.ok(CategoryDto.fromEntity(category));
    }

    @PostMapping("/category_to_current_user")
    public ResponseEntity<CategoryDto> addCategoryToCurrentUser(@RequestBody CategoryDto categoryDto) {
        CategoryDto category = categoryService.addCategoryToCurrentUser(categoryDto);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/get_my_categories")
    public ResponseEntity<List<CategoryDto>> getMyCategories() {
        List<CategoryDto> categories = categoryService.getCurrentUserCategories();
        log.info("Returning categories for current user: {}", categories);
        return ResponseEntity.ok(categories);
    }
}
