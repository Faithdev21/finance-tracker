package com.example.FinanceTracker.dto;

import java.time.LocalDateTime;
import java.util.Optional;

public record TransactionFilterRequest(
        Optional<LocalDateTime> startDate,
        Optional<LocalDateTime> endDate,
        Optional<Long> categoryId,
        int page,
        int size
) {
    public TransactionFilterRequest {
        page = page < 0 ? 0 : page;
        size = size <= 0 ? 10 : size;
    }
}