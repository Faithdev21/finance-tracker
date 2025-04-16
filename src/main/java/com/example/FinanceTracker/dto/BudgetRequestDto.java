package com.example.FinanceTracker.dto;

import com.example.FinanceTracker.entity.BudgetPeriodEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetRequestDto {
    private Long categoryId;
    private BigDecimal limitAmount;
    private BudgetPeriodEntity period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
