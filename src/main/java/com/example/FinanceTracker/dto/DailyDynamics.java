package com.example.FinanceTracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyDynamics(
        LocalDate date,
        String type,
        BigDecimal totalAmount
) {}
