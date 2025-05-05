package com.example.financetracker.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.financetracker.dto.CategoryDto;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.repository.CategoryRepository;
import com.example.financetracker.repository.UserRepository;
import com.example.financetracker.service.impl.CategoryServiceImpl;
import com.example.financetracker.service.impl.UserServiceImpl;

public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    void getCategoryByIdAndUser_ShouldReturnCategory_WhenCategoryExists() {
        // Arrange
        Long categoryId = 1L;
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("testCategory");
        user.setCategories(new HashSet<>(Collections.singletonList(category)));

        // Act
        CategoryEntity result = categoryService.getCategoryByIdAndUser(categoryId, user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("testCategory");
    }

    @Test
    void getCategoryByIdAndUser_ShouldThrowResourceNotFoundException_WhenCategoryDoesNotExist() {
        // Arrange
        Long categoryId = 1L;
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setCategories(new HashSet<>());

        // Act and Assert
        assertThatThrownBy(() -> categoryService.getCategoryByIdAndUser(categoryId, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: " + categoryId + " for user: " + user.getUsername());
    }


    @Test
    void addCategory_ShouldReturnEmptyList_WhenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findByNameIn(Arrays.asList("Еда", "Транспорт", "Зарплата")))
                .thenReturn(Collections.emptyList());

        // Act
        List<CategoryEntity> result = categoryService.getDefaultCategories();

        // Arrange
        assertThat(result).isEmpty();
    }

    @Test
    void addCategory_ShouldSaveCategory_WhenCategoryDoesNotExist() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("NewCategory");
        categoryDto.setType("EXPENSE");
        when(categoryRepository.existsByName("NewCategory")).thenReturn(false);
        CategoryEntity savedCategory = CategoryEntity.builder()
                .name("NewCategory")
                .type("EXPENSE")
                .build();
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(savedCategory);

        // Act
        CategoryEntity result = categoryService.addCategory(categoryDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("NewCategory");
        assertThat(result.getType()).isEqualTo("EXPENSE");
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void addCategory_ShouldThrowException_WhenCategoryExists() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("ExistingCategory");
        when(categoryRepository.existsByName("ExistingCategory")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.addCategory(categoryDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category with name 'ExistingCategory' exists already");
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void addCategoryToCurrentUser_ShouldAddNewCategory_WhenCategoryDoesNotExist() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("NewCategory");
        categoryDto.setType("EXPENSE");

        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setCategories(new HashSet<>());

        CategoryEntity newCategory = CategoryEntity.builder()
                .name("NewCategory")
                .type("EXPENSE")
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByName("NewCategory")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(newCategory);
        when(userService.save(any(UserEntity.class))).thenReturn(user);

        // Act
        CategoryDto result = categoryService.addCategoryToCurrentUser(categoryDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("NewCategory");
        assertThat(result.getType()).isEqualTo("EXPENSE");
        assertThat(user.getCategories()).contains(newCategory);
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
        verify(userService, times(1)).save(user);
    }

    @Test
    void addCategoryToCurrentUser_ShouldAddExistingCategory_WhenCategoryExists() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("ExistingCategory");
        categoryDto.setType("EXPENSE");

        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setCategories(new HashSet<>());

        CategoryEntity existingCategory = CategoryEntity.builder()
                .name("ExistingCategory")
                .type("EXPENSE")
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByName("ExistingCategory")).thenReturn(Optional.of(existingCategory));
        when(userService.save(any(UserEntity.class))).thenReturn(user);

        // Act
        CategoryDto result = categoryService.addCategoryToCurrentUser(categoryDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ExistingCategory");
        assertThat(result.getType()).isEqualTo("EXPENSE");
        assertThat(user.getCategories()).contains(existingCategory);
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
        verify(userService, times(1)).save(user);
    }

    @Test
    void addCategoryToCurrentUser_ShouldNotAddCategory_WhenCategoryAlreadyAssociated() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("ExistingCategory");
        categoryDto.setType("EXPENSE");

        CategoryEntity existingCategory = CategoryEntity.builder()
                .name("ExistingCategory")
                .type("EXPENSE")
                .build();

        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setCategories(new HashSet<>(Collections.singletonList(existingCategory)));

        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByName("ExistingCategory")).thenReturn(Optional.of(existingCategory));

        // Act
        CategoryDto result = categoryService.addCategoryToCurrentUser(categoryDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ExistingCategory");
        assertThat(user.getCategories()).hasSize(1);
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
        verify(userService, never()).save(any(UserEntity.class));
    }

}
