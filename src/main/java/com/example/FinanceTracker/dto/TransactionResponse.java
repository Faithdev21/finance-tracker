package com.example.FinanceTracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String description,
        LocalDateTime date,
        String categoryName
) {}
