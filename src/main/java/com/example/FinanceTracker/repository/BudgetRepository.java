package com.example.FinanceTracker.repository;

import com.example.FinanceTracker.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    List<BudgetEntity> findByUserId (Long userId);
    List<BudgetEntity> findByUserIdAndCategoryId (Long userId, Long categoryId);
}
