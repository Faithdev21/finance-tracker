package com.example.FinanceTracker.dto;

import java.math.BigDecimal;

public record BalanceSummaryDto(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
    public BalanceSummaryDto(double totalIncome,
                             double totalExpense,
                             double totalBalance) {
        this(BigDecimal.valueOf(totalIncome),
                BigDecimal.valueOf(totalExpense),
                BigDecimal.valueOf(totalBalance));
    }
}

