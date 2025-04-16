package com.example.FinanceTracker.dto;

import com.example.FinanceTracker.entity.BudgetPeriodEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BudgetResponseDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BudgetPeriodEntity period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal currentSpending;
}
