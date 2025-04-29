package com.example.financetracker.dto;

import java.math.BigDecimal;

public record CategorySummaryDto(
        Long categoryId,
        String categoryName,
        String type,
        BigDecimal totalAmount
) {}

