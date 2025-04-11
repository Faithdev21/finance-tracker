package com.example.FinanceTracker.controller;

import com.example.FinanceTracker.dto.CategoryDto;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/add_category")
    public ResponseEntity<?> createNewCategory(@RequestBody CategoryDto categoryDto) {
        CategoryEntity category = categoryService.addCategory(categoryDto);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/category_to_current_user")
    public ResponseEntity<?> addCategoryToCurrentUser(@RequestBody CategoryDto categoryDto) {
        CategoryEntity category = categoryService.addCategoryToCurrentUser(categoryDto);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/get_my_categories")
    public ResponseEntity<?> getMyCategories() {
        Set<CategoryEntity> categories = categoryService.getCurrentUserCategories();
        return ResponseEntity.ok(categories);
    }
}
