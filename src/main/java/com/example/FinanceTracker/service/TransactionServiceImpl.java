package com.example.FinanceTracker.service;

import com.example.FinanceTracker.dto.*;
import com.example.FinanceTracker.entity.BudgetEntity;
import com.example.FinanceTracker.entity.CategoryEntity;
import com.example.FinanceTracker.entity.TransactionEntity;
import com.example.FinanceTracker.repository.BudgetRepository;
import com.example.FinanceTracker.repository.TransactionRepository;
import com.example.FinanceTracker.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;

    public TransactionEntity createTransaction(TransactionRequest transactionRequest, UserEntity user) {
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

    public TransactionResponse toResponse(TransactionEntity transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getCategory().getName()
        );
    }

    @Override
    public Page<TransactionResponse> getUserTransactions(Long userId, TransactionFilterRequest transactionFilter) {
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
        List<TransactionResponse> response = transactionPage.getContent().stream()
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

    public List<DailyDynamics> getDailyDynamics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getDailyDynamics(userId, startDate, endDate);
    }

    public List<MonthlyStatisticDto> getMonthlyStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getMonthlyStatistics(userId, startDate, endDate);
    }
}
