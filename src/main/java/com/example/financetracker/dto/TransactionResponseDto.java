package com.example.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        Long id,
        BigDecimal amount,
        String description,
        LocalDateTime date,
        String categoryName
) {}
