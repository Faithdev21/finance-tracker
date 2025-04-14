package com.example.FinanceTracker.dto;

import java.math.BigDecimal;

public record CategorySummaryDto(
        Long categoryId,
        String categoryName,
        String type,
        BigDecimal totalAmount
) {}

