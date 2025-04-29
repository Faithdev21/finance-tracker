package com.example.financetracker.service.impl;

import com.example.financetracker.dto.BudgetRequestDto;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.TransactionEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.exception.ForbiddenException;
import com.example.financetracker.exception.ResourceNotFoundException;
import com.example.financetracker.repository.BudgetRepository;
import com.example.financetracker.repository.CategoryRepository;
import com.example.financetracker.repository.TransactionRepository;
import com.example.financetracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationServiceImpl notificationService;
    private final CategoryRepository categoryRepository;

    @Value("${budget.notification.threshold:0.8}")
    private double notificationThreshold;

    @Transactional
    public BudgetEntity createBudget(BudgetRequestDto budgetRequest, UserEntity user) {
        CategoryEntity category = categoryRepository.findById(budgetRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        BudgetEntity budget = BudgetEntity.builder()
                .user(user)
                .category(category)
                .limitAmount(budgetRequest.getLimitAmount())
                .period(budgetRequest.getPeriod())
                .startDate(budgetRequest.getStartDate())
                .endDate(budgetRequest.getEndDate())
                .build();

        BudgetEntity savedBudget = budgetRepository.save(budget);
        checkBudgetStatus(savedBudget);
        return savedBudget;
    }

    public BigDecimal getCurrentSpending(BudgetEntity budget) {
        List<TransactionEntity> transactions = transactionRepository.findByUserIdAndDateBetween(
                budget.getUser().getId(), budget.getStartDate(), budget.getEndDate()
        );
        return transactions.stream()
                .filter(t -> t.getCategory().getId().equals(budget.getCategory().getId()))
                .filter(t -> t.getCategory().getType().equals("EXPENSE"))
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void checkBudgetStatus(BudgetEntity budget) {
        BigDecimal currentSpending = getCurrentSpending(budget);
        BigDecimal limit = budget.getLimitAmount();

        if (currentSpending.compareTo(limit.multiply(BigDecimal.valueOf(notificationThreshold))) >= 0) {
            notificationService.sendWarningNotification(budget, currentSpending);
        }
        if (currentSpending.compareTo(limit) > 0) {
            notificationService.sendOverLimitNotification(budget, currentSpending);
        }
    }

    public List<BudgetEntity> getUserBudgets(UserEntity user) {
        return budgetRepository.findByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public BudgetEntity getBudgetById(Long id, UserEntity user) {
        BudgetEntity budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this budget");
        }
        return budget;
    }

    @Transactional
    public BudgetEntity updateBudget(Long id, BudgetRequestDto budgetRequest, UserEntity user) {
        BudgetEntity budget = getBudgetById(id, user);

        CategoryEntity category = categoryRepository.findById(budgetRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        budget.setCategory(category);
        budget.setLimitAmount(budgetRequest.getLimitAmount());
        budget.setPeriod(budgetRequest.getPeriod());
        budget.setStartDate(budgetRequest.getStartDate());
        budget.setEndDate(budgetRequest.getEndDate());

        BudgetEntity updatedBudget = budgetRepository.save(budget);
        checkBudgetStatus(updatedBudget);
        return updatedBudget;
    }

    @Transactional
    public void deleteBudget(Long id, UserEntity user) {
        BudgetEntity budget = getBudgetById(id, user);
        budgetRepository.delete(budget);
    }
}