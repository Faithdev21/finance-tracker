package com.example.financetracker.service;

import com.example.financetracker.dto.*;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.TransactionEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.repository.BudgetRepository;
import com.example.financetracker.repository.TransactionRepository;
import com.example.financetracker.service.impl.BudgetServiceImpl;
import com.example.financetracker.service.impl.CategoryServiceImpl;
import com.example.financetracker.service.impl.TransactionServiceImpl;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionalServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryServiceImpl categoryService;

    @Mock
    BudgetServiceImpl budgetService;

    @Mock
    BudgetRepository budgetRepository;

    @InjectMocks
    TransactionServiceImpl transactionService;

    private UserEntity user;
    private CategoryEntity category;
    private BudgetEntity budget;
    private TransactionRequestDto transactionRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(1L);
        user.setUsername("testUser");

        category = CategoryEntity.builder()
                .id(1L)
                .name("Food")
                .type("EXPENSE")
                .build();

        budget = BudgetEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .limitAmount(BigDecimal.valueOf(1000))
                .period(BudgetPeriodDto.MONTHLY)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .build();

        transactionRequestDto = new TransactionRequestDto(BigDecimal.valueOf(500), "Test Transation", 1L);
    }

    @Test
    void createTransaction_ShouldReturnTransaction_WhenCategoryExists() {
        // Arrange
        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .amount(BigDecimal.valueOf(500))
                .description("Test transaction")
                .date(LocalDateTime.now())
                .build();

        when(categoryService.getCategoryByIdAndUser(1L, user)).thenReturn(category);
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
        when(budgetRepository.findByUserIdAndCategoryId(user.getId(), category.getId())).thenReturn(Collections.singletonList(budget));

        // Act
        TransactionEntity result = transactionService.createTransaction(transactionRequestDto, user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(result.getDescription()).isEqualTo("Test transaction");
        assertThat(result.getCategory()).isEqualTo(category);
        verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldThrowResourceNotFoundException_WhenCategoryDoesNotExist() {
        // Arrange
        when(categoryService.getCategoryByIdAndUser(1L, user))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        // Act and Assert
        assertThatThrownBy(() -> transactionService.createTransaction(transactionRequestDto, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldThrowIllegalArgumentException_WhenAmountIsNegative() {
        // Arrange
        TransactionRequestDto invalidRequest = new TransactionRequestDto(BigDecimal.valueOf(-500), "Test transaction", 1L);

        // Act and Assert
        assertThatThrownBy(() -> transactionService.createTransaction(invalidRequest, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldThrowIllegalArgumentException_WhenDescriptionIsEmpty() {
        // Arrange
        TransactionRequestDto invalidRequest = new TransactionRequestDto(BigDecimal.valueOf(500), "", 1L);

        // Act and Assert
        assertThatThrownBy(() -> transactionService.createTransaction(invalidRequest, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description must not be blank");
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void toResponse_ShouldReturnTransactionResponseDto_WhenTransactionExists() {
        // Arrange
        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .amount(BigDecimal.valueOf(500))
                .description("Test description")
                .date(LocalDateTime.now())
                .build();

        // Act
        TransactionResponseDto result = transactionService.toResponse(transaction);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.categoryName()).isEqualTo(category.getName());
        assertThat(result.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(result.description()).isEqualTo("Test description");
    }

    @Test
    void getUserTransactions_ShouldReturnTransactions_WhenCategoryFilterIsPresent() {
        // Arrange
        TransactionFilterRequestDto filter = new TransactionFilterRequestDto(
                Optional.of(LocalDateTime.now().minusDays(30)),
                Optional.of(LocalDateTime.now()),
                Optional.of(1L),
                0,
                10
        );
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").descending());

        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .amount(BigDecimal.valueOf(500))
                .description("Test description")
                .date(LocalDateTime.now())
                .build();

        Page<TransactionEntity> transactionPage = new PageImpl<>
                (Collections.singletonList(transaction), pageable, 1);
        when(transactionRepository.findByUserIdAndDateBetweenAndCategoryId(
                user.getId(), filter.startDate().get(), filter.endDate().get(),
                filter.categoryId().get(), pageable
        )).thenReturn(transactionPage);

        // Act
        Page<TransactionResponseDto> result = transactionService.getUserTransactions(user.getId(), filter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        TransactionResponseDto transactionResponseDto = result.getContent().get(0);

        assertThat(transactionResponseDto.id()).isEqualTo(1L);
        assertThat(transactionResponseDto.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(transactionResponseDto.description()).isEqualTo("Test description");
        assertThat(transactionResponseDto.categoryName()).isEqualTo("Food");
    }

    @Test
    void getUserTransactions_ShouldReturnTransactions_WhenCategoryFilterIsNotPresent() {
        // Arrange
        TransactionFilterRequestDto filter = new TransactionFilterRequestDto(
                Optional.of(LocalDateTime.now().minusDays(30)),
                Optional.of(LocalDateTime.now()),
                Optional.empty(),
                0,
                10);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").descending());

        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .amount(BigDecimal.valueOf(500))
                .description("Test transaction")
                .date(LocalDateTime.now())
                .build();

        Page<TransactionEntity> transactionPage = new PageImpl<>(Collections.singletonList(transaction), pageable, 1);
        when(transactionRepository.findByUserIdAndDateBetween(
                user.getId(), filter.startDate().get(), filter.endDate().get(), pageable))
                .thenReturn(transactionPage);

        // Act
        Page<TransactionResponseDto> result = transactionService.getUserTransactions(user.getId(), filter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        TransactionResponseDto transactionResponseDto = result.getContent().get(0);

        assertThat(transactionResponseDto.id()).isEqualTo(1L);
        assertThat(transactionResponseDto.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(transactionResponseDto.description()).isEqualTo("Test transaction");
        assertThat(transactionResponseDto.date()).isEqualTo(transaction.getDate());
        assertThat(transactionResponseDto.categoryName()).isEqualTo("Food");
    }

    @Test
    void getUserTransactions_ShouldHandleNegativePageAndZeroSize() {
        // Arrange
        TransactionFilterRequestDto filter = new TransactionFilterRequestDto(
                Optional.of(LocalDateTime.now().minusDays(30)),
                Optional.of(LocalDateTime.now()),
                Optional.empty(),
                -1, // Негативная страница
                0); // Нулевой размер
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").descending()); // Ожидаем page=0, size=10

        TransactionEntity transaction = TransactionEntity.builder()
                .id(1L)
                .user(user)
                .category(category)
                .amount(BigDecimal.valueOf(500))
                .description("Test transaction")
                .date(LocalDateTime.now())
                .build();

        Page<TransactionEntity> transactionPage = new PageImpl<>(Collections.singletonList(transaction), pageable, 1);
        when(transactionRepository.findByUserIdAndDateBetween(
                user.getId(), filter.startDate().get(), filter.endDate().get(), pageable))
                .thenReturn(transactionPage);

        // Act
        Page<TransactionResponseDto> result = transactionService.getUserTransactions(user.getId(), filter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);

        TransactionResponseDto transactionResponseDto = result.getContent().get(0);

        assertThat(transactionResponseDto.id()).isEqualTo(1L);
        assertThat(transactionResponseDto.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(transactionResponseDto.description()).isEqualTo("Test transaction");
        assertThat(transactionResponseDto.date()).isEqualTo(transaction.getDate());
        assertThat(transactionResponseDto.categoryName()).isEqualTo("Food");
    }

    @Test
    void getCategorySummary_ShouldReturnSummary_WhenDataExists() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        CategorySummaryDto summaryDto = new CategorySummaryDto(1L, "Food", "EXPENSE", BigDecimal.valueOf(500));

        when(transactionRepository.getCategorySummary(user.getId(), startDate, endDate)).thenReturn(Collections.singletonList(summaryDto));

        // Act
        List<CategorySummaryDto> result = transactionService.getCategorySummary(user.getId(), startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get(0)).isEqualTo(summaryDto);
    }

    @Test
    void getBalanceSummary_ShouldReturnSummary_WhenDataExists() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        BalanceSummaryDto summaryDto = new BalanceSummaryDto(BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(500));

        when(transactionRepository.getBalanceSummary(user.getId(), startDate, endDate)).thenReturn(summaryDto);

        // Act
        BalanceSummaryDto result = transactionService.getBalanceSummary(user.getId(), startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(summaryDto);
    }

    @Test
    void getDailyDynamics_ShouldReturnDynamics_WhenDataExists() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        DailyDynamicsDto dynamicsDto = new DailyDynamicsDto(LocalDate.now(), "EXPENSE", BigDecimal.valueOf(500));

        when(transactionRepository.getDailyDynamics(user.getId(), startDate, endDate)).thenReturn(Collections.singletonList(dynamicsDto));

        // Act
        List<DailyDynamicsDto> result = transactionService.getDailyDynamics(user.getId(), startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get(0)).isEqualTo(dynamicsDto);
    }

    @Test
    void getMonthlyStatistics_ShouldReturnStatistics_WhenDataExists() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        MonthlyStatisticDto statisticDto = new MonthlyStatisticDto(LocalDate.now(), BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(500));

        when(transactionRepository.getMonthlyStatistics(user.getId(), startDate, endDate))
                .thenReturn(Collections.singletonList(statisticDto));

        // Act
        List<MonthlyStatisticDto> result = transactionService.getMonthlyStatistics(user.getId(), startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get(0)).isEqualTo(statisticDto);
    }
}
