package com.example.financetracker.repository;

import com.example.financetracker.dto.BalanceSummaryDto;
import com.example.financetracker.dto.CategorySummaryDto;
import com.example.financetracker.dto.DailyDynamicsDto;
import com.example.financetracker.dto.MonthlyStatisticDto;
import com.example.financetracker.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserId(Long userId);

    Page<TransactionEntity> findByUserIdAndDateBetweenAndCategoryId(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long categoryId,
            Pageable pageable
    );

    Page<TransactionEntity> findByUserIdAndDateBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    List<TransactionEntity> findByUserIdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT new com.example.financetracker.dto.CategorySummaryDto(" +
            "t.category.id, t.category.name, t.category.type, SUM(t.amount)) " +
            "FROM TransactionEntity t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category.id, t.category.name, t.category.type")
    List<CategorySummaryDto> getCategorySummary(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
            "new com.example.financetracker.dto.BalanceSummaryDto(" +
            "SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), " +
            "(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END) - " +
            "SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END))) " +
            "FROM TransactionEntity t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BalanceSummaryDto getBalanceSummary(@Param("userId") Long userId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.example.financetracker.dto.DailyDynamicsDto(" +
            "CAST(t.date AS localdate), " +
            "t.category.type, " +
            "SUM(t.amount)) " +
            "FROM TransactionEntity t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(t.date AS localdate), t.category.type " +
            "ORDER BY CAST(t.date AS localdate)")
    List<DailyDynamicsDto> getDailyDynamics(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);


    @Query("SELECT new com.example.financetracker.dto.MonthlyStatisticDto(" +
            "CAST(DATE_TRUNC('month', t.date) AS localdate), " +
            "SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), " +
            "(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END) - " +
            "SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END))) " +
            "FROM TransactionEntity t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE_TRUNC('month', t.date)" +
            "ORDER BY DATE_TRUNC('month', t.date) DESC")
    List<MonthlyStatisticDto> getMonthlyStatistics(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    }
