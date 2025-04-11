package com.example.FinanceTracker.repository;

import com.example.FinanceTracker.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserId(Long userId);
    List<TransactionEntity> findByUserIdAndCategoryId(Long userId, Long categoryId);
    List<TransactionEntity> findByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
