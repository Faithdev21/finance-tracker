package com.example.FinanceTracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyStatisticDto(
        LocalDate month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {}
