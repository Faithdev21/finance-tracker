package com.example.financetracker.service.impl;

import com.example.financetracker.dto.*;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.TransactionEntity;
import com.example.financetracker.repository.BudgetRepository;
import com.example.financetracker.repository.TransactionRepository;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryServiceImpl categoryService;
    private final BudgetServiceImpl budgetService;
    private final BudgetRepository budgetRepository;

    public TransactionEntity createTransaction(TransactionRequestDto transactionRequest, UserEntity user) {
        CategoryEntity category = categoryService.getCategoryByIdAndUser(transactionRequest.categoryId(), user);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(transactionRequest.amount());
        transaction.setDescription(transactionRequest.description());
        transaction.setDate(LocalDateTime.now());
        transaction.setCategory(category);
        transaction.setUser(user);

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        List<BudgetEntity> budgets = budgetRepository.findByUserIdAndCategoryId(user.getId(), category.getId());
        for (BudgetEntity budget : budgets) {
            budgetService.checkBudgetStatus(budget);
        }

        return savedTransaction;
    }

    public TransactionResponseDto toResponse(TransactionEntity transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getCategory().getName()
        );
    }

    @Override
    public Page<TransactionResponseDto> getUserTransactions(Long userId, TransactionFilterRequestDto transactionFilter) {
        Pageable pageable = PageRequest.of(transactionFilter.page(), transactionFilter.size(), Sort.by("date").descending());

        LocalDateTime startDate = transactionFilter.startDate().orElse(LocalDateTime.of(1970, 1, 1, 0, 0));
        LocalDateTime endDate = transactionFilter.endDate().orElse(LocalDateTime.now());

        Page<TransactionEntity> transactionPage;
        if (transactionFilter.categoryId().isPresent()) {
          transactionPage = transactionRepository.findByUserIdAndDateBetweenAndCategoryId(
                  userId, startDate, endDate, transactionFilter.categoryId().get(), pageable
          );
        } else {
            transactionPage = transactionRepository.findByUserIdAndDateBetween(
                    userId, startDate, endDate, pageable
            );
        }
        List<TransactionResponseDto> response = transactionPage.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PageImpl<>(response, pageable, transactionPage.getTotalElements());
    }

    public List<CategorySummaryDto> getCategorySummary(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getCategorySummary(userId, startDate, endDate);
    }

    public BalanceSummaryDto getBalanceSummary(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getBalanceSummary(userId, startDate, endDate);
    }

    public List<DailyDynamicsDto> getDailyDynamics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getDailyDynamics(userId, startDate, endDate);
    }

    public List<MonthlyStatisticDto> getMonthlyStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getMonthlyStatistics(userId, startDate, endDate);
    }
}
