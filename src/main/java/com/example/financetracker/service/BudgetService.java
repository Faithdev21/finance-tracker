package com.example.financetracker.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.financetracker.dto.BudgetRequestDto;
import com.example.financetracker.entity.BudgetEntity;
import com.example.financetracker.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;

public interface BudgetService {
    @Transactional
    BudgetEntity createBudget(BudgetRequestDto budgetRequest, UserEntity user);

    BigDecimal getCurrentSpending(BudgetEntity budget);

    @Transactional
    void checkBudgetStatus(BudgetEntity budget);

    List<BudgetEntity> getUserBudgets(UserEntity user);

    @Transactional(readOnly = true)
    BudgetEntity getBudgetById(Long id, UserEntity user);

    @Transactional
    BudgetEntity updateBudget(Long id, BudgetRequestDto budgetRequest, UserEntity user);

    @Transactional
    void deleteBudget(Long id, UserEntity user);
}