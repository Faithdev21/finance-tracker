package com.example.financetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetResponseDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BudgetPeriodDto period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal currentSpending;
}
