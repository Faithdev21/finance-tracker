package com.example.financetracker.service;

import com.example.financetracker.dto.BudgetPeriodDto;
import com.example.financetracker.dto.BudgetRequestDto;
import com.example.financetracker.dto.BudgetResponseDto;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.TransactionEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.exception.ForbiddenException;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.repository.BudgetRepository;
import com.example.financetracker.repository.CategoryRepository;
import com.example.financetracker.repository.TransactionRepository;
import com.example.financetracker.service.impl.BudgetServiceImpl;
import com.example.financetracker.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BudgetServiceTest {

    @Mock
    BudgetRepository budgetRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    NotificationServiceImpl notificationService;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    BudgetServiceImpl budgetService;

    private UserEntity user;
    private CategoryEntity category;
    private BudgetEntity budget;
    private BudgetRequestDto budgetRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(budgetService, "notificationThreshold", 0.8);

        this.user = new UserEntity();
        this.user.setId(1L);
        this.user.setUsername("testUser");

        this.category = CategoryEntity.builder()
                .id(1L)
                .name("Food")
                .type("EXPENSE")
                .build();

        this.budget = BudgetEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .limitAmount(BigDecimal.valueOf(1000))
                .period(BudgetPeriodDto.MONTHLY)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .build();

        this.budgetRequest = new BudgetRequestDto();
        this.budgetRequest.setCategoryId(1L);
        this.budgetRequest.setLimitAmount(BigDecimal.valueOf(1000));
        this.budgetRequest.setPeriod(BudgetPeriodDto.MONTHLY);
        this.budgetRequest.setStartDate(LocalDateTime.now().minusDays(10));
        this.budgetRequest.setEndDate(LocalDateTime.now().plusDays(20));
    }

    @Test
    void createBudget_ShouldReturnBudget_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(BudgetEntity.class))).thenReturn(budget);
        when(transactionRepository.findByUserIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        BudgetEntity result = budgetService.createBudget(budgetRequest, user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.getCategory().getName()).isEqualTo(category.getName());
        assertThat(result.getLimitAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.getPeriod()).isEqualTo(BudgetPeriodDto.MONTHLY);
        verify(budgetRepository, times(1)).save(any(BudgetEntity.class));
    }

    @Test
    void createBudget_ShouldReturnBudget_WhenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(() -> budgetService.createBudget(budgetRequest, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
        verify(budgetRepository, never()).save(any(BudgetEntity.class));
    }

    @Test
    void getCurrentSpending_ShouldReturnZero_WhenNoTransactions() {
        // Arrange
        when(transactionRepository.findByUserIdAndDateBetween(user.getId(), budget.getStartDate(), budget.getEndDate())).thenReturn(Collections.emptyList());

        // Act
        BigDecimal result = budgetService.getCurrentSpending(budget);

        // Asser
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentSpending_ShouldReturnSum_WhenTransactionsExist() {
        // Arrange
        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(this.user)
                .category(this.category)
                .amount(BigDecimal.valueOf(500))
                .date(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdAndDateBetween(
                this.user.getId(), this.budget.getStartDate(), this.budget.getEndDate()))
                .thenReturn(Collections.singletonList(transaction));

        // Act
        BigDecimal result = budgetService.getCurrentSpending(this.budget);

        // Assert
        assertThat(result).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void checkBudgetStatus_ShouldSendOverLimitNotification_WhenLimitExceeded() {
        // Arrange
        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(this.user)
                .category(this.category)
                .amount(BigDecimal.valueOf(1100)) // Exceeds limit
                .date(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdAndDateBetween(
                this.user.getId(), this.budget.getStartDate(), this.budget.getEndDate()))
                .thenReturn(Collections.singletonList(transaction));

        // Act
        budgetService.checkBudgetStatus(this.budget);

        // Assert
        verify(notificationService, times(1)).sendWarningNotification(this.budget, BigDecimal.valueOf(1100));
        verify(notificationService, times(1)).sendOverLimitNotification(this.budget, BigDecimal.valueOf(1100));
    }

    @Test
    void getUserBudget_ShouldReturnBudget_WhenBudgetExists() {
        // Arrange
        when(budgetRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(budget));
        when(transactionRepository.findByUserIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<BudgetEntity> result = budgetService.getUserBudgets(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get(0).getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.get(0).getCategory().getName()).isEqualTo(category.getName());
        assertThat(result.get(0).getLimitAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.get(0).getPeriod()).isEqualTo(BudgetPeriodDto.MONTHLY);
    }

    @Test
    void getBudgetId_ShouldReturnBudget_WhenBudgetExistsAndUserHasPermission() {
        // Arrange
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(transactionRepository.findByUserIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        BudgetEntity result = budgetService.getBudgetById(1L, user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.getCategory().getName()).isEqualTo(category.getName());
        assertThat(result.getLimitAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.getPeriod()).isEqualTo(BudgetPeriodDto.MONTHLY);
    }

    @Test
    void getBudgetId_ShouldThrowResourceNotFoundException_WhenBudgetDoesNotExist() {
        // Arrange
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(() -> budgetService.getBudgetById(1L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Budget not found");
    }

    @Test
    void getBudgetId_ShouldThrowForbiddenException_WhenUserDoesNotHavePermission() {
        // Arrange
        UserEntity alien = new UserEntity();
        alien.setId(2L);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // Act and Assert
        assertThatThrownBy(() -> budgetService.getBudgetById(1L, alien))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to access this budget");
    }

    @Test
    void updateBudget_ShouldReturnUpdatedBudget_WhenBudgetExists() {
        // Arrange
        budgetRequest.setLimitAmount(BigDecimal.valueOf(2000));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(budget)).thenReturn(budget);

        // Act
        BudgetEntity result = budgetService.updateBudget(1L, budgetRequest, user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.getCategory().getName()).isEqualTo(category.getName());
        assertThat(result.getLimitAmount()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(result.getPeriod()).isEqualTo(BudgetPeriodDto.MONTHLY);
        verify(budgetRepository, times(1)).save(budget);
    }

    @Test
    void updateBudget_ShouldThrowResourceNotFoundException_WhenBudgetDoesNotExist() {
        // Arrange
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(() -> budgetService.updateBudget(1L, budgetRequest, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
        verify(budgetRepository, never()).save(any(BudgetEntity.class));
    }

    @Test
    void deleteBudget_ShouldDeleteBudget_WhenBudgetExists() {
        // Arrange
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // Act
        budgetService.deleteBudget(1L, user);

        // Assert
        verify(budgetRepository, times(1)).delete(budget);
    }

    @Test
    void deleteBudget_ShouldThrowForbiddenException_WhenUserHasNoPermission() {
        // Arrange
        UserEntity alien = new UserEntity();
        alien.setId(2L);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // Act & Assert
        assertThatThrownBy(() -> budgetService.deleteBudget(1L, alien))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to access this budget");
        verify(budgetRepository, never()).delete(any(BudgetEntity.class));
    }

}
