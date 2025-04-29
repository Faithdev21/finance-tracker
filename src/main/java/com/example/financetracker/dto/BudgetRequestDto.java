package com.example.financetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetRequestDto {
    private Long categoryId;
    private BigDecimal limitAmount;
    private BudgetPeriodDto period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
